package com.avatye.haru.memo.data.extension

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * <Manifest> android:authorities="${applicationId}.fileprovider" 설정 필요
 * */
fun File.toUri(context: Context): Uri =
    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", this)
