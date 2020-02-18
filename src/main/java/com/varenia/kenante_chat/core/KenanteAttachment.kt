package com.varenia.kenante_chat.core

class KenanteAttachment(val name: String, val extension: String, val url: String, val fileType: String) {

    companion object{

        fun isFileSupported(extension: String): Boolean {
            val supportedFormats = arrayOf("jpg", "png", "jpeg", "gif", "mp4", "m4a", "3gp", "flv", "wav",
                    "mp3", "aac", "wma", "pdf", "pptx", "xls", "xlsx", "doc", "docx")
            val ext = extension.toLowerCase()
            if(supportedFormats.contains(ext))
                return true
            return false
        }

        internal fun getFileType(extension: String): String {
            val ext = extension.toLowerCase()
            if (ext == "jpg" || ext == "png" || ext == "jpeg" || ext == "gif") {
                return "image"
            } else if (ext == "mp4" || ext == "m4a" || ext == "3gp" || ext == "flv" || ext == "wav") {
                return "vidoes"
            } else if (ext == "mp3" || ext == "aac" || ext == "wma") {
                return "audio"
            } else if (ext == "pdf" || ext == "pptx" || ext == "xls" || ext == "xlsx" || ext == "doc" || ext == "docx") {
                return "documents"
            }
            return "UnsupportedFormat"
        }

    }



}