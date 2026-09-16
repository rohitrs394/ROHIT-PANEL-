package com.example.models

enum class KeyStatus {
  ACTIVE,
  EXPIRED,
  DISABLED,
  REVOKED,
  INVALID
}

data class VipKey(
  val keyId: String = "",
  val keyHash: String = "",
  val status: KeyStatus = KeyStatus.ACTIVE,
  val createdAt: Long = System.currentTimeMillis(),
  val expiresAt: Long = -1L, // -1 means Lifetime
  val deviceLimit: Int = 1,
  val boundDevices: List<String> = emptyList(),
  val createdBy: String = "ROHIT_ADMIN",
  val note: String = ""
) {
  val isLifetime: Boolean
    get() = expiresAt <= 0L

  val isExpired: Boolean
    get() = !isLifetime && System.currentTimeMillis() > expiresAt

  val effectiveStatus: KeyStatus
    get() = when {
      status == KeyStatus.REVOKED -> KeyStatus.REVOKED
      status == KeyStatus.DISABLED -> KeyStatus.DISABLED
      isExpired -> KeyStatus.EXPIRED
      else -> KeyStatus.ACTIVE
    }

  fun getRemainingTimeFormatted(): String {
    if (isLifetime) return "LIFETIME ACCESS"
    val diff = expiresAt - System.currentTimeMillis()
    if (diff <= 0) return "EXPIRED"

    val days = diff / (1000 * 60 * 60 * 24)
    val hours = (diff / (1000 * 60 * 60)) % 24
    val minutes = (diff / (1000 * 60)) % 60

    return when {
      days > 0 -> "$days Days, $hours Hours remaining"
      hours > 0 -> "$hours Hours, $minutes Minutes remaining"
      else -> "$minutes Minutes remaining"
    }
  }
}

sealed class KeyValidationResult {
  data class Success(val key: VipKey, val message: String = "ACCESS GRANTED") : KeyValidationResult()
  data class InvalidKey(val message: String = "INVALID KEY") : KeyValidationResult()
  data class Expired(val message: String = "KEY EXPIRED") : KeyValidationResult()
  data class Disabled(val message: String = "KEY TEMPORARILY DISABLED") : KeyValidationResult()
  data class Revoked(val message: String = "ACCESS REVOKED BY ADMIN") : KeyValidationResult()
  data class DeviceLimitExceeded(val message: String = "DEVICE LIMIT EXCEEDED") : KeyValidationResult()
  data class NetworkError(val message: String = "NETWORK UNAVAILABLE") : KeyValidationResult()
  data class ServerError(val message: String = "SERVER TEMPORARILY UNAVAILABLE") : KeyValidationResult()
}
