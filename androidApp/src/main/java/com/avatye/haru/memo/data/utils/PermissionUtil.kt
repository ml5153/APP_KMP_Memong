package com.avatye.haru.memo.data.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

internal object PermissionUtil {

    fun hasImageReadPermission(context: Context): Boolean {
        return requiredImageReadPermissions().all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun hasImageWritePermission(context: Context): Boolean {
        return requiredImageWritePermissions().all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestImageReadPermission(activity: Activity, requestCode: Int) {
        val permissions = requiredImageReadPermissions()
        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, permissions, requestCode)
        }
    }

    fun requestImageWritePermission(activity: Activity, requestCode: Int) {
        val permissions = requiredImageWritePermissions()
        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, permissions, requestCode)
        }
    }

    fun shouldShowReadRationale(activity: Activity): Boolean {
        return requiredImageReadPermissions().any {
            ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
        }
    }

    fun shouldShowWriteRationale(activity: Activity): Boolean {
        return requiredImageWritePermissions().any {
            ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
        }
    }

    private fun requiredImageReadPermissions(): Array<String> {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            else -> emptyArray()
        }
    }

    private fun requiredImageWritePermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            emptyArray()  // Android 10 이상은 권한 없이 MediaStore 사용 가능
        }
    }
}