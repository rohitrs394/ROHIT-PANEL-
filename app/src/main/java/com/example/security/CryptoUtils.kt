package com.example.security

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import java.security.MessageDigest
import java.security.SecureRandom

object CryptoUtils {

  private val secureRandom = SecureRandom()
  private const val ALPHANUM = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

  fun sha256(input: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(input.trim().toByteArray(Charsets.UTF_8))
    return bytes.joinToString("") { "%02x".format(it) }
  }

  fun generateKeyPart(length: Int = 4): String {
    val sb = StringBuilder(length)
    for (i in 0 until length) {
      sb.append(ALPHANUM[secureRandom.nextInt(ALPHANUM.length)])
    }
    return sb.toString()
  }

  fun generateVipKey(): String {
    return "ROHIT-VIP-${generateKeyPart(4)}-${generateKeyPart(4)}-${generateKeyPart(4)}-${generateKeyPart(4)}"
  }

  @SuppressLint("HardwareIds")
  fun getDeviceId(context: Context): String {
    return try {
      val androidId = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ANDROID_ID
      ) ?: "unknown_android_id"
      val hardwareFingerprint = "${Build.MANUFACTURER}_${Build.MODEL}_${Build.BOARD}_${Build.HARDWARE}"
      sha256("$androidId::$hardwareFingerprint").take(16).uppercase()
    } catch (_: Exception) {
      "ROHIT-DEV-${generateKeyPart(8)}"
    }
  }
}
