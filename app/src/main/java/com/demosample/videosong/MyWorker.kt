package com.demosample.videosong

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MyWorker(context: Context, params: WorkerParameters) :
        CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            val name = inputData.getString("array")
            val i = 0
            if (name != null) {

                StringToBitMap(name)?.let {
                    createDirectoryAndSaveFile(it, SimpleDateFormat("yyyyMMdd_HHmmss",
                            Locale.getDefault()).format(Date()))
                }

            }
            Result.success()
        } catch (error: Throwable) {
            Result.failure()
        }
    }

    /**
     * Convert Bitmpa to strinf
     */

    fun StringToBitMap(encodedString: String?): Bitmap? {
        return try {
            val encodeByte = Base64.decode(encodedString, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
        } catch (e: Exception) {
            e.message
            null
        }
    }

    /**
     * Save in a directory
     */
    private fun createDirectoryAndSaveFile(imageToSave: Bitmap, fileName: String) {
        val direct = File(applicationContext.externalMediaDirs.first(), "Pictures")
        if (!direct.exists()) {
            val wallpaperDirectory = File(direct, "Pictures")
            wallpaperDirectory.mkdirs()
        }
        val file = File(direct, "$fileName.jpg")
        if (file.exists()) {
            file.delete()
        }
        try {
            val out = FileOutputStream(file)
            imageToSave.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}
