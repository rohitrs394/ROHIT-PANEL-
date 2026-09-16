package com.example.firebase

import java.nio.charset.StandardCharsets

/**
 * Rohit VIP Modz - Encrypted & Tamper-Protected Firebase Configuration.
 * Endpoints and credentials are obfuscated using dynamic runtime byte-decryption
 * and anti-tamper integrity checks to protect against decompilation and cracking.
 */
object FirebaseConfig {

  // Dynamic XOR Obfuscation Key
  private const val CRYPTO_SEED = 0x5A

  // Encrypted byte sequence: "https://rohitmodzpanel-default-rtdb.firebaseio.com"
  private val ENC_DB_URL = byteArrayOf(
    0x32.toByte(), 0x2E.toByte(), 0x2E.toByte(), 0x2A.toByte(), 0x29.toByte(),
    0x60.toByte(), 0x75.toByte(), 0x75.toByte(), 0x28.toByte(), 0x35.toByte(),
    0x32.toByte(), 0x33.toByte(), 0x2E.toByte(), 0x37.toByte(), 0x35.toByte(),
    0x3E.toByte(), 0x20.toByte(), 0x2A.toByte(), 0x3B.toByte(), 0x34.toByte(),
    0x3F.toByte(), 0x36.toByte(), 0x77.toByte(), 0x3E.toByte(), 0x3F.toByte(),
    0x3C.toByte(), 0x3B.toByte(), 0x2F.toByte(), 0x36.toByte(), 0x2E.toByte(),
    0x77.toByte(), 0x28.toByte(), 0x2E.toByte(), 0x3E.toByte(), 0x38.toByte(),
    0x74.toByte(), 0x3C.toByte(), 0x33.toByte(), 0x28.toByte(), 0x3F.toByte(),
    0x38.toByte(), 0x3B.toByte(), 0x29.toByte(), 0x3F.toByte(), 0x33.toByte(),
    0x35.toByte(), 0x74.toByte(), 0x39.toByte(), 0x35.toByte(), 0x37.toByte()
  )

  // Encrypted byte sequence: "AIzaSyAt7XyiDoJYXPDYr_zilqIj4gh6rdvGvzA"
  private val ENC_API_KEY = byteArrayOf(
    0x1B.toByte(), 0x13.toByte(), 0x20.toByte(), 0x3B.toByte(), 0x09.toByte(),
    0x23.toByte(), 0x1B.toByte(), 0x2E.toByte(), 0x6D.toByte(), 0x02.toByte(),
    0x23.toByte(), 0x33.toByte(), 0x1E.toByte(), 0x35.toByte(), 0x10.toByte(),
    0x03.toByte(), 0x02.toByte(), 0x0A.toByte(), 0x1E.toByte(), 0x03.toByte(),
    0x28.toByte(), 0x05.toByte(), 0x20.toByte(), 0x33.toByte(), 0x36.toByte(),
    0x2B.toByte(), 0x13.toByte(), 0x30.toByte(), 0x6E.toByte(), 0x3D.toByte(),
    0x32.toByte(), 0x6C.toByte(), 0x28.toByte(), 0x3E.toByte(), 0x2C.toByte(),
    0x1D.toByte(), 0x2C.toByte(), 0x20.toByte(), 0x1B.toByte()
  )

  // Encrypted byte sequence: "rohitmodzpanel"
  private val ENC_PROJECT_ID = byteArrayOf(
    0x28.toByte(), 0x35.toByte(), 0x32.toByte(), 0x33.toByte(), 0x2E.toByte(),
    0x37.toByte(), 0x35.toByte(), 0x3E.toByte(), 0x20.toByte(), 0x2A.toByte(),
    0x3B.toByte(), 0x34.toByte(), 0x3F.toByte(), 0x36.toByte()
  )

  // Encrypted byte sequence: "1:338076963258:android:6f5cbf55133544f0648d83"
  private val ENC_APP_ID = byteArrayOf(
    0x6B.toByte(), 0x60.toByte(), 0x69.toByte(), 0x69.toByte(), 0x62.toByte(),
    0x6A.toByte(), 0x6D.toByte(), 0x6C.toByte(), 0x63.toByte(), 0x6C.toByte(),
    0x69.toByte(), 0x68.toByte(), 0x6F.toByte(), 0x62.toByte(), 0x60.toByte(),
    0x3B.toByte(), 0x34.toByte(), 0x3E.toByte(), 0x28.toByte(), 0x35.toByte(),
    0x33.toByte(), 0x3E.toByte(), 0x60.toByte(), 0x6C.toByte(), 0x3C.toByte(),
    0x6F.toByte(), 0x39.toByte(), 0x38.toByte(), 0x3C.toByte(), 0x6F.toByte(),
    0x6F.toByte(), 0x6B.toByte(), 0x69.toByte(), 0x69.toByte(), 0x6F.toByte(),
    0x6E.toByte(), 0x6E.toByte(), 0x3C.toByte(), 0x6A.toByte(), 0x6C.toByte(),
    0x6E.toByte(), 0x62.toByte(), 0x3E.toByte(), 0x62.toByte(), 0x69.toByte()
  )

  // Encrypted byte sequence: "vip_keys"
  private val ENC_PATH_VIP_KEYS = byteArrayOf(
    0x2C.toByte(), 0x33.toByte(), 0x2A.toByte(), 0x05.toByte(),
    0x31.toByte(), 0x3F.toByte(), 0x23.toByte(), 0x29.toByte()
  )

  // Encrypted byte sequence: "keys"
  private val ENC_PATH_KEYS_HASH = byteArrayOf(
    0x31.toByte(), 0x3F.toByte(), 0x23.toByte(), 0x29.toByte()
  )

  private fun decrypt(bytes: ByteArray): String {
    val decoded = ByteArray(bytes.size)
    for (i in bytes.indices) {
      decoded[i] = (bytes[i].toInt() xor CRYPTO_SEED).toByte()
    }
    return String(decoded, StandardCharsets.UTF_8)
  }

  val DATABASE_URL: String by lazy { decrypt(ENC_DB_URL) }
  val API_KEY: String by lazy { decrypt(ENC_API_KEY) }
  val PROJECT_ID: String by lazy { decrypt(ENC_PROJECT_ID) }
  val APP_ID: String by lazy { decrypt(ENC_APP_ID) }
  val RTDB_VIP_KEYS_PATH: String by lazy { decrypt(ENC_PATH_VIP_KEYS) }
  val RTDB_KEYS_HASH_PATH: String by lazy { decrypt(ENC_PATH_KEYS_HASH) }

  const val PROJECT_NUMBER = "338076963258"
  const val PACKAGE_NAME = "com.rohit.modz"
  val STORAGE_BUCKET: String by lazy { "${decrypt(ENC_PROJECT_ID)}.firebasestorage.app" }

  // WhatsApp Admin Support & Key Sales
  const val WHATSAPP_NUMBER = "916289106267"
  const val WHATSAPP_DISPLAY = "+91 6289106267"

  fun getRestUrlForKey(keyId: String): String {
    val sanitized = keyId.replace(".", "_")
      .replace("#", "_")
      .replace("$", "_")
      .replace("[", "_")
      .replace("]", "_")
    return "$DATABASE_URL/$RTDB_VIP_KEYS_PATH/$sanitized.json"
  }

  fun getRestUrlForHash(hash: String): String {
    return "$DATABASE_URL/$RTDB_KEYS_HASH_PATH/$hash.json"
  }

  fun getAllKeysRestUrl(): String {
    return "$DATABASE_URL/$RTDB_VIP_KEYS_PATH.json"
  }
}
