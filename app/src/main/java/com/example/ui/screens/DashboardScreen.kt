package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.extraction.ZipAssetExtractor
import com.example.firebase.FirebaseKeyValidator
import com.example.models.KeyStatus
import com.example.models.VipKey
import com.example.security.CryptoUtils
import com.example.security.PermissionManager
import com.example.security.SessionManager
import com.example.service.FloatingOverlayService
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import com.example.ui.components.CyberParticles
import com.example.ui.components.CyberStatusBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.PermissionSetupDialog
import com.example.ui.components.StatusType
import com.example.ui.components.ThreeDButton
import com.example.ui.components.ThreeDCard
import com.example.ui.components.ThreeDCrest
import com.example.ui.components.ThreeDVipCard
import com.example.ui.components.rememberRgbBrush
import com.example.ui.theme.CrimsonGoldGradient
import com.example.ui.theme.CrimsonMetallic3D
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberGlassBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.GoldMetallic3D
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.Titanium3D
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
  vipKey: VipKey,
  onNavigateToSettings: () -> Unit
) {
  val context = LocalContext.current
  val sessionManager = remember { SessionManager(context) }
  val scope = rememberCoroutineScope()

  val isServiceRunning by FloatingOverlayService.isServiceRunningFlow.collectAsState()
  var showPermissionDialog by remember { mutableStateOf(false) }
  var showStoragePermissionDialog by remember {
    mutableStateOf(!PermissionManager.hasStoragePermission(context))
  }
  var isExtractingAssets by remember { mutableStateOf(false) }

  val transition = rememberInfiniteTransition(label = "pulse_anim")
  val pulseScale by transition.animateFloat(
    initialValue = 0.96f,
    targetValue = 1.04f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "btn_pulse"
  )

  val rgbBrush = rememberRgbBrush(enabled = sessionManager.rgbEffectsEnabled)

  fun startOverlayService() {
    if (!Settings.canDrawOverlays(context)) {
      showPermissionDialog = true
      return
    }

    val intent = Intent(context, FloatingOverlayService::class.java).apply {
      action = FloatingOverlayService.ACTION_START_OVERLAY
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      ContextCompat.startForegroundService(context, intent)
    } else {
      context.startService(intent)
    }
    Toast.makeText(context, "ROHIT MODZ VIP OVERLAY ACTIVATED", Toast.LENGTH_SHORT).show()
  }

  fun stopOverlayService() {
    val intent = Intent(context, FloatingOverlayService::class.java).apply {
      action = FloatingOverlayService.ACTION_STOP_OVERLAY
    }
    context.startService(intent)
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(CyberBlack)
  ) {
    CyberParticles(particleCount = 20)

    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Top Navigation / Header Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          ThreeDCrest(size = 42.dp)
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "ROHIT VIP",
              color = TextWhite,
              fontSize = 18.sp,
              fontWeight = FontWeight.Black,
              letterSpacing = 1.5.sp,
              fontFamily = FontFamily.Monospace
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(5.dp)
                  .clip(CircleShape)
                  .background(NeonGold)
              )
              Spacer(modifier = Modifier.width(5.dp))
              Text(
                text = "EXECUTIVE SUITE",
                color = NeonGold,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }

        // 3D Settings Button
        Box(
          modifier = Modifier
            .size(42.dp)
            .shadow(8.dp, CircleShape)
            .clip(CircleShape)
            .background(GoldMetallic3D)
            .padding(1.5.dp)
            .clip(CircleShape)
            .background(CyberBlack)
            .clickable { onNavigateToSettings() },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Settings",
            tint = NeonGold,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Status Indicators Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        CyberStatusBadge(
          type = if (FirebaseKeyValidator.isNetworkAvailable(context)) StatusType.ONLINE else StatusType.OFFLINE
        )
        CyberStatusBadge(
          type = if (vipKey.effectiveStatus == KeyStatus.ACTIVE) StatusType.KEY_ACTIVE else StatusType.KEY_EXPIRED
        )
        CyberStatusBadge(
          type = if (isServiceRunning) StatusType.SERVICE_RUNNING else StatusType.SERVICE_STOPPED
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      // 3D Executive VIP Membership Card
      ThreeDVipCard(
        vipKey = vipKey,
        deviceId = CryptoUtils.getDeviceId(context)
      )

      Spacer(modifier = Modifier.height(18.dp))

      // 3D Storage & Privilege Access Card
      val hasStorage = PermissionManager.hasStoragePermission(context)
      ThreeDCard(
        modifier = Modifier
          .fillMaxWidth()
          .clickable {
            if (!hasStorage) showStoragePermissionDialog = true
          },
        shape = RoundedCornerShape(16.dp),
        elevation = 10.dp
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (hasStorage) NeonGreen.copy(alpha = 0.15f) else NeonCrimson.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = if (hasStorage) NeonGreen else NeonCrimson,
                modifier = Modifier.size(22.dp)
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
              Text(
                text = "STORAGE PRIVILEGES",
                color = TextWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = if (hasStorage) "All Files Access Granted • Shizuku Active" else "Automated Grant via Shizuku Required",
                color = TextGray,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
              )
            }
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(if (hasStorage) Color(0x2200E676) else Color(0x33FF1E56))
              .border(
                1.dp,
                if (hasStorage) NeonGreen.copy(alpha = 0.6f) else NeonCrimson.copy(alpha = 0.6f),
                RoundedCornerShape(8.dp)
              )
              .padding(horizontal = 10.dp, vertical = 5.dp)
          ) {
            Text(
              text = if (hasStorage) "ACTIVE" else "GRANT",
              color = if (hasStorage) NeonGreen else NeonCrimson,
              fontSize = 10.sp,
              fontWeight = FontWeight.ExtraBold,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      // 3D Physical Turbine Ignition Power Switch
      val startInteraction = remember { MutableInteractionSource() }
      val isPressed by startInteraction.collectIsPressedAsState()
      val buttonPushOffset by animateDpAsState(
        targetValue = if (isPressed) 5.dp else 0.dp,
        animationSpec = androidx.compose.animation.core.tween(70),
        label = "turbine_press"
      )

      Box(
        modifier = Modifier
          .size(175.dp),
        contentAlignment = Alignment.Center
      ) {
        // 3D Deep Base Outer Ring
        Box(
          modifier = Modifier
            .size(175.dp)
            .offset(y = 5.dp)
            .shadow(28.dp, CircleShape, ambientColor = Color(0xAA000000), spotColor = Color(0xCC000000))
            .clip(CircleShape)
            .background(if (isServiceRunning) Color(0xFF4A0A1C) else Color(0xFF6B4500))
        )

        // 3D Moving Cap
        Box(
          modifier = Modifier
            .size(170.dp)
            .offset(y = buttonPushOffset)
            .clip(CircleShape)
            .background(
              if (isServiceRunning) CrimsonMetallic3D else GoldMetallic3D
            )
            .drawBehind {
              // Specular upper highlight
              drawLine(
                color = Color(0x77FFFFFF),
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = 3.dp.toPx()
              )
            }
            .padding(5.dp)
            .clip(CircleShape)
            .background(CyberBlack)
            .clickable(
              interactionSource = startInteraction,
              indication = null,
              onClick = {
                if (isServiceRunning) {
                  stopOverlayService()
                } else {
                  startOverlayService()
                }
              }
            ),
          contentAlignment = Alignment.Center
        ) {
          // Inner Turbine Core
          Box(
            modifier = Modifier
              .size(136.dp)
              .clip(CircleShape)
              .background(
                Brush.radialGradient(
                  if (isServiceRunning) {
                    listOf(Color(0xFF8B0D2C), Color(0xFF330510), CyberBlack)
                  } else {
                    listOf(Color(0xFF7A5800), Color(0xFF2A1C00), CyberBlack)
                  }
                )
              )
              .border(
                width = 1.5.dp,
                brush = if (isServiceRunning) CrimsonMetallic3D else GoldMetallic3D,
                shape = CircleShape
              ),
            contentAlignment = Alignment.Center
          ) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Icon(
                imageVector = if (isServiceRunning) Icons.Default.PowerSettingsNew else Icons.Default.PlayArrow,
                contentDescription = if (isServiceRunning) "STOP" else "START",
                tint = if (isServiceRunning) NeonCrimson else NeonGold,
                modifier = Modifier.size(42.dp)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = if (isServiceRunning) "STOP" else "START",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace
              )
              Text(
                text = if (isServiceRunning) "VIP ENGINE ACTIVE" else "LAUNCH VIP ENGINE",
                color = if (isServiceRunning) NeonCrimson else NeonGold,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      // 3D Quick Actions Grid
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Load Assets 3D Action
        ThreeDCard(
          modifier = Modifier
            .weight(1f)
            .clickable(
              enabled = !isExtractingAssets,
              onClick = {
                isExtractingAssets = true
                scope.launch {
                  val result = ZipAssetExtractor.extractVipAssets(context) {}
                  isExtractingAssets = false
                  if (result.isSuccess) {
                    Toast.makeText(context, "VIP Assets Synchronized", Toast.LENGTH_SHORT).show()
                  } else {
                    Toast.makeText(
                      context,
                      "Extraction failed: ${result.exceptionOrNull()?.message}",
                      Toast.LENGTH_SHORT
                    ).show()
                  }
                }
              }
            ),
          shape = RoundedCornerShape(14.dp),
          elevation = 8.dp
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(
              imageVector = Icons.Default.Download,
              contentDescription = null,
              tint = NeonGold,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (isExtractingAssets) "SYNCING..." else "SYNC ASSETS",
              color = TextWhite,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }
        }

        // Clean Cache 3D Action
        ThreeDCard(
          modifier = Modifier
            .weight(1f)
            .clickable {
              val deleted = ZipAssetExtractor.cleanupExtractedFiles(context)
              Toast.makeText(context, "Cache Cleaned ($deleted files)", Toast.LENGTH_SHORT).show()
            },
          shape = RoundedCornerShape(14.dp),
          elevation = 8.dp
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(
              imageVector = Icons.Default.CleaningServices,
              contentDescription = null,
              tint = NeonCrimson,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "CLEAN CACHE",
              color = TextWhite,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 3D Official WhatsApp Support Button
      ThreeDButton(
        text = "OFFICIAL SUPPORT • WHATSAPP",
        onClick = {
          openWhatsAppForVipKey(context, vipKey.keyId)
        },
        primaryBrush = Brush.horizontalGradient(listOf(Color(0xFF25D366), Color(0xFF128C7E))),
        baseColor = Color(0xFF0B5738),
        height = 46.dp
      )

      Spacer(modifier = Modifier.height(24.dp))
    }

    // Permission Explanation Dialog for Display Over Other Apps
    if (showPermissionDialog) {
      AlertDialog(
        onDismissRequest = { showPermissionDialog = false },
        title = {
          Text(
            text = "OVERLAY PERMISSION REQUIRED",
            color = NeonGold,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        },
        text = {
          Text(
            text = "To display the floating VIP control widget over running apps and games, please grant 'Display over other apps' permission in Android Settings.",
            color = TextWhite,
            fontSize = 13.sp
          )
        },
        confirmButton = {
          TextButton(
            onClick = {
              showPermissionDialog = false
              val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
              )
              context.startActivity(intent)
            }
          ) {
            Text(
              text = "OPEN SETTINGS",
              color = NeonGreen,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }
        },
        dismissButton = {
          TextButton(onClick = { showPermissionDialog = false }) {
            Text(text = "CANCEL", color = TextGray)
          }
        },
        containerColor = CyberSurface,
        shape = RoundedCornerShape(16.dp)
      )
    }

    if (showStoragePermissionDialog) {
      PermissionSetupDialog(
        onDismiss = { showStoragePermissionDialog = false },
        onPermissionGranted = {
          showStoragePermissionDialog = false
          Toast.makeText(context, "Privileges & storage permissions updated!", Toast.LENGTH_SHORT).show()
        }
      )
    }
  }
}

@Composable
fun InfoMetricCard(
  title: String,
  value: String,
  subtitle: String,
  icon: ImageVector,
  tint: Color,
  onClick: (() -> Unit)? = null
) {
  val cardModifier = if (onClick != null) {
    Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
  } else {
    Modifier.fillMaxWidth()
  }

  GlassCard(
    modifier = cardModifier,
    shape = RoundedCornerShape(14.dp),
    borderColor = tint.copy(alpha = 0.35f),
    backgroundColor = Color(0xFF0C101D)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(tint.copy(alpha = 0.15f))
          .border(1.dp, tint.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = tint,
          modifier = Modifier.size(22.dp)
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          color = TextGray,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp,
          fontFamily = FontFamily.Monospace
        )
        Text(
          text = value,
          color = TextWhite,
          fontSize = 13.sp,
          fontWeight = FontWeight.ExtraBold,
          fontFamily = FontFamily.Monospace
        )
        Text(
          text = subtitle,
          color = tint.copy(alpha = 0.9f),
          fontSize = 10.sp,
          fontFamily = FontFamily.Monospace
        )
      }
    }
  }
}
