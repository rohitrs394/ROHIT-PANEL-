package com.example.firebase

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.models.KeyStatus
import com.example.models.KeyValidationResult
import com.example.models.VipKey
import com.example.security.CryptoUtils
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

object FirebaseKeyValidator {

  private const val TAG = "FirebaseKeyValidator"

  private val httpClient = OkHttpClient.Builder()
    .connectTimeout(6, TimeUnit.SECONDS)
    .readTimeout(6, TimeUnit.SECONDS)
    .build()

  /**
   * Validates a VIP Key strictly against Firebase Realtime Database (and Firestore).
   * Automatically binds device ID on successful first verification.
   * Dummy login logic is completely removed: only keys created in Firebase/Admin Panel authenticate.
   */
  suspend fun validateKey(
    rawKey: String,
    context: Context
  ): KeyValidationResult = withContext(Dispatchers.IO) {
    val cleanKey = rawKey.trim().uppercase()

    if (cleanKey.isBlank()) {
      return@withContext KeyValidationResult.InvalidKey("PLEASE ENTER A VIP KEY")
    }

    // Check basic network connectivity
    if (!isNetworkAvailable(context)) {
      return@withContext KeyValidationResult.NetworkError("INTERNET REQUIRED - FIREBASE REALTIME VERIFICATION")
    }

    val keyHash = CryptoUtils.sha256(cleanKey)
    val deviceId = CryptoUtils.getDeviceId(context)

    // 1. First Attempt: Firebase Realtime Database (RTDB) via SDK or REST API
    var vipKey: VipKey? = fetchKeyFromRealtimeDb(cleanKey, keyHash)

    // 2. Second Attempt: If not found in RTDB, try Firestore
    if (vipKey == null) {
      vipKey = fetchKeyFromFirestore(cleanKey, keyHash, context)
    }

    // No dummy fallback - Key must strictly exist in Firebase Database
    if (vipKey == null) {
      return@withContext KeyValidationResult.InvalidKey("INVALID VIP KEY - NOT FOUND IN DATABASE")
    }

    // Status checks
    when (vipKey.status) {
      KeyStatus.REVOKED -> return@withContext KeyValidationResult.Revoked("ACCESS REVOKED BY ADMIN")
      KeyStatus.DISABLED -> return@withContext KeyValidationResult.Disabled("VIP KEY CURRENTLY DISABLED")
      KeyStatus.EXPIRED -> return@withContext KeyValidationResult.Expired("VIP LICENSE HAS EXPIRED")
      KeyStatus.INVALID -> return@withContext KeyValidationResult.InvalidKey("VIP KEY IS MARKED INVALID")
      KeyStatus.ACTIVE -> {}
    }

    // Expiry Check
    if (vipKey.isExpired) {
      updateKeyStatusInRealtimeDb(vipKey.keyId, KeyStatus.EXPIRED)
      return@withContext KeyValidationResult.Expired("VIP LICENSE HAS EXPIRED")
    }

    // Device limit check & binding
    val boundList = vipKey.boundDevices.toMutableList()
    if (boundList.isNotEmpty() && !boundList.contains(deviceId)) {
      if (boundList.size >= vipKey.deviceLimit) {
        return@withContext KeyValidationResult.DeviceLimitExceeded(
          "DEVICE LIMIT EXCEEDED (${vipKey.deviceLimit} MAX ALLOWED)"
        )
      }
    }

    // Auto-bind device if not yet bound
    if (!boundList.contains(deviceId)) {
      boundList.add(deviceId)
      bindDeviceToKey(vipKey.keyId, boundList, context)
    }

    return@withContext KeyValidationResult.Success(
      key = vipKey.copy(boundDevices = boundList)
    )
  }

  /**
   * Fetches key from Firebase Realtime Database.
   * Checks both SDK and REST API.
   */
  private suspend fun fetchKeyFromRealtimeDb(keyId: String, keyHash: String): VipKey? {
    // Try RTDB REST API first for ultra-fast response
    val keyFromRest = fetchKeyViaRest(keyId) ?: fetchKeyViaRest(keyHash)
    if (keyFromRest != null) return keyFromRest

    // Try RTDB Android SDK
    return try {
      val rtdb = FirebaseDatabase.getInstance(FirebaseConfig.DATABASE_URL)
      val cleanId = keyId.replace(".", "_")

      withTimeoutOrNull(4000) {
        suspendCancellableCoroutine<VipKey?> { continuation ->
          val ref = rtdb.reference.child(FirebaseConfig.RTDB_VIP_KEYS_PATH).child(cleanId)
          val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
              if (snapshot.exists()) {
                val statusStr = snapshot.child("status").getValue(String::class.java) ?: "ACTIVE"
                val status = try { KeyStatus.valueOf(statusStr.uppercase()) } catch (_: Exception) { KeyStatus.ACTIVE }
                val expiresAt = snapshot.child("expiresAt").getValue(Long::class.java) ?: -1L
                val createdAt = snapshot.child("createdAt").getValue(Long::class.java) ?: System.currentTimeMillis()
                val deviceLimit = snapshot.child("deviceLimit").getValue(Long::class.java)?.toInt() ?: 1
                val note = snapshot.child("note").getValue(String::class.java) ?: ""

                val boundList = mutableListOf<String>()
                snapshot.child("boundDevices").children.forEach { child ->
                  child.getValue(String::class.java)?.let { boundList.add(it) }
                }

                continuation.resume(
                  VipKey(
                    keyId = keyId,
                    keyHash = keyHash,
                    status = status,
                    createdAt = createdAt,
                    expiresAt = expiresAt,
                    deviceLimit = deviceLimit,
                    boundDevices = boundList,
                    createdBy = "FIREBASE_RTDB",
                    note = note
                  )
                )
              } else {
                continuation.resume(null)
              }
            }

            override fun onCancelled(error: DatabaseError) {
              Log.w(TAG, "RTDB error: ${error.message}")
              continuation.resume(null)
            }
          }
          ref.addListenerForSingleValueEvent(listener)
        }
      }
    } catch (e: Exception) {
      Log.w(TAG, "RTDB SDK fetch exception: ${e.message}")
      null
    }
  }

  /**
   * Direct OkHttp REST API call to Firebase Realtime Database.
   */
  private fun fetchKeyViaRest(pathId: String): VipKey? {
    val url = FirebaseConfig.getRestUrlForKey(pathId)
    return try {
      val request = Request.Builder()
        .url(url)
        .header("Accept", "application/json")
        .get()
        .build()

      val response = httpClient.newCall(request).execute()
      val bodyStr = response.body?.string()?.trim()

      if (response.isSuccessful && !bodyStr.isNullOrEmpty() && bodyStr != "null") {
        val json = JSONObject(bodyStr)
        val statusStr = json.optString("status", "ACTIVE")
        val status = try { KeyStatus.valueOf(statusStr.uppercase()) } catch (_: Exception) { KeyStatus.ACTIVE }
        val expiresAt = json.optLong("expiresAt", -1L)
        val createdAt = json.optLong("createdAt", System.currentTimeMillis())
        val deviceLimit = json.optInt("deviceLimit", 1)
        val note = json.optString("note", "")

        val boundList = mutableListOf<String>()
        val boundArr = json.optJSONArray("boundDevices")
        if (boundArr != null) {
          for (i in 0 until boundArr.length()) {
            boundList.add(boundArr.getString(i))
          }
        }

        VipKey(
          keyId = json.optString("keyId", pathId),
          keyHash = CryptoUtils.sha256(pathId),
          status = status,
          createdAt = createdAt,
          expiresAt = expiresAt,
          deviceLimit = deviceLimit,
          boundDevices = boundList,
          createdBy = "FIREBASE_REST",
          note = note
        )
      } else {
        null
      }
    } catch (e: Exception) {
      Log.d(TAG, "REST fetch exception for $url: ${e.message}")
      null
    }
  }

  /**
   * Fallback to Cloud Firestore
   */
  private suspend fun fetchKeyFromFirestore(keyId: String, keyHash: String, context: Context): VipKey? {
    val isFirebaseInitialized = try {
      FirebaseApp.getApps(context).isNotEmpty()
    } catch (_: Exception) {
      false
    }
    if (!isFirebaseInitialized) return null

    return try {
      val firestore = FirebaseFirestore.getInstance()
      val snapshot = firestore.collection("keys").document(keyHash).get().await()
      if (snapshot.exists()) {
        val statusStr = snapshot.getString("status") ?: "ACTIVE"
        val status = try { KeyStatus.valueOf(statusStr.uppercase()) } catch (_: Exception) { KeyStatus.ACTIVE }
        val expiresAt = snapshot.getLong("expiresAt") ?: -1L
        val createdAt = snapshot.getLong("createdAt") ?: System.currentTimeMillis()
        val deviceLimit = snapshot.getLong("deviceLimit")?.toInt() ?: 1
        val boundDevices = (snapshot.get("boundDevices") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
        val createdBy = snapshot.getString("createdBy") ?: "ADMIN"
        val note = snapshot.getString("note") ?: ""

        VipKey(
          keyId = keyId,
          keyHash = keyHash,
          status = status,
          createdAt = createdAt,
          expiresAt = expiresAt,
          deviceLimit = deviceLimit,
          boundDevices = boundDevices,
          createdBy = createdBy,
          note = note
        )
      } else {
        null
      }
    } catch (e: Exception) {
      Log.d(TAG, "Firestore fetch exception: ${e.message}")
      null
    }
  }

  /**
   * Updates bound devices list in Firebase Realtime Database and Firestore.
   */
  private fun bindDeviceToKey(keyId: String, boundList: List<String>, context: Context) {
    // 1. RTDB REST write
    try {
      val cleanId = keyId.replace(".", "_")
      val jsonArray = JSONArray(boundList)
      val jsonBody = jsonArray.toString().toRequestBody("application/json".toMediaType())
      val url = "${FirebaseConfig.DATABASE_URL}/${FirebaseConfig.RTDB_VIP_KEYS_PATH}/$cleanId/boundDevices.json"

      val request = Request.Builder()
        .url(url)
        .put(jsonBody)
        .build()

      httpClient.newCall(request).execute().close()
    } catch (e: Exception) {
      Log.w(TAG, "REST device binding error: ${e.message}")
    }

    // 2. RTDB SDK write
    try {
      val rtdb = FirebaseDatabase.getInstance(FirebaseConfig.DATABASE_URL)
      val cleanId = keyId.replace(".", "_")
      rtdb.reference.child(FirebaseConfig.RTDB_VIP_KEYS_PATH).child(cleanId).child("boundDevices").setValue(boundList)
    } catch (_: Exception) {}

    // 3. Firestore write
    try {
      val keyHash = CryptoUtils.sha256(keyId)
      FirebaseFirestore.getInstance().collection("keys").document(keyHash)
        .update("boundDevices", boundList)
    } catch (_: Exception) {}
  }

  private fun updateKeyStatusInRealtimeDb(keyId: String, status: KeyStatus) {
    try {
      val cleanId = keyId.replace(".", "_")
      val jsonBody = "\"${status.name}\"".toRequestBody("application/json".toMediaType())
      val url = "${FirebaseConfig.DATABASE_URL}/${FirebaseConfig.RTDB_VIP_KEYS_PATH}/$cleanId/status.json"

      val request = Request.Builder()
        .url(url)
        .put(jsonBody)
        .build()

      httpClient.newCall(request).execute().close()
    } catch (_: Exception) {}
  }

  fun isNetworkAvailable(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
    val network = cm.activeNetwork ?: return false
    val capabilities = cm.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
  }
}
