package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.StopScreenShare
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.extraction.ZipAssetExtractor
import com.example.security.SessionManager
import com.example.ui.components.CyberStatusBadge
import com.example.ui.components.StatusType
import com.example.ui.theme.CrimsonMetallic3D
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.GoldMetallic3D
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
import com.example.ui.theme.RgbGradientColors
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

class FloatingOverlayService : Service() {

  companion object {
    const val ACTION_START_OVERLAY = "com.example.action.START_OVERLAY"
    const val ACTION_STOP_OVERLAY = "com.example.action.STOP_OVERLAY"
    private const val NOTIFICATION_ID = 9911
    private const val CHANNEL_ID = "rohit_vip_service_channel"

    val isServiceRunningFlow = MutableStateFlow(false)
    val isModActiveFlow = MutableStateFlow(false)
  }

  private lateinit var windowManager: WindowManager
  private var overlayView: View? = null
  private lateinit var layoutParams: WindowManager.LayoutParams
  private lateinit var lifecycleOwner: OverlayLifecycleOwner

  private val serviceJob = Job()
  private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

  private val isExpandedFlow = MutableStateFlow(false)
  private val extractionStateFlow = MutableStateFlow(ZipAssetExtractor.ExtractionProgress())
  private val isExtractingFlow = MutableStateFlow(false)

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onCreate() {
    super.onCreate()
    isModActiveFlow.value = SessionManager(this).isModActive
    createNotificationChannel()
    startForeground(NOTIFICATION_ID, buildNotification("ROHIT VIP MODZ ACTIVE"))

    windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    lifecycleOwner = OverlayLifecycleOwner()
    lifecycleOwner.onCreate()

    SessionManager(this).isServiceRunning = true
    isServiceRunningFlow.value = true

    setupFloatingOverlay()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    when (intent?.action) {
      ACTION_STOP_OVERLAY -> {
        stopOverlayService()
        return START_NOT_STICKY
      }
      else -> {
        // Ensure running
      }
    }
    return START_STICKY
  }

  @SuppressLint("ClickableViewAccessibility")
  private fun setupFloatingOverlay() {
    layoutParams = WindowManager.LayoutParams(
      WindowManager.LayoutParams.WRAP_CONTENT,
      WindowManager.LayoutParams.WRAP_CONTENT,
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
      } else {
        @Suppress("DEPRECATION")
        WindowManager.LayoutParams.TYPE_PHONE
      },
      WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
          WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
      PixelFormat.TRANSLUCENT
    ).apply {
      gravity = Gravity.TOP or Gravity.START
      x = 50
      y = 200
    }

    val composeView = ComposeView(this).apply {
      setViewTreeLifecycleOwner(lifecycleOwner)
      setViewTreeViewModelStoreOwner(lifecycleOwner)
      setViewTreeSavedStateRegistryOwner(lifecycleOwner)

      setContent {
        val isExpanded by isExpandedFlow.collectAsState()
        val extractionProgress by extractionStateFlow.collectAsState()
        val isExtracting by isExtractingFlow.collectAsState()
        val isModActive by isModActiveFlow.collectAsState()

        FloatingOverlayContent(
          isExpanded = isExpanded,
          extractionProgress = extractionProgress,
          isExtracting = isExtracting,
          isModActive = isModActive,
          onToggleExpand = { toggleExpanded() },
          onEnableMod = { handleEnableMod() },
          onDisableMod = { handleDisableMod() },
          onStopService = { stopOverlayService() }
        )
      }
    }

    // Touch & Drag Handling on the floating view
    var initialX = 0
    var initialY = 0
    var initialTouchX = 0f
    var initialTouchY = 0f
    var isDragging = false

    composeView.setOnTouchListener { _, event ->
      when (event.action) {
        MotionEvent.ACTION_DOWN -> {
          initialX = layoutParams.x
          initialY = layoutParams.y
          initialTouchX = event.rawX
          initialTouchY = event.rawY
          isDragging = false
          false
        }
        MotionEvent.ACTION_MOVE -> {
          val dx = (event.rawX - initialTouchX).toInt()
          val dy = (event.rawY - initialTouchY).toInt()
          if (abs(dx) > 10 || abs(dy) > 10) {
            isDragging = true
            layoutParams.x = initialX + dx
            layoutParams.y = initialY + dy
            try {
              windowManager.updateViewLayout(composeView, layoutParams)
            } catch (_: Exception) {}
            true
          } else {
            false
          }
        }
        MotionEvent.ACTION_UP -> {
          if (isDragging) {
            true
          } else {
            false
          }
        }
        else -> false
      }
    }

    overlayView = composeView
    try {
      windowManager.addView(overlayView, layoutParams)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  private fun toggleExpanded() {
    val newExpanded = !isExpandedFlow.value
    isExpandedFlow.value = newExpanded

    // If expanded, update window layout flags if needed to allow touch
    try {
      if (newExpanded) {
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
      } else {
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
      }
      overlayView?.let { windowManager.updateViewLayout(it, layoutParams) }
    } catch (_: Exception) {}
  }

  private fun handleEnableMod() {
    if (isExtractingFlow.value) return
    isExtractingFlow.value = true

    serviceScope.launch {
      val result = ZipAssetExtractor.extractVipAssets(applicationContext) { progress ->
        extractionStateFlow.value = progress
      }
      isExtractingFlow.value = false
      if (result.isSuccess) {
        SessionManager(applicationContext).isModActive = true
        isModActiveFlow.value = true
        Toast.makeText(
          applicationContext,
          "ROHIT VIP MODZ: ENABLED & ACTIVE!",
          Toast.LENGTH_SHORT
        ).show()
      } else {
        Toast.makeText(
          applicationContext,
          "ENABLE FAILED: ${result.exceptionOrNull()?.localizedMessage}",
          Toast.LENGTH_SHORT
        ).show()
      }
    }
  }

  private fun handleDisableMod() {
    val cleanedCount = ZipAssetExtractor.cleanupExtractedFiles(applicationContext)
    SessionManager(applicationContext).isModActive = false
    isModActiveFlow.value = false
    extractionStateFlow.value = ZipAssetExtractor.ExtractionProgress(
      statusMessage = "VIP MODZ DISABLED - $cleanedCount File(s) Removed",
      isComplete = false
    )
    Toast.makeText(
      applicationContext,
      "ROHIT VIP MODZ: DISABLED & CLEANED",
      Toast.LENGTH_SHORT
    ).show()
  }

  private fun stopOverlayService() {
    // 1. Surgical manifest cleanup
    val cleanedCount = ZipAssetExtractor.cleanupExtractedFiles(applicationContext)

    // 2. Clear state
    SessionManager(this).isServiceRunning = false
    isServiceRunningFlow.value = false

    Toast.makeText(
      applicationContext,
      "ROHIT VIP PANEL OFF - $cleanedCount Asset(s) Cleaned",
      Toast.LENGTH_SHORT
    ).show()

    stopForeground(STOP_FOREGROUND_REMOVE)
    stopSelf()
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        "ROHIT VIP PANEL Service",
        NotificationManager.IMPORTANCE_LOW
      ).apply {
        description = "Keeps the ROHIT VIP floating controls active"
        setShowBadge(false)
      }
      val manager = getSystemService(NotificationManager::class.java)
      manager?.createNotificationChannel(channel)
    }
  }

  private fun buildNotification(statusText: String): Notification {
    val openIntent = Intent(this, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
    }
    val openPendingIntent = PendingIntent.getActivity(
      this, 0, openIntent, PendingIntent.FLAG_IMMUTABLE
    )

    val stopIntent = Intent(this, FloatingOverlayService::class.java).apply {
      action = ACTION_STOP_OVERLAY
    }
    val stopPendingIntent = PendingIntent.getService(
      this, 1, stopIntent, PendingIntent.FLAG_IMMUTABLE
    )

    return NotificationCompat.Builder(this, CHANNEL_ID)
      .setContentTitle("ROHIT VIP PANEL")
      .setContentText(statusText)
      .setSmallIcon(android.R.drawable.ic_menu_compass)
      .setContentIntent(openPendingIntent)
      .addAction(android.R.drawable.ic_menu_close_clear_cancel, "OFF / STOP", stopPendingIntent)
      .setOngoing(true)
      .setPriority(NotificationCompat.PRIORITY_LOW)
      .build()
  }

  override fun onDestroy() {
    super.onDestroy()
    serviceJob.cancel()
    lifecycleOwner.onDestroy()
    overlayView?.let {
      try {
        windowManager.removeView(it)
      } catch (_: Exception) {}
    }
    overlayView = null
    SessionManager(this).isServiceRunning = false
    isServiceRunningFlow.value = false
  }
}

@Composable
fun FloatingOverlayContent(
  isExpanded: Boolean,
  extractionProgress: ZipAssetExtractor.ExtractionProgress,
  isExtracting: Boolean,
  isModActive: Boolean,
  onToggleExpand: () -> Unit,
  onEnableMod: () -> Unit,
  onDisableMod: () -> Unit,
  onStopService: () -> Unit
) {
  val transition = rememberInfiniteTransition(label = "badge_rgb")
  val offset by transition.animateFloat(
    initialValue = 0f,
    targetValue = 1000f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 4000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "badge_offset"
  )

  val rgbBrush = Brush.linearGradient(
    colors = RgbGradientColors,
    start = Offset(offset, offset),
    end = Offset(offset + 400f, offset + 400f)
  )

  Box(
    modifier = Modifier.wrapContentSize(),
    contentAlignment = Alignment.TopStart
  ) {
    if (!isExpanded) {
      // 3D Floating VIP Pill / Badge
      Box(
        modifier = Modifier
          .shadow(16.dp, RoundedCornerShape(26.dp), ambientColor = Color(0xAA000000), spotColor = Color(0xFFFFB800))
          .clip(RoundedCornerShape(26.dp))
          .background(GoldMetallic3D)
          .padding(1.5.dp)
          .clip(RoundedCornerShape(24.dp))
          .background(CyberBlack.copy(alpha = 0.94f))
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onToggleExpand
          )
          .padding(horizontal = 14.dp, vertical = 9.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Box(
            modifier = Modifier
              .size(9.dp)
              .clip(CircleShape)
              .background(if (isModActive) NeonGreen else NeonGold)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "ROHIT VIP",
            color = NeonGold,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.5.sp,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    } else {
      // Expanded 3D VIP Executive Panel
      AnimatedVisibility(
        visible = true,
        enter = scaleIn(spring()) + fadeIn(),
        exit = scaleOut(spring()) + fadeOut()
      ) {
        Surface(
          modifier = Modifier
            .width(280.dp)
            .shadow(24.dp, RoundedCornerShape(20.dp), ambientColor = Color(0xAA000000), spotColor = Color(0xFFFF1E56))
            .clip(RoundedCornerShape(20.dp))
            .border(
              1.2.dp,
              Brush.linearGradient(listOf(Color(0xFFFFDF7A), Color(0xFFFF1E56), Color(0x33000000))),
              RoundedCornerShape(20.dp)
            ),
          color = CyberSurface.copy(alpha = 0.97f),
          shape = RoundedCornerShape(20.dp)
        ) {
          Column(
            modifier = Modifier.padding(16.dp)
          ) {
            // Header
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Security,
                  contentDescription = null,
                  tint = NeonGold,
                  modifier = Modifier.size(19.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "ROHIT VIP",
                  color = TextWhite,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Black,
                  letterSpacing = 1.5.sp,
                  fontFamily = FontFamily.Monospace
                )
              }
              Box(
                modifier = Modifier
                  .size(28.dp)
                  .clip(CircleShape)
                  .background(Color(0x33FFFFFF))
                  .clickable(onClick = onToggleExpand),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Close Menu",
                  tint = TextWhite,
                  modifier = Modifier.size(16.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status Badges Row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              CyberStatusBadge(type = StatusType.ONLINE)
              CyberStatusBadge(type = StatusType.KEY_ACTIVE)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Extraction Status & Mod Mode Card
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF090C15))
                .border(
                  1.dp,
                  if (isModActive) Color(0x6600FF66) else Color(0x33FFDF7A),
                  RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
            ) {
              Column {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "VIP ENGINE STATUS",
                    color = NeonGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Monospace
                  )
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                      modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isModActive) NeonGreen else NeonRed)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                      text = if (isModActive) "ACTIVE" else "OFFLINE",
                      color = if (isModActive) NeonGreen else NeonRed,
                      fontSize = 10.sp,
                      fontWeight = FontWeight.ExtraBold,
                      fontFamily = FontFamily.Monospace
                    )
                  }
                }
                Spacer(modifier = Modifier.height(6.dp))

                if (isExtracting || extractionProgress.filesExtracted > 0) {
                  LinearProgressIndicator(
                    progress = { extractionProgress.percent / 100f },
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(6.dp)
                      .clip(RoundedCornerShape(3.dp)),
                    color = if (isModActive) NeonGreen else NeonGold,
                    trackColor = Color(0xFF1E293B)
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = extractionProgress.statusMessage,
                    color = if (extractionProgress.error != null) NeonRed else TextGray,
                    fontSize = 10.sp,
                    maxLines = 2
                  )
                } else {
                  Text(
                    text = if (isModActive) "VIP assets & engine configuration deployed." else "Tap ENABLE to activate engine, DISABLE to purge.",
                    color = TextGray,
                    fontSize = 10.sp
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: ENABLE & DISABLE with 3D Depth
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              // ENABLE Button
              Box(
                modifier = Modifier
                  .weight(1f)
                  .height(42.dp)
                  .shadow(6.dp, RoundedCornerShape(10.dp))
                  .clip(RoundedCornerShape(10.dp))
                  .background(
                    if (isModActive) Brush.horizontalGradient(listOf(NeonGreen, Color(0xFF00AA50)))
                    else Brush.horizontalGradient(listOf(Color(0xFF00AA50), Color(0xFF075E54)))
                  )
                  .clickable(
                    enabled = !isExtracting,
                    onClick = onEnableMod
                  ),
                contentAlignment = Alignment.Center
              ) {
                if (isExtracting && !isModActive) {
                  CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                  )
                } else {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.CheckCircle,
                      contentDescription = null,
                      tint = Color.White,
                      modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                      text = "ENABLE",
                      color = Color.White,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Black,
                      letterSpacing = 1.sp,
                      fontFamily = FontFamily.Monospace
                    )
                  }
                }
              }

              // DISABLE Button
              Box(
                modifier = Modifier
                  .weight(1f)
                  .height(42.dp)
                  .shadow(6.dp, RoundedCornerShape(10.dp))
                  .clip(RoundedCornerShape(10.dp))
                  .background(
                    Brush.horizontalGradient(listOf(Color(0xFFB00020), Color(0xFF6B0E2A)))
                  )
                  .clickable(
                    enabled = !isExtracting,
                    onClick = onDisableMod
                  ),
                contentAlignment = Alignment.Center
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.StopScreenShare,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                  )
                  Spacer(modifier = Modifier.width(5.dp))
                  Text(
                    text = "DISABLE",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Monospace
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action: STOP / OFF Button
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF220810))
                .border(1.dp, NeonCrimson.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                .clickable(onClick = onStopService),
              contentAlignment = Alignment.Center
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.PowerSettingsNew,
                  contentDescription = null,
                  tint = NeonCrimson,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "STOP SERVICE",
                  color = NeonCrimson,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 1.sp,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }
        }
      }
    }
  }
}
