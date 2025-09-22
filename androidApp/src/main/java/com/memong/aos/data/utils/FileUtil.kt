package com.memong.aos.data.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.memong.aos.data.extension.toUri
import java.io.File
import java.io.FileOutputStream

object FileUtil {


    /**
     * 단일 비트맵 URI
     */
    fun getUriForCompressedBitmap(context: Context, bitmap: Bitmap): Uri? {
        return getBitmapCompressedFile(context, bitmap)?.toUri(context)
    }

    /**
     * 다중 비트맵 URI
     */
    fun getUriForCompressedBitmaps(context: Context, bitmaps: List<Bitmap>): List<Uri> {
        return getBitmapCompressedFiles(context, bitmaps).map {
            it.toUri(context)
        }
    }



    private fun getBitmapCompressedFiles(
        context: Context,
        bitmaps: List<Bitmap>
    ): List<File> {
        return bitmaps.mapIndexedNotNull { index, bitmap ->
            getBitmapCompressedFile(
                context = context,
                bitmap = bitmap
            )
        }
    }

    private fun getBitmapCompressedFile(
        context: Context,
        bitmap: Bitmap
    ): File? {
        return try {
            val fileName = "memo_img_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, fileName)  // cacheDir → filesDir
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}
