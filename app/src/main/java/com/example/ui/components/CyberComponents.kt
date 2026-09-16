package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.KeyStatus
import com.example.models.VipKey
import com.example.ui.theme.BevelShadow3D
import com.example.ui.theme.CrimsonGoldGradient
import com.example.ui.theme.CrimsonMetallic3D
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberGlassBg
import com.example.ui.theme.CyberGlassBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.GoldMetallic3D
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.Obsidian3DBrush
import com.example.ui.theme.RgbGradientColors
import com.example.ui.theme.SpecularHighlight3D
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import com.example.ui.theme.Titanium3D
import kotlin.random.Random

@Composable
fun rememberRgbBrush(enabled: Boolean = true): Brush {
  if (!enabled) {
    return Brush.linearGradient(listOf(NeonCyan, NeonBlue))
  }
  val transition = rememberInfiniteTransition(label = "rgb_transition")
  val offset by transition.animateFloat(
    initialValue = 0f,
    targetValue = 1000f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 5000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "rgb_offset"
  )

  return Brush.linearGradient(
    colors = RgbGradientColors,
    start = Offset(offset, offset),
    end = Offset(offset + 600f, offset + 600f)
  )
}

fun Modifier.rgbBorder(
  brush: Brush,
  strokeWidth: Dp = 1.5.dp,
  shape: Shape = RoundedCornerShape(12.dp)
): Modifier = this.border(
  width = strokeWidth,
  brush = brush,
  shape = shape
)

fun Modifier.cyberGlass(
  shape: Shape = RoundedCornerShape(16.dp),
  borderColor: Color = CyberGlassBorder,
  backgroundColor: Color = CyberGlassBg
): Modifier = this
  .clip(shape)
  .background(backgroundColor, shape)
  .border(1.dp, borderColor, shape)

@Composable
fun GlassCard(
  modifier: Modifier = Modifier,
  shape: Shape = RoundedCornerShape(16.dp),
  borderColor: Color = CyberGlassBorder,
  backgroundColor: Color = CyberSurface.copy(alpha = 0.75f),
  content: @Composable BoxScope.() -> Unit
) {
  Box(
    modifier = modifier
      .clip(shape)
      .background(backgroundColor)
      .border(1.dp, borderColor, shape)
      .padding(16.dp),
    content = content
  )
}

/**
 * 3D Elevated Card with Specular Top Highlight, Ambient Drop Shadow,
 * and Physical Beveled Underside.
 */
@Composable
fun ThreeDCard(
  modifier: Modifier = Modifier,
  shape: Shape = RoundedCornerShape(18.dp),
  borderBrush: Brush = Brush.verticalGradient(
    listOf(SpecularHighlight3D, Color(0x33FFB800), Color(0x11000000))
  ),
  backgroundBrush: Brush = Obsidian3DBrush,
  elevation: Dp = 14.dp,
  tiltEffect: Boolean = false,
  content: @Composable BoxScope.() -> Unit
) {
  val transition = rememberInfiniteTransition(label = "card_tilt")
  val tiltDegree by if (tiltEffect) {
    transition.animateFloat(
      initialValue = -1.5f,
      targetValue = 1.5f,
      animationSpec = infiniteRepeatable(
        animation = tween(4000, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Reverse
      ),
      label = "tilt_deg"
    )
  } else {
    remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
  }

  Box(
    modifier = modifier
      .graphicsLayer {
        if (tiltEffect) {
          rotationX = tiltDegree
          rotationY = tiltDegree * 0.7f
          cameraDistance = 14f * density
        }
      }
      .shadow(elevation, shape, ambientColor = Color(0x88000000), spotColor = Color(0xAA000000))
      .clip(shape)
      .background(backgroundBrush)
      // Top specular light simulation
      .drawBehind {
        // Specular upper edge line
        drawLine(
          color = Color(0x40FFFFFF),
          start = Offset(0f, 0f),
          end = Offset(size.width, 0f),
          strokeWidth = 2.dp.toPx()
        )
        // Bottom deep shadow edge line
        drawLine(
          color = Color(0x99000000),
          start = Offset(0f, size.height),
          end = Offset(size.width, size.height),
          strokeWidth = 3.dp.toPx()
        )
      }
      .border(1.dp, borderBrush, shape)
      .padding(18.dp),
    content = content
  )
}

/**
 * Real Physical 3D Push Button with Bottom Base & Press-Down Feedback
 */
@Composable
fun ThreeDButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  isLoading: Boolean = false,
  primaryBrush: Brush = CrimsonGoldGradient,
  baseColor: Color = Color(0xFF6B0E2A),
  height: Dp = 52.dp
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  // 3D Push down animation
  val pushOffset by animateDpAsState(
    targetValue = if (isPressed) 3.dp else 0.dp,
    animationSpec = tween(80),
    label = "btn_press_offset"
  )
  val bottomBevelHeight by animateDpAsState(
    targetValue = if (isPressed) 1.dp else 4.dp,
    animationSpec = tween(80),
    label = "bevel_height"
  )

  val shape = RoundedCornerShape(14.dp)

  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(height + 4.dp) // accommodate 3D base
      .padding(horizontal = 2.dp),
    contentAlignment = Alignment.TopCenter
  ) {
    // 3D Bottom Base (the physical extruded depth of the button)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(height)
        .offset(y = 4.dp)
        .clip(shape)
        .background(if (enabled) baseColor else Color(0xFF141824))
    )

    // 3D Top Cap (moves down when pressed)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(height)
        .offset(y = pushOffset)
        .shadow(if (isPressed) 2.dp else 8.dp, shape)
        .clip(shape)
        .background(
          if (enabled) primaryBrush else Brush.horizontalGradient(listOf(Color(0xFF232B3E), Color(0xFF161B26)))
        )
        .drawBehind {
          // Specular bevel highlight on top edge
          drawLine(
            color = Color(0x66FFFFFF),
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 2.dp.toPx()
          )
        }
        .border(
          1.dp,
          if (enabled) Color(0x55FFFFFF) else Color(0x22FFFFFF),
          shape
        )
        .clickable(
          interactionSource = interactionSource,
          indication = null,
          enabled = enabled && !isLoading,
          onClick = onClick
        ),
      contentAlignment = Alignment.Center
    ) {
      if (isLoading) {
        CircularProgressIndicator(
          modifier = Modifier.size(24.dp),
          color = Color.White,
          strokeWidth = 2.5.dp
        )
      } else {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Text(
            text = text,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }
  }
}

/**
 * 3D Floating Official VIP Titanium Crest with Perspective Tilt
 */
@Composable
fun ThreeDCrest(
  modifier: Modifier = Modifier,
  size: Dp = 80.dp
) {
  val transition = rememberInfiniteTransition(label = "crest_3d")
  val rotY by transition.animateFloat(
    initialValue = -8f,
    targetValue = 8f,
    animationSpec = infiniteRepeatable(
      animation = tween(3500, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "rot_y"
  )
  val rotX by transition.animateFloat(
    initialValue = 4f,
    targetValue = -4f,
    animationSpec = infiniteRepeatable(
      animation = tween(2800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "rot_x"
  )

  Box(
    modifier = modifier
      .size(size)
      .graphicsLayer {
        rotationX = rotX
        rotationY = rotY
        cameraDistance = 14f * density
      },
    contentAlignment = Alignment.Center
  ) {
    // Ambient 3D Backlight Glow
    Box(
      modifier = Modifier
        .size(size * 0.95f)
        .clip(CircleShape)
        .background(
          Brush.radialGradient(
            listOf(NeonCrimson.copy(alpha = 0.35f), NeonGold.copy(alpha = 0.15f), Color.Transparent)
          )
        )
    )

    // Outer 3D Ring
    Box(
      modifier = Modifier
        .size(size)
        .shadow(16.dp, CircleShape, ambientColor = NeonGold, spotColor = NeonCrimson)
        .clip(CircleShape)
        .background(GoldMetallic3D)
        .padding(2.5.dp)
        .clip(CircleShape)
        .background(CyberBlack),
      contentAlignment = Alignment.Center
    ) {
      // Inner Titanium Bevel
      Box(
        modifier = Modifier
          .size(size * 0.76f)
        .clip(CircleShape)
        .background(CrimsonMetallic3D)
        .padding(2.dp)
        .clip(CircleShape)
        .background(Color(0xFF0D111A)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Shield,
          contentDescription = "Official VIP Shield",
          tint = NeonGold,
          modifier = Modifier.size(size * 0.45f)
        )
      }
    }
  }
}

/**
 * 3D Executive VIP Membership Card
 */
@Composable
fun ThreeDVipCard(
  vipKey: VipKey,
  deviceId: String,
  modifier: Modifier = Modifier
) {
  ThreeDCard(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    borderBrush = Brush.linearGradient(
      listOf(Color(0xFFFFDF7A), Color(0xFFFF1E56), Color(0x33FFFFFF))
    ),
    backgroundBrush = Brush.linearGradient(
      listOf(Color(0xFF161A28), Color(0xFF0F121C), Color(0xFF080A10))
    ),
    elevation = 20.dp,
    tiltEffect = true
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Top Row: Brand & Chip
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = null,
            tint = NeonGold,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "ROHIT VIP",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            fontFamily = FontFamily.Monospace
          )
        }

        // Chip Simulation
        Box(
          modifier = Modifier
            .width(36.dp)
            .height(26.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(
              Brush.linearGradient(
                listOf(Color(0xFFFFDF7A), Color(0xFFFFB800), Color(0xFFC48800))
              )
            )
            .border(0.8.dp, Color(0xFFFFF2B2), RoundedCornerShape(6.dp))
            .padding(3.dp)
        ) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .border(0.5.dp, Color(0x66000000), RoundedCornerShape(3.dp))
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Key Id (Embossed style)
      Text(
        text = vipKey.keyId,
        color = NeonGold,
        fontSize = 16.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 2.sp,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier.drawBehind {
          // subtle 3D text shadow simulation
        }
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Card Details Row (Device ID & Expiry)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
      ) {
        Column {
          Text(
            text = "DEVICE ID",
            color = TextGray,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            fontFamily = FontFamily.Monospace
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = deviceId.take(16) + if (deviceId.length > 16) "..." else "",
            color = TextWhite,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
          )
        }

        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "STATUS & VALIDITY",
            color = TextGray,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            fontFamily = FontFamily.Monospace
          )
          Spacer(modifier = Modifier.height(2.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (vipKey.isExpired) NeonRed else NeonGreen)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (vipKey.isExpired) "EXPIRED" else vipKey.getRemainingTimeFormatted(),
              color = if (vipKey.isExpired) NeonRed else NeonGreen,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }
    }
  }
}

@Composable
fun CyberButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  isLoading: Boolean = false,
  rgbEnabled: Boolean = true,
  primaryColor: Color = NeonCyan,
  textColor: Color = CyberBlack
) {
  val rgbBrush = rememberRgbBrush(enabled = rgbEnabled)
  val shape = RoundedCornerShape(12.dp)

  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(52.dp)
      .clip(shape)
      .then(
        if (enabled) {
          Modifier
            .background(if (rgbEnabled) rgbBrush else Brush.horizontalGradient(listOf(primaryColor, NeonBlue)))
            .clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = null,
              onClick = onClick
            )
        } else {
          Modifier.background(Color(0xFF232B3E))
        }
      )
      .padding(1.5.dp)
      .clip(RoundedCornerShape(10.dp))
      .background(if (enabled) CyberBlack.copy(alpha = 0.85f) else Color(0xFF161B26)),
    contentAlignment = Alignment.Center
  ) {
    if (isLoading) {
      CircularProgressIndicator(
        modifier = Modifier.size(24.dp),
        color = NeonCyan,
        strokeWidth = 2.dp
      )
    } else {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Text(
          text = text,
          color = if (enabled) NeonCyan else TextGray,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.5.sp,
          fontFamily = FontFamily.Monospace
        )
      }
    }
  }
}

enum class StatusType {
  ONLINE,
  OFFLINE,
  KEY_ACTIVE,
  KEY_EXPIRED,
  SERVICE_RUNNING,
  SERVICE_STOPPED
}

@Composable
fun CyberStatusBadge(
  type: StatusType,
  modifier: Modifier = Modifier
) {
  val (label, dotColor, pulse) = when (type) {
    StatusType.ONLINE -> Triple("ONLINE", NeonGreen, true)
    StatusType.OFFLINE -> Triple("OFFLINE", NeonRed, false)
    StatusType.KEY_ACTIVE -> Triple("KEY ACTIVE", NeonCyan, true)
    StatusType.KEY_EXPIRED -> Triple("KEY EXPIRED", NeonRed, false)
    StatusType.SERVICE_RUNNING -> Triple("SERVICE RUNNING", NeonGreen, true)
    StatusType.SERVICE_STOPPED -> Triple("SERVICE STOPPED", TextGray, false)
  }

  val transition = rememberInfiniteTransition(label = "pulse")
  val alpha by if (pulse) {
    transition.animateFloat(
      initialValue = 0.4f,
      targetValue = 1f,
      animationSpec = infiniteRepeatable(
        animation = tween(800, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Reverse
      ),
      label = "dot_alpha"
    )
  } else {
    remember { androidx.compose.runtime.mutableFloatStateOf(1f) }
  }

  Row(
    modifier = modifier
      .clip(RoundedCornerShape(20.dp))
      .background(dotColor.copy(alpha = 0.12f))
      .border(1.dp, dotColor.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
      .padding(horizontal = 10.dp, vertical = 5.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(8.dp)
        .clip(CircleShape)
        .background(dotColor.copy(alpha = alpha))
    )
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = label,
      color = dotColor,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.sp,
      fontFamily = FontFamily.Monospace
    )
  }
}

@Composable
fun CyberParticles(
  modifier: Modifier = Modifier,
  particleCount: Int = 30
) {
  val transition = rememberInfiniteTransition(label = "particles")
  val progress by transition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 10000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "particle_motion"
  )

  val seeds = remember {
    List(particleCount) {
      Triple(
        Random.nextFloat(), // x ratio
        Random.nextFloat(), // y initial ratio
        Random.nextFloat() * 2.5f + 1f // size
      )
    }
  }

  Canvas(modifier = modifier.fillMaxSize()) {
    val width = size.width
    val height = size.height

    seeds.forEachIndexed { index, (xRatio, yRatio, pSize) ->
      val currentY = ((yRatio + progress * (0.3f + (index % 4) * 0.1f)) % 1.0f) * height
      val currentX = (xRatio * width) + (kotlin.math.sin((progress * 6.28f) + index) * 15f)

      val color = when (index % 3) {
        0 -> NeonCyan.copy(alpha = 0.25f)
        1 -> NeonMagenta.copy(alpha = 0.2f)
        else -> NeonBlue.copy(alpha = 0.2f)
      }

      drawCircle(
        color = color,
        radius = pSize,
        center = Offset(currentX, currentY)
      )
    }
  }
}
