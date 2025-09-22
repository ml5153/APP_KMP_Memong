package com.memong.aos.data.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.memong.aos.data.database.MemoDatabase
import com.avatye.haru.log.LogTrack
import com.memong.aos.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipOutputStream

object AutoBackupUtil {

    suspend fun performAutoBackupIfNeeded(context: Context) {
        if (!PreferenceUtil.get(PreferenceUtil.KEY_AUTO_BACKUP, true)) return

        val memoDao = MemoDatabase.getInstance(context).memoDao()
        val memos = memoDao.getAllNoConditionMemos()

        if (memos.isEmpty()) return

        withContext(Dispatchers.IO) {
            try {
                val now = Date()
                val todayDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(now)
                val fullTimeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(now)

                val backupDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "HaruMemo"
                )
                if (!backupDir.exists()) backupDir.mkdirs()

                val backupFile = File(backupDir, "${fullTimeStamp}_auto_memong.zip")

                // 기존 같은 날짜의 auto 백업 파일 삭제
                backupDir.listFiles()?.forEach { file ->
                    if (file.name.startsWith(todayDate) && file.name.endsWith("_auto_memong.zip")) {
                        file.delete()
                    }
                }

                val dbFile = context.getDatabasePath("memopad.db")
                val dbDir = dbFile.parentFile!!
                val walFile = File(dbDir, "memopad.db-wal")
                val shmFile = File(dbDir, "memopad.db-shm")

                val filesToZip = mutableListOf<Pair<File, String>>()
                filesToZip.add(dbFile to "memopad.db")
                if (walFile.exists()) filesToZip.add(walFile to "memopad.db-wal")
                if (shmFile.exists()) filesToZip.add(shmFile to "memopad.db-shm")

                if (PreferenceUtil.get(PreferenceUtil.KEY_PHOTO_BACKUP, true)) {
                    val allImagePaths = memos.flatMap { it.imagePath.values.flatten() }

                    val resolvedImageFiles = allImagePaths.mapNotNull { path ->
                        if (path.startsWith("content://") && path.contains("/cache/")) {
                            null
                        } else {
                            val file = if (path.startsWith("content://")) {
                                copyContentUriToFile(context, path)
                            } else {
                                File(path).takeIf { it.exists() && it.canRead() }
                            }
                            file
                        }
                    }
                        .distinctBy { it.absolutePath }  // 같은 파일 경로 제거
                        .map { it to "images/${it.name}" } // 원본 파일명 유지

                    filesToZip.addAll(resolvedImageFiles)
                }

                ZipOutputStream(BufferedOutputStream(FileOutputStream(backupFile))).use { zipOut ->
                    filesToZip.forEach { (file, entryName) ->
                        zipOut.putNextEntry(ZipEntry(entryName))
                        file.inputStream().use { it.copyTo(zipOut) }
                        zipOut.closeEntry()
                    }
                    zipOut.flush()
                }

                LogTrack.i("AutoBackup") { "Auto backup completed: ${backupFile.absolutePath} (${backupFile.length()} bytes)" }

                withContext(Dispatchers.Main) {
                    EventUtil.sendEvent(
                        context,
                        EventUtil.CATEGORY_SET_BACKUP,
                        EventUtil.ACTION_USE_AUTO_BACKUP
                    )
                    val dateFormatKorean = SimpleDateFormat("yyyy년 M월 d일", Locale.KOREA)
                    val dayOfWeek = SimpleDateFormat("E", Locale.KOREA).format(now)
                    val timeFormat = SimpleDateFormat("h:mm a", Locale.US)

                    val formattedTime =
                        "${dateFormatKorean.format(now)} ($dayOfWeek) ${timeFormat.format(now)}"
                    PreferenceUtil.set(PreferenceUtil.KEY_LAST_LOCAL_BACKUP_TIME, formattedTime)

                    if (PreferenceUtil.get(PreferenceUtil.KEY_AUTO_BACKUP_NOTIFICATION, false)) {
                        AutoBackupNotificationUtil.showNotification(context, formattedTime, true)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    if (PreferenceUtil.get(PreferenceUtil.KEY_AUTO_BACKUP_NOTIFICATION, false)) {
                        val reason = when (e) {
                            is FileNotFoundException -> context.getString(R.string.haru_backup_error_file_not_found)
                            is SecurityException -> context.getString(R.string.haru_backup_error_permission_denied)
                            is ZipException -> context.getString(R.string.haru_backup_error_zip)
                            is IOException -> context.getString(R.string.haru_backup_error_io)
                            is IllegalArgumentException -> context.getString(R.string.haru_backup_error_invalid_path)
                            is NullPointerException -> context.getString(R.string.haru_backup_error_null)
                            else -> e.localizedMessage ?: context.getString(R.string.haru_backup_error_unknown)
                        }
                        AutoBackupNotificationUtil.showNotification(context, reason, false)
                    }
                }
            }
        }
    }

    private fun copyContentUriToFile(context: Context, uriString: String): File? {
        return try {
            val uri = Uri.parse(uriString)
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null

            // 파일명 추출
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: return null
            val tempFile = File(context.cacheDir, fileName)

            FileOutputStream(tempFile).use { output ->
                val copiedSize = inputStream.copyTo(output)
                LogTrack.d("Backup") { "Copied URI to temp file: $fileName ($copiedSize bytes)" }
                if (copiedSize == 0L) return null
            }

            tempFile
        } catch (e: Exception) {
            LogTrack.e { "URI 복사 실패: $e" }
            null
        }
    }

}

