package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.extraction.ZipAssetExtractor
import com.example.security.SessionManager
import com.example.service.FloatingOverlayService
import com.example.ui.components.CyberButton
import com.example.ui.components.CyberParticles
import com.example.ui.components.GlassCard
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SettingsScreen(
  onNavigateBack: () -> Unit,
  onLogoutCompleted: () -> Unit
) {
  val context = LocalContext.current
  val sessionManager = remember { SessionManager(context) }

  var rgbEffects by remember { mutableStateOf(sessionManager.rgbEffectsEnabled) }
  var floatingMenu by remember { mutableStateOf(sessionManager.floatingMenuEnabled) }
  var showLogoutConfirm by remember { mutableStateOf(false) }

  fun performLogout() {
    // 1. Stop Foreground Overlay Service & Remove Overlay
    val stopIntent = Intent(context, FloatingOverlayService::class.java).apply {
      action = FloatingOverlayService.ACTION_STOP_OVERLAY
    }
    context.startService(stopIntent)

    // 2. Clear Session
    sessionManager.clearSession()

    // 3. Firebase Auth sign out
    try {
      FirebaseAuth.getInstance().signOut()
    } catch (_: Exception) {}

    Toast.makeText(context, "LOGGED OUT FROM VIP PANEL", Toast.LENGTH_SHORT).show()
    onLogoutCompleted()
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(CyberBlack)
  ) {
    CyberParticles(particleCount = 15)

    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onNavigateBack,
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(Color(0xFF131A2E))
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = NeonCyan
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
          Text(
            text = "VIP SETTINGS",
            color = TextWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            fontFamily = FontFamily.Monospace
          )
          Text(
            text = "SYSTEM PREFERENCES & SECURITY",
            color = NeonCyan,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Preferences Section
      Text(
        text = "INTERFACE & CONTROLS",
        color = NeonCyan,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        fontFamily = FontFamily.Monospace
      )

      Spacer(modifier = Modifier.height(10.dp))

      GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
          // RGB Effects
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.ColorLens,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Animated RGB Effects",
                  color = TextWhite,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Dynamic neon gradients and lighting",
                  color = TextGray,
                  fontSize = 11.sp
                )
              }
            }

            Switch(
              checked = rgbEffects,
              onCheckedChange = {
                rgbEffects = it
                sessionManager.rgbEffectsEnabled = it
              },
              colors = SwitchDefaults.colors(
                checkedThumbColor = CyberBlack,
                checkedTrackColor = NeonCyan,
                uncheckedThumbColor = TextGray,
                uncheckedTrackColor = Color(0xFF161F38)
              )
            )
          }

          // Floating Menu Enabled
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Layers,
                contentDescription = null,
                tint = NeonMagenta,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Floating Overlay Menu",
                  color = TextWhite,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Display ROHIT MODZ VIP badge",
                  color = TextGray,
                  fontSize = 11.sp
                )
              }
            }

            Switch(
              checked = floatingMenu,
              onCheckedChange = {
                floatingMenu = it
                sessionManager.floatingMenuEnabled = it
              },
              colors = SwitchDefaults.colors(
                checkedThumbColor = CyberBlack,
                checkedTrackColor = NeonMagenta,
                uncheckedThumbColor = TextGray,
                uncheckedTrackColor = Color(0xFF161F38)
              )
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Asset & Storage Section
      Text(
        text = "ASSET STORAGE MANAGEMENT",
        color = NeonCyan,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        fontFamily = FontFamily.Monospace
      )

      Spacer(modifier = Modifier.height(10.dp))

      GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
          val manifestCount = sessionManager.getExtractedFilesManifest().size

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Tracked VIP Assets",
                color = TextWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "$manifestCount tracked extraction file(s)",
                color = if (manifestCount > 0) NeonGreen else TextGray,
                fontSize = 11.sp
              )
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF2A101C))
                .border(1.dp, NeonRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .clickable {
                  val deleted = ZipAssetExtractor.cleanupExtractedFiles(context)
                  Toast.makeText(context, "CLEANED $deleted FILE(S)", Toast.LENGTH_SHORT).show()
                }
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(
                text = "PURGE",
                color = NeonRed,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Security & System Information
      Text(
        text = "APP & ARCHITECTURE",
        color = NeonCyan,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        fontFamily = FontFamily.Monospace
      )

      Spacer(modifier = Modifier.height(10.dp))

      GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(text = "App Build", color = TextGray, fontSize = 12.sp)
            Text(text = "ROHIT VIP PANEL v1.0.0", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(text = "Target API", color = TextGray, fontSize = 12.sp)
            Text(text = "Android 14+ (SDK 36)", color = TextWhite, fontSize = 12.sp)
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(text = "Security Layer", color = TextGray, fontSize = 12.sp)
            Text(text = "SHA-256 + Cloud Verification", color = NeonCyan, fontSize = 12.sp)
          }
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      // Logout Button
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0xFF2E0D18))
          .border(1.dp, NeonRed.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
          .clickable { showLogoutConfirm = true },
        contentAlignment = Alignment.Center
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
            contentDescription = null,
            tint = NeonRed,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "LOGOUT OF VIP PANEL",
            color = NeonRed,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            fontFamily = FontFamily.Monospace
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))
    }

    // Confirmation Dialog
    if (showLogoutConfirm) {
      AlertDialog(
        onDismissRequest = { showLogoutConfirm = false },
        title = {
          Text(
            text = "CONFIRM LOGOUT",
            color = NeonRed,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        },
        text = {
          Text(
            text = "Logging out will stop the floating overlay, terminate background services, clean session state, and return to the login screen. Continue?",
            color = TextWhite,
            fontSize = 13.sp
          )
        },
        confirmButton = {
          TextButton(
            onClick = {
              showLogoutConfirm = false
              performLogout()
            }
          ) {
            Text(
              text = "LOGOUT",
              color = NeonRed,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }
        },
        dismissButton = {
          TextButton(onClick = { showLogoutConfirm = false }) {
            Text(text = "CANCEL", color = TextGray)
          }
        },
        containerColor = CyberSurface,
        shape = RoundedCornerShape(16.dp)
      )
    }
  }
}
