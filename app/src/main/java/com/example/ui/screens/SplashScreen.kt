package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.security.SessionManager
import com.example.ui.components.CyberParticles
import com.example.ui.components.rememberRgbBrush
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
  onNavigateToLogin: () -> Unit,
  onNavigateToDashboard: () -> Unit
) {
  val context = LocalContext.current
  val sessionManager = SessionManager(context)

  val infiniteTransition = rememberInfiniteTransition(label = "splash_anim")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.95f,
    targetValue = 1.05f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse"
  )

  val rgbBrush = rememberRgbBrush(enabled = true)

  LaunchedEffect(Unit) {
    delay(2000)
    if (sessionManager.isAuthenticated && sessionManager.savedKey != null) {
      onNavigateToDashboard()
    } else {
      onNavigateToLogin()
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(CyberBlack),
    contentAlignment = Alignment.Center
  ) {
    CyberParticles(particleCount = 35)

    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.padding(24.dp)
    ) {
      // Glowing VIP Emblem
      Box(
        modifier = Modifier
          .size(110.dp)
          .scale(pulseScale)
          .clip(CircleShape)
          .background(rgbBrush)
          .padding(2.5.dp)
          .clip(CircleShape)
          .background(CyberBlack),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Shield,
          contentDescription = "VIP Shield",
          tint = NeonCyan,
          modifier = Modifier.size(54.dp)
        )
      }

      Spacer(modifier = Modifier.height(28.dp))

      Text(
        text = "ROHIT VIP PANEL",
        color = TextWhite,
        fontSize = 26.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 3.sp,
        fontFamily = FontFamily.Monospace
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "PREMIUM VIP ACCESS CORE",
        color = NeonCyan,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.5.sp,
        fontFamily = FontFamily.Monospace
      )

      Spacer(modifier = Modifier.height(40.dp))

      // Cyber loader bar
      Box(
        modifier = Modifier
          .size(width = 160.dp, height = 3.dp)
          .clip(CircleShape)
          .background(Color(0xFF161F38))
      ) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(rgbBrush)
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = "INITIALIZING SECURE PROTOCOL...",
        color = TextGray,
        fontSize = 10.sp,
        letterSpacing = 1.sp,
        fontFamily = FontFamily.Monospace
      )
    }
  }
}
