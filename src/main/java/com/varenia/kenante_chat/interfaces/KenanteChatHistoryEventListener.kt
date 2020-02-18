package com.varenia.kenante_chat.interfaces

import com.varenia.kenante_chat.core.KenanteChatMessage

interface KenanteChatHistoryEventListener {

    fun onSuccess(messages: ArrayList<KenanteChatMessage>, userId: Int)
    fun onError(reason: String)

}