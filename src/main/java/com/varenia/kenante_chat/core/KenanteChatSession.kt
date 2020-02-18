package com.varenia.kenante_chat.core

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.SparseArray
import androidx.core.util.containsValue
import com.varenia.kenante_chat.enums.KenanteChatMessageAction
import com.varenia.kenante_chat.interfaces.KenanteChatEventListener
import com.varenia.kenante_chat.interfaces.KenanteChatHistoryEventListener
import com.varenia.kenante_core.core.KenanteSettings
import com.varenia.kenante_core.interfaces.KenanteWsConnEventListener
import org.json.JSONObject
import java.lang.Exception
import java.lang.RuntimeException

class KenanteChatSession private constructor() : KenanteWsConnEventListener {

    private val TAG = KenanteChatSession::class.java.simpleName
    private var webSocketConnected = false
    private var kenanteChatEventListener: KenanteChatEventListener? = null
    private var userId: Int = 0
    internal var handler = Handler(KenanteSettings.getInstance().getContext()!!.mainLooper)
    internal var chatChannels = SparseArray<String>()
    internal var chatHistoryEventListener: KenanteChatHistoryEventListener? = null
    internal var connectedToChat = false

    companion object {
        private var kInstance: KenanteChatSession? = null
        fun getInstance(): KenanteChatSession {
            if (kInstance == null) {
                kInstance = KenanteChatSession()
            }
            return kInstance!!
        }
    }

    fun setChatListener(listener: KenanteChatEventListener) {
        this.kenanteChatEventListener = listener
    }

    fun enterChat(roomId: Int, userId: Int) {
        if (connectedToChat) {
            kenanteChatEventListener?.onError("Already connected to chat")
            return
        }
        this.userId = userId
        var chatRoom = "".plus(roomId).plus("/").plus(userId)
        chatRoom = "ws/chat/$chatRoom/"
        KenanteChatWebSocket.setListener(this)
        KenanteChatWebSocket.room = chatRoom
        KenanteChatWebSocket.connect()
    }

    fun sendMessage(chatMessage: KenanteChatMessage) {
        if (!chatChannels.containsValue(chatMessage.channel))
            throw RuntimeException("Incorrect channel")
        KenanteChatWsSendMessages.sendMessage(chatMessage)
    }

    fun getChatHistory(listener: KenanteChatHistoryEventListener, roomId: Int, userId: Int) {
        chatHistoryEventListener = listener
        KenanteChatWsSendMessages.requestHistory(roomId, userId)
    }

    fun leaveChat() {
        KenanteChatWsSendMessages.leaveChat(userId)
        KenanteChatWebSocket.disconnect()
        handler.post {
            kenanteChatEventListener?.onChatLeft()
        }
    }

    private fun releaseChatObjects() {
        if (webSocketConnected) {
            webSocketConnected = false
            //Todo: Release all chat objects
        }
        connectedToChat = false
        chatChannels.clear()
    }

    override fun onOpen() {
        webSocketConnected = true
        KenanteChatMessageParser.setListener(kenanteChatEventListener)
    }

    override fun onMessage(obj: JSONObject) {
        KenanteChatMessageParser.handleMessage(obj)
    }

    override fun onError(ex: Exception) {
        handler.post {
            kenanteChatEventListener?.onError(ex.message)
        }
        releaseChatObjects()
    }

    override fun onClose() {
        connectedToChat = false
        handler.post {
            kenanteChatEventListener?.onError("Connection closed")
        }
        releaseChatObjects()
    }

    override fun onDisconnected() {
        releaseChatObjects()
    }


}