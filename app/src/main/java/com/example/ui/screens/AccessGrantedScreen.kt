package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.VipKey
import com.example.ui.components.CyberParticles
import com.example.ui.components.rememberRgbBrush
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.delay

@Composable
fun AccessGrantedScreen(
  vipKey: VipKey,
  onNavigateToDashboard: () -> Unit
) {
  var stage by remember { mutableStateOf(0) }

  val transition = rememberInfiniteTransition(label = "scan_transition")
  val scanY by transition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(1500, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "scan_y"
  )

  val rgbBrush = rememberRgbBrush(enabled = true)

  LaunchedEffect(Unit) {
    delay(500)
    stage = 1 // Show ACCESS GRANTED
    delay(1200)
    stage = 2 // Show WELCOME TO ROHIT VIP PANEL
    delay(1600)
    onNavigateToDashboard()
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(CyberBlack),
    contentAlignment = Alignment.Center
  ) {
    CyberParticles(particleCount = 40)

    // Cyber Scanline Sweep Effect
    Canvas(modifier = Modifier.fillMaxSize()) {
      val yPos = scanY * size.height
      drawLine(
        brush = Brush.horizontalGradient(
          listOf(Color.Transparent, NeonCyan.copy(alpha = 0.6f), Color.Transparent)
        ),
        start = Offset(0f, yPos),
        end = Offset(size.width, yPos),
        strokeWidth = 3f
      )
    }

    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.padding(24.dp)
    ) {
      // Animated Check Icon
      AnimatedVisibility(
        visible = stage >= 1,
        enter = scaleIn(tween(500, easing = FastOutSlowInEasing)) + fadeIn()
      ) {
        Box(
          modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(rgbBrush)
            .padding(2.5.dp)
            .clip(CircleShape)
            .background(CyberBlack),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.VerifiedUser,
            contentDescription = "Access Granted",
            tint = NeonGreen,
            modifier = Modifier.size(52.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      AnimatedVisibility(
        visible = stage >= 1,
        enter = fadeIn(tween(400))
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "ACCESS GRANTED",
            color = NeonGreen,
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 4.sp,
            fontFamily = FontFamily.Monospace
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "VIP PROTOCOL DECRYPTED",
            color = NeonCyan,
            fontSize = 11.sp,
            letterSpacing = 2.sp,
            fontFamily = FontFamily.Monospace
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      AnimatedVisibility(
        visible = stage >= 2,
        enter = fadeIn(tween(600))
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Box(
            modifier = Modifier
              .fillMaxWidth(0.85f)
              .height(1.dp)
              .background(
                Brush.horizontalGradient(
                  listOf(Color.Transparent, NeonCyan, Color.Transparent)
                )
              )
          )
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = "WELCOME TO",
            color = TextGray,
            fontSize = 12.sp,
            letterSpacing = 2.5.sp,
            fontFamily = FontFamily.Monospace
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "ROHIT VIP PANEL",
            color = TextWhite,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 3.sp,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }
  }
}
