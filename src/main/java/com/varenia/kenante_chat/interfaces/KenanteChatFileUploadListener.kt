package com.varenia.kenante_chat.interfaces

import com.varenia.kenante_chat.core.KenanteFile

interface KenanteChatFileUploadListener {

    fun onProgress(i: Int)
    fun onSuccess(file: KenanteFile)
    fun onError(error: String)

}