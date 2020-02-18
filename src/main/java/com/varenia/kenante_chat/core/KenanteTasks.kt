package com.varenia.kenante_chat.core

import android.content.Context
import android.os.AsyncTask
import android.os.Handler
import android.util.Log
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.*
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.PutObjectRequest
import com.varenia.kenante_chat.interfaces.KenanteChatFileUploadListener
import com.varenia.kenante_core.core.KenanteSettings
import java.io.File
import java.lang.Exception


object KenanteTasks {

    val TAG = KenanteTasks::class.java.simpleName
    val s3Bucket = "kenantechatmedia"
    val region = "ap-south-1"
    var fileUploadListener: KenanteChatFileUploadListener? = null
    var handler = Handler(KenanteSettings.getInstance().getContext()!!.mainLooper)

    var context: Context? = null
    var s3Client: AmazonS3? = null
    var transferUtility: TransferUtility? = null

    fun init(context: Context) {
        if (this.context == null) {
            this.context = context
        }
        if (s3Client == null) {
            setAwsClient()
        }
        if (transferUtility == null) {
            setTransferUtility(context)
        }
    }

    fun uploadFile(file: File, listener: KenanteChatFileUploadListener) {
        this.fileUploadListener = listener
        Upload(file).execute()
    }

    private fun setAwsClient() {
        s3Client = AmazonS3Client(BasicAWSCredentials("AKIA6A4HCVMZZNSXMCUK",
                "tIZY2Zp3t1o2XQdhHznOzn+f5TsgOmNOR3ZX56EA"))
        s3Client?.setRegion(Region.getRegion(Regions.AP_SOUTH_1))
    }

    private fun setTransferUtility(context: Context) {
        transferUtility = TransferUtility.builder()
                .context(context)
                .s3Client(s3Client)
                .build()
    }

    private fun upload(file: File, fileName: String, extension: String, fileType: String) {
        val key = "media/$fileType/$fileName"
        val transferObserver = transferUtility?.upload(s3Bucket, key, file,
                CannedAccessControlList.PublicRead)

        /*val putObjectRequest = PutObjectRequest(applicationBucket, fileName, file)
        putObjectRequest.cannedAcl = CannedAccessControlList.PublicRead
        s3Client?.putObject(putObjectRequest)
        val path = "media/image_videos_documents"
        val fileUrl = "https://$applicationBucket.s3.$region.amazonaws.com/$fileName"
        val kenanteFile = KenanteFile(fileName, fileUrl, extension)
        fileUploadListener?.onSuccess(kenanteFile)*/

        transferObserver?.setTransferListener(object : TransferListener {
            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                Log.e(TAG, "Progress: id: $id, bytesCurrent: $bytesCurrent, bytesTotal: $bytesTotal")
                handler.post {
                    fileUploadListener?.onProgress(id)
                }
            }

            override fun onStateChanged(id: Int, state: TransferState?) {
                when (state) {
                    TransferState.WAITING -> {
                        Log.e(TAG, "WAITING")
                    }
                    TransferState.IN_PROGRESS -> {
                        Log.e(TAG, "IN_PROGRESS")
                    }
                    TransferState.PAUSED -> {
                        Log.e(TAG, "PAUSED")
                    }
                    TransferState.RESUMED_WAITING -> {
                        Log.e(TAG, "RESUMED_WAITING")
                    }
                    TransferState.COMPLETED -> {

                        Log.e(TAG, "COMPLETED")

                        val fileUrl = "https://$s3Bucket.s3.$region.amazonaws.com/$key"
                        handler.post {
                            fileUploadListener?.onSuccess(KenanteFile(fileName, fileUrl,
                                    extension, fileType))
                        }

                    }
                    TransferState.CANCELED -> {
                        Log.e(TAG, "CANCELED")
                    }
                    TransferState.FAILED -> {
                        Log.e(TAG, "FAILED")
                        handler.post {
                            fileUploadListener?.onError("Upload failed")
                        }
                    }
                    TransferState.WAITING_FOR_NETWORK -> {
                        Log.e(TAG, "WAITING_FOR_NETWORK")
                    }
                    TransferState.PART_COMPLETED -> {
                        Log.e(TAG, "PART_COMPLETED")
                    }
                    TransferState.PENDING_CANCEL -> {
                        Log.e(TAG, "PENDING_CANCEL")
                    }
                    TransferState.PENDING_PAUSE -> {
                        Log.e(TAG, "PENDING_PAUSE")
                    }
                    TransferState.PENDING_NETWORK_DISCONNECT -> {
                        Log.e(TAG, "PENDING_NETWORK_DISCONNECT")
                    }
                    TransferState.UNKNOWN -> {
                        Log.e(TAG, "UNKNOWN")
                    }
                }
            }

            override fun onError(id: Int, ex: Exception?) {
                Log.e(TAG, ex?.message)
                handler.post {
                    fileUploadListener?.onError(ex?.message!!)
                }
            }

        })
    }

    class Upload(val file: File) : AsyncTask<String, String, String>() {
        override fun doInBackground(vararg p0: String?): String {
            val fileName = file.toString().substring(file.toString().lastIndexOf("/") + 1)
            val extension = fileName.substring(fileName.lastIndexOf(".") + 1)
            val fileType = KenanteAttachment.getFileType(extension)
            upload(file, fileName, extension, fileType)
            return ""
        }

    }



}