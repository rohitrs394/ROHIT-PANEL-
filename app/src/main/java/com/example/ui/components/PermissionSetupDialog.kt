package com.example.ui.components

import android.Manifest
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.security.PermissionManager
import com.example.security.SessionManager
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionSetupDialog(
  onDismiss: () -> Unit,
  onPermissionGranted: () -> Unit
) {
  val context = LocalContext.current
  val sessionManager = remember { SessionManager(context) }
  val isAndroid11 = remember { PermissionManager.isAndroid11OrAbove() }

  var hasStoragePermission by remember {
    mutableStateOf(PermissionManager.hasStoragePermission(context))
  }
  var isWirelessPaired by remember {
    mutableStateOf(sessionManager.isWirelessDebuggingPaired || PermissionManager.hasStoragePermission(context))
  }
  var isWifiConnected by remember {
    mutableStateOf(PermissionManager.isWifiConnected(context))
  }
  var statusMessage by remember { mutableStateOf<String?>(null) }

  val rgbBrush = rememberRgbBrush(enabled = true)

  // Android 5 - 10 storage permission launcher
  val storagePermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { perms ->
    val readGranted = perms[Manifest.permission.READ_EXTERNAL_STORAGE] == true
    val writeGranted = perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true
    if (readGranted || writeGranted) {
      hasStoragePermission = true
      Toast.makeText(context, "Storage permission granted!", Toast.LENGTH_SHORT).show()
      onPermissionGranted()
    } else {
      Toast.makeText(context, "Storage permission denied.", Toast.LENGTH_SHORT).show()
    }
  }

  // Periodic refresh when user returns from system settings
  LaunchedEffect(Unit) {
    hasStoragePermission = PermissionManager.hasStoragePermission(context)
    isWifiConnected = PermissionManager.isWifiConnected(context)
  }

  BasicAlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp)
        .clip(RoundedCornerShape(20.dp))
        .border(1.5.dp, rgbBrush, RoundedCornerShape(20.dp)),
      color = CyberSurface.copy(alpha = 0.98f),
      shape = RoundedCornerShape(20.dp)
    ) {
      Column(
        modifier = Modifier
          .padding(20.dp)
          .verticalScroll(rememberScrollState())
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0x2200FFE0)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "SYSTEM PRIVILEGES",
                color = TextWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
              Text(
                text = "OS: Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
                color = NeonCyan,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Android 5-10 Section
        if (!isAndroid11) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFF0C101D))
              .border(1.dp, Color(0x3300FFE0), RoundedCornerShape(12.dp))
              .padding(14.dp)
          ) {
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Folder,
                  contentDescription = null,
                  tint = NeonCyan,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "STORAGE PERMISSION (ANDROID 5-10)",
                  color = TextWhite,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
              }
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Requires Read & Write External Storage to patch assets and apply configurations.",
                color = TextGray,
                fontSize = 11.sp
              )
              Spacer(modifier = Modifier.height(12.dp))

              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(44.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(Brush.horizontalGradient(listOf(NeonCyan, NeonBlue)))
                  .clickable {
                    storagePermissionLauncher.launch(
                      arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                      )
                    )
                  },
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = if (hasStoragePermission) "PERMISSION GRANTED" else "GRANT STORAGE PERMISSION",
                  color = CyberBlack,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }
        } else {
          // Android 11+ Section (All Files Access + Wireless Debugging / Shizuku)
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFF0C101D))
              .border(
                1.dp,
                if (hasStoragePermission) Color(0x4400FF66) else Color(0x3300FFE0),
                RoundedCornerShape(12.dp)
              )
              .padding(14.dp)
          ) {
            Column {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = if (hasStoragePermission) NeonGreen else NeonCyan,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "ALL FILES ACCESS",
                    color = TextWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                  )
                }

                Text(
                  text = if (hasStoragePermission) "GRANTED" else "REQUIRED",
                  color = if (hasStoragePermission) NeonGreen else NeonYellow,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
              }

              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Android 11+ scoped storage requires All Files Access (MANAGE_EXTERNAL_STORAGE) to extract patch files.",
                color = TextGray,
                fontSize = 11.sp
              )
              Spacer(modifier = Modifier.height(10.dp))

              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(42.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(
                    if (hasStoragePermission) SolidColor(Color(0xFF132B1C))
                    else Brush.horizontalGradient(listOf(NeonCyan, NeonBlue))
                  )
                  .clickable {
                    PermissionManager.openStorageSettings(context)
                  },
                contentAlignment = Alignment.Center
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = if (hasStoragePermission) Icons.Default.CheckCircle else Icons.Default.Launch,
                    contentDescription = null,
                    tint = if (hasStoragePermission) NeonGreen else CyberBlack,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = if (hasStoragePermission) "ALL FILES ACCESS ACTIVE" else "GRANT ALL FILES ACCESS",
                    color = if (hasStoragePermission) NeonGreen else CyberBlack,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Shizuku Automatic Privilege & Elevated Storage Section
          val isShizukuPresent = remember { PermissionManager.isShizukuInstalled(context) }
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFF0C101D))
              .border(
                1.dp,
                if (hasStoragePermission) Color(0x4400FF66) else Color(0x44FF1E56),
                RoundedCornerShape(12.dp)
              )
              .padding(14.dp)
          ) {
            Column {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.DeveloperMode,
                    contentDescription = null,
                    tint = NeonCrimson,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "SHIZUKU AUTO FILES ACCESS",
                    color = TextWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                  )
                }

                // Shizuku Status Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = if (hasStoragePermission) Icons.Default.CheckCircle else Icons.Default.Security,
                    contentDescription = null,
                    tint = if (hasStoragePermission) NeonGreen else NeonGold,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = if (hasStoragePermission) "AUTO-GRANTED" else if (isShizukuPresent) "SHIZUKU READY" else "NEEDS SETUP",
                    color = if (hasStoragePermission) NeonGreen else NeonGold,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                  )
                }
              }

              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Run Shizuku, then tap 'AUTO-GRANT FILES ACCESS'. Elevated storage permissions will be granted automatically without any pairing codes.",
                color = TextGray,
                fontSize = 10.sp
              )

              Spacer(modifier = Modifier.height(10.dp))

              // Action Buttons: Run Shizuku & Auto-Grant
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1A1F30))
                    .border(1.dp, Color(0x44FFB800), RoundedCornerShape(8.dp))
                    .clickable {
                      PermissionManager.openShizukuApp(context)
                    },
                  contentAlignment = Alignment.Center
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.Launch,
                      contentDescription = null,
                      tint = NeonGold,
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "RUN SHIZUKU",
                      color = NeonGold,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      fontFamily = FontFamily.Monospace
                    )
                  }
                }

                Box(
                  modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1A1F30))
                    .border(1.dp, Color(0x44FF1E56), RoundedCornerShape(8.dp))
                    .clickable {
                      PermissionManager.openDeveloperSettings(context)
                    },
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = "DEV SETTINGS",
                    color = NeonCrimson,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                  )
                }
              }

              Spacer(modifier = Modifier.height(12.dp))

              // Automated Files Permission Grant Button
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(44.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(
                    if (hasStoragePermission) SolidColor(Color(0xFF132B1C))
                    else Brush.horizontalGradient(listOf(NeonCrimson, NeonGold))
                  )
                  .clickable {
                    val result = PermissionManager.autoGrantFilesPermission(context)
                    hasStoragePermission = PermissionManager.hasStoragePermission(context)
                    isWirelessPaired = hasStoragePermission
                    statusMessage = result.message
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                    if (hasStoragePermission) {
                      onPermissionGranted()
                    }
                  },
                contentAlignment = Alignment.Center
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = if (hasStoragePermission) Icons.Default.CheckCircle else Icons.Default.Folder,
                    contentDescription = null,
                    tint = if (hasStoragePermission) NeonGreen else Color.White,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = if (hasStoragePermission) "FILES ACCESS: GRANTED ✅" else "⚡ AUTO-GRANT FILES ACCESS",
                    color = if (hasStoragePermission) NeonGreen else Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                  )
                }
              }

              if (statusMessage != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = statusMessage ?: "",
                  color = if (hasStoragePermission) NeonGreen else NeonGold,
                  fontSize = 10.sp
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Done / Continue Button
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(rgbBrush)
            .clickable {
              hasStoragePermission = PermissionManager.hasStoragePermission(context)
              onDismiss()
            },
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = if (hasStoragePermission || isWirelessPaired) "CONTINUE TO VIP PANEL" else "SKIP FOR NOW",
            color = CyberBlack,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }
  }
}
