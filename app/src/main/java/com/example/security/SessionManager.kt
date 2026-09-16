package com.example.security

import android.content.Context
import android.content.SharedPreferences
import com.example.models.KeyStatus
import com.example.models.VipKey

class SessionManager(context: Context) {

  private val prefs: SharedPreferences =
    context.getSharedPreferences("rohit_vip_panel_prefs", Context.MODE_PRIVATE)

  companion object {
    private const val KEY_SAVED_KEY = "saved_vip_key"
    private const val KEY_REMEMBER_SESSION = "remember_session"
    private const val KEY_IS_AUTHENTICATED = "is_authenticated"
    private const val KEY_STATUS = "key_status"
    private const val KEY_EXPIRES_AT = "expires_at"
    private const val KEY_CREATED_AT = "created_at"
    private const val KEY_RGB_EFFECTS = "rgb_effects_enabled"
    private const val KEY_FLOATING_MENU = "floating_menu_enabled"
    private const val KEY_MANIFEST_FILES = "manifest_files"
    private const val KEY_SERVICE_RUNNING = "is_service_running"
    private const val KEY_MOD_ACTIVE = "is_mod_active"
    private const val KEY_WIRELESS_PAIRED = "is_wireless_paired"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_NOTE = "key_note"
  }

  var userName: String
    get() = prefs.getString(KEY_USER_NAME, "") ?: ""
    set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

  var note: String
    get() = prefs.getString(KEY_NOTE, "") ?: ""
    set(value) = prefs.edit().putString(KEY_NOTE, value).apply()

  var isModActive: Boolean
    get() = prefs.getBoolean(KEY_MOD_ACTIVE, false)
    set(value) = prefs.edit().putBoolean(KEY_MOD_ACTIVE, value).apply()

  var isWirelessDebuggingPaired: Boolean
    get() = prefs.getBoolean(KEY_WIRELESS_PAIRED, false)
    set(value) = prefs.edit().putBoolean(KEY_WIRELESS_PAIRED, value).apply()

  var savedKey: String?
    get() = prefs.getString(KEY_SAVED_KEY, null)
    set(value) = prefs.edit().putString(KEY_SAVED_KEY, value).apply()

  var rememberSession: Boolean
    get() = prefs.getBoolean(KEY_REMEMBER_SESSION, true)
    set(value) = prefs.edit().putBoolean(KEY_REMEMBER_SESSION, value).apply()

  var isAuthenticated: Boolean
    get() = prefs.getBoolean(KEY_IS_AUTHENTICATED, false)
    set(value) = prefs.edit().putBoolean(KEY_IS_AUTHENTICATED, value).apply()

  var keyStatus: KeyStatus
    get() {
      val name = prefs.getString(KEY_STATUS, KeyStatus.ACTIVE.name) ?: KeyStatus.ACTIVE.name
      return try {
        KeyStatus.valueOf(name)
      } catch (_: Exception) {
        KeyStatus.ACTIVE
      }
    }
    set(value) = prefs.edit().putString(KEY_STATUS, value.name).apply()

  var expiresAt: Long
    get() = prefs.getLong(KEY_EXPIRES_AT, -1L)
    set(value) = prefs.edit().putLong(KEY_EXPIRES_AT, value).apply()

  var createdAt: Long
    get() = prefs.getLong(KEY_CREATED_AT, 0L)
    set(value) = prefs.edit().putLong(KEY_CREATED_AT, value).apply()

  var rgbEffectsEnabled: Boolean
    get() = prefs.getBoolean(KEY_RGB_EFFECTS, true)
    set(value) = prefs.edit().putBoolean(KEY_RGB_EFFECTS, value).apply()

  var floatingMenuEnabled: Boolean
    get() = prefs.getBoolean(KEY_FLOATING_MENU, true)
    set(value) = prefs.edit().putBoolean(KEY_FLOATING_MENU, value).apply()

  var isServiceRunning: Boolean
    get() = prefs.getBoolean(KEY_SERVICE_RUNNING, false)
    set(value) = prefs.edit().putBoolean(KEY_SERVICE_RUNNING, value).apply()

  fun saveSession(key: VipKey, remember: Boolean) {
    prefs.edit().apply {
      putBoolean(KEY_IS_AUTHENTICATED, true)
      putBoolean(KEY_REMEMBER_SESSION, remember)
      putString(KEY_SAVED_KEY, key.keyId)
      putString(KEY_STATUS, key.effectiveStatus.name)
      putLong(KEY_EXPIRES_AT, key.expiresAt)
      putLong(KEY_CREATED_AT, key.createdAt)
      putString(KEY_USER_NAME, key.userName)
      putString(KEY_NOTE, key.note)
      apply()
    }
  }

  fun getSessionKey(): VipKey? {
    val keyId = savedKey ?: return null
    return VipKey(
      keyId = keyId,
      keyHash = CryptoUtils.sha256(keyId),
      status = keyStatus,
      createdAt = createdAt,
      expiresAt = expiresAt,
      userName = userName,
      note = note
    )
  }

  fun clearSession() {
    prefs.edit().apply {
      putBoolean(KEY_IS_AUTHENTICATED, false)
      if (!rememberSession) {
        remove(KEY_SAVED_KEY)
      }
      remove(KEY_STATUS)
      remove(KEY_EXPIRES_AT)
      remove(KEY_CREATED_AT)
      putBoolean(KEY_SERVICE_RUNNING, false)
      apply()
    }
  }

  fun saveExtractedFilesManifest(filePaths: Set<String>) {
    prefs.edit().putStringSet(KEY_MANIFEST_FILES, filePaths).apply()
  }

  fun getExtractedFilesManifest(): Set<String> {
    return prefs.getStringSet(KEY_MANIFEST_FILES, emptySet()) ?: emptySet()
  }

  fun clearExtractedFilesManifest() {
    prefs.edit().remove(KEY_MANIFEST_FILES).apply()
  }
}
