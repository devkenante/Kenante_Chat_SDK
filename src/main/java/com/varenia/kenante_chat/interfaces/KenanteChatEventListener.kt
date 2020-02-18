package com.varenia.kenante_chat.interfaces

import com.varenia.kenante_chat.core.KenanteChatMessage

interface KenanteChatEventListener {

    fun onChatJoined()
    fun onChatUserConnected(userId: Int, channelName: String)
    fun onChatUserLeft(userId: Int)
    fun onMessage(chatMessage: KenanteChatMessage)
    fun onError(message: String?)
    fun onChatLeft()

}