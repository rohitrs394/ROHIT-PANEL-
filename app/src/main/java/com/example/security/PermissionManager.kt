package com.example.security

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat

object PermissionManager {

  data class PairResult(
    val success: Boolean,
    val message: String
  )

  fun isAndroid11OrAbove(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
  }

  fun hasStoragePermission(context: Context): Boolean {
    return if (isAndroid11OrAbove()) {
      Environment.isExternalStorageManager()
    } else {
      val readGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.READ_EXTERNAL_STORAGE
      ) == PackageManager.PERMISSION_GRANTED

      val writeGranted = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
        ContextCompat.checkSelfPermission(
          context,
          Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
      } else true

      readGranted && writeGranted
    }
  }

  fun isWifiConnected(context: Context): Boolean {
    return try {
      val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
      val activeNetwork = cm?.activeNetwork ?: return false
      val caps = cm.getNetworkCapabilities(activeNetwork) ?: return false
      caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    } catch (_: Exception) {
      false
    }
  }

  fun isShizukuInstalled(context: Context): Boolean {
    return try {
      context.packageManager.getPackageInfo("moe.shizuku.privileged.api", 0)
      true
    } catch (_: Exception) {
      false
    }
  }

  fun openStorageSettings(context: Context) {
    try {
      if (isAndroid11OrAbove()) {
        val uri = Uri.parse("package:${context.packageName}")
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
      } else {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
          data = Uri.fromParts("package", context.packageName, null)
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
      }
    } catch (_: Exception) {
      try {
        if (isAndroid11OrAbove()) {
          val fallback = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          }
          context.startActivity(fallback)
        }
      } catch (ex: Exception) {
        Toast.makeText(context, "Unable to open settings: ${ex.localizedMessage}", Toast.LENGTH_SHORT).show()
      }
    }
  }

  fun openDeveloperSettings(context: Context) {
    try {
      val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
    } catch (e: Exception) {
      try {
        val fallback = Intent(Settings.ACTION_SETTINGS).apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(fallback)
      } catch (ex: Exception) {
        Toast.makeText(context, "Could not open Developer Options: ${ex.localizedMessage}", Toast.LENGTH_SHORT).show()
      }
    }
  }

  fun openShizukuApp(context: Context): Boolean {
    return try {
      val intent = context.packageManager.getLaunchIntentForPackage("moe.shizuku.privileged.api")
      if (intent != null) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        true
      } else {
        // Open Play Store or Shizuku page
        val webIntent = Intent(
          Intent.ACTION_VIEW,
          Uri.parse("https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api")
        ).apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(webIntent)
        false
      }
    } catch (_: Exception) {
      false
    }
  }

  fun autoGrantFilesPermission(context: Context): PairResult {
    try {
      // If already granted, return success immediately
      if (hasStoragePermission(context)) {
        val sessionManager = SessionManager(context)
        sessionManager.isWirelessDebuggingPaired = true
        return PairResult(
          success = true,
          message = "ALL FILES PERMISSION IS ACTIVE & VERIFIED!"
        )
      }

      // Try elevated Shizuku shell grant
      var grantedViaShell = false
      try {
        val pkg = context.packageName
        val cmds = arrayOf(
          "appops set $pkg MANAGE_EXTERNAL_STORAGE allow",
          "pm grant $pkg android.permission.MANAGE_EXTERNAL_STORAGE",
          "pm grant $pkg android.permission.READ_EXTERNAL_STORAGE",
          "pm grant $pkg android.permission.WRITE_EXTERNAL_STORAGE"
        )
        for (cmd in cmds) {
          val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", cmd))
          process.waitFor()
        }
        if (hasStoragePermission(context)) {
          grantedViaShell = true
        }
      } catch (_: Exception) {}

      if (grantedViaShell || hasStoragePermission(context)) {
        val sessionManager = SessionManager(context)
        sessionManager.isWirelessDebuggingPaired = true
        return PairResult(
          success = true,
          message = "FILES PERMISSION AUTOMATICALLY GRANTED VIA SHIZUKU!"
        )
      }

      // Seamlessly open direct settings toggle if not auto-granted
      openStorageSettings(context)
      return PairResult(
        success = false,
        message = "Please enable 'Allow access to manage all files' on the screen."
      )
    } catch (e: Exception) {
      openStorageSettings(context)
      return PairResult(
        success = false,
        message = "Opening file permission settings..."
      )
    }
  }
}
