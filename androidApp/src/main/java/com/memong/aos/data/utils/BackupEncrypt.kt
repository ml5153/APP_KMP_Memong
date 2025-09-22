package com.memong.aos.data.utils

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.GeneralSecurityException
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.SecretKeySpec

object BackupEncrypt {

    private fun getKey(): ByteArray {
        val rawKey = "GionNetworkMemoGLocalBackup5118".toByteArray(Charsets.UTF_8)
        val sha = MessageDigest.getInstance("SHA-1").digest(rawKey)
        return sha.copyOf(16) // AES key = 128 bit
    }

    @Throws(IOException::class, GeneralSecurityException::class)
    fun decrypt(inputStream: InputStream, outputStream: OutputStream) {
        val keySpec = SecretKeySpec(getKey(), "AES")
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, keySpec)

        CipherInputStream(inputStream, cipher).use { cis ->
            val buffer = ByteArray(8)
            var bytesRead: Int
            while (cis.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            outputStream.flush()
        }
    }

    @Throws(IOException::class, GeneralSecurityException::class)
    fun encrypt(inputStream: InputStream, outputStream: OutputStream) {
        val keySpec = SecretKeySpec(getKey(), "AES")
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec)

        CipherOutputStream(outputStream, cipher).use { cos ->
            val buffer = ByteArray(8)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                cos.write(buffer, 0, bytesRead)
            }
            cos.flush()
        }
    }
}
