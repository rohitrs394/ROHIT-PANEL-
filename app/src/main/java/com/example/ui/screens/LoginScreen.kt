package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.firebase.FirebaseKeyValidator
import com.example.models.KeyValidationResult
import com.example.models.VipKey
import com.example.security.CryptoUtils
import com.example.security.SessionManager
import com.example.ui.components.CyberButton
import com.example.ui.components.CyberParticles
import com.example.ui.components.CyberStatusBadge
import com.example.ui.components.GlassCard
import com.example.ui.components.StatusType
import com.example.ui.components.ThreeDButton
import com.example.ui.components.ThreeDCard
import com.example.ui.components.ThreeDCrest
import com.example.ui.components.ThreeDHorizonBackground
import com.example.ui.components.rememberRgbBrush
import com.example.util.VoiceGreetingHelper
import com.example.ui.theme.CrimsonGoldGradient
import com.example.ui.theme.CrimsonMetallic3D
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberGlassBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.GoldMetallic3D
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.launch

private const val ROHIT_WHATSAPP_NUMBER = "916289106267"
private const val ROHIT_WHATSAPP_DISPLAY = "+91 6289106267"

fun openWhatsAppForVipKey(context: Context, enteredKey: String = "") {
  val deviceId = CryptoUtils.getDeviceId(context)
  val message = buildString {
    append("Hello Rohit Sir, I want to purchase / get a VIP Key for ROHIT VIP PANEL.\n\n")
    append("📱 Device ID: $deviceId\n")
    if (enteredKey.isNotBlank()) {
      append("🔑 Key Entered: $enteredKey\n")
    }
    append("\nPlease provide me an ACTIVE VIP Key.")
  }

  val url = "https://api.whatsapp.com/send?phone=$ROHIT_WHATSAPP_NUMBER&text=" + Uri.encode(message)
  val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
  }

  try {
    intent.setPackage("com.whatsapp")
    context.startActivity(intent)
  } catch (_: Exception) {
    try {
      intent.setPackage(null)
      context.startActivity(intent)
    } catch (_: Exception) {
      Toast.makeText(context, "Contact Rohit Sir: $ROHIT_WHATSAPP_DISPLAY", Toast.LENGTH_LONG).show()
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
  onLoginSuccess: (VipKey) -> Unit
) {
  val context = LocalContext.current
  val sessionManager = remember { SessionManager(context) }
  val scope = rememberCoroutineScope()
  val keyboardController = LocalSoftwareKeyboardController.current

  var keyValue by remember { mutableStateOf(sessionManager.savedKey ?: "") }
  var isKeyVisible by remember { mutableStateOf(false) }
  var rememberKey by remember { mutableStateOf(sessionManager.rememberSession) }
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var showWhatsAppRedirectDialog by remember { mutableStateOf(false) }

  val rgbBrush = rememberRgbBrush(enabled = sessionManager.rgbEffectsEnabled)

  fun handleLogin() {
    val cleanKey = keyValue.trim()
    if (cleanKey.isEmpty()) {
      errorMessage = "PLEASE ENTER YOUR VIP KEY"
      return
    }

    keyboardController?.hide()
    isLoading = true
    errorMessage = null

    scope.launch {
      val result = FirebaseKeyValidator.validateKey(cleanKey, context)
      isLoading = false
      when (result) {
        is KeyValidationResult.Success -> {
          sessionManager.saveSession(result.key, rememberKey)
          val greetingName = result.key.getGreetingName()
          VoiceGreetingHelper.speakLoginWelcome(context, greetingName)
          onLoginSuccess(result.key)
        }
        is KeyValidationResult.InvalidKey -> {
          errorMessage = result.message
          showWhatsAppRedirectDialog = true
          openWhatsAppForVipKey(context, cleanKey)
        }
        is KeyValidationResult.Expired -> {
          errorMessage = result.message
          showWhatsAppRedirectDialog = true
          openWhatsAppForVipKey(context, cleanKey)
        }
        is KeyValidationResult.Disabled -> {
          errorMessage = result.message
          showWhatsAppRedirectDialog = true
          openWhatsAppForVipKey(context, cleanKey)
        }
        is KeyValidationResult.Revoked -> {
          errorMessage = result.message
          showWhatsAppRedirectDialog = true
          openWhatsAppForVipKey(context, cleanKey)
        }
        is KeyValidationResult.DeviceLimitExceeded -> {
          errorMessage = result.message
          showWhatsAppRedirectDialog = true
          openWhatsAppForVipKey(context, cleanKey)
        }
        is KeyValidationResult.NetworkError -> errorMessage = result.message
        is KeyValidationResult.ServerError -> errorMessage = result.message
      }
    }
  }

  // Official Dialog when key cannot be verified
  if (showWhatsAppRedirectDialog) {
    BasicAlertDialog(
      onDismissRequest = { showWhatsAppRedirectDialog = false },
      properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
      ThreeDCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        borderBrush = Brush.verticalGradient(listOf(NeonCrimson, Color(0x33000000))),
        elevation = 24.dp
      ) {
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(54.dp)
              .shadow(12.dp, CircleShape)
              .clip(CircleShape)
              .background(NeonCrimson.copy(alpha = 0.2f))
              .border(1.5.dp, NeonCrimson, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Warning,
              contentDescription = null,
              tint = NeonCrimson,
              modifier = Modifier.size(28.dp)
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = "LICENSE NOT FOUND",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp,
            fontFamily = FontFamily.Monospace
          )

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "The entered VIP license could not be validated. Contact Rohit Sir on WhatsApp to acquire or activate your official license.",
            color = TextGray,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(horizontal = 8.dp)
          )

          Spacer(modifier = Modifier.height(20.dp))

          ThreeDButton(
            text = "CONTACT OFFICIAL SUPPORT",
            onClick = {
              openWhatsAppForVipKey(context, keyValue.trim())
              showWhatsAppRedirectDialog = false
            },
            primaryBrush = Brush.horizontalGradient(listOf(Color(0xFF25D366), Color(0xFF128C7E))),
            baseColor = Color(0xFF075E54)
          )

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "DISMISS",
            color = TextGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
              .clickable { showWhatsAppRedirectDialog = false }
              .padding(8.dp)
          )
        }
      }
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(CyberBlack)
  ) {
    ThreeDHorizonBackground()
    CyberParticles(particleCount = 18)

    Column(
      modifier = Modifier
        .fillMaxSize()
        .imePadding()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp, vertical = 36.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      // 3D Floating Official Crest
      ThreeDCrest(size = 84.dp)

      Spacer(modifier = Modifier.height(18.dp))

      Text(
        text = "ROHIT VIP",
        color = TextWhite,
        fontSize = 26.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 3.sp,
        fontFamily = FontFamily.Monospace
      )

      Spacer(modifier = Modifier.height(6.dp))

      // Official Badge
      Row(
        modifier = Modifier
          .clip(RoundedCornerShape(20.dp))
          .background(GoldMetallic3D)
          .padding(1.dp)
          .clip(RoundedCornerShape(19.dp))
          .background(CyberBlack)
          .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(NeonGold)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "OFFICIAL VIP SUITE",
          color = NeonGold,
          fontSize = 10.sp,
          fontWeight = FontWeight.Black,
          letterSpacing = 1.5.sp,
          fontFamily = FontFamily.Monospace
        )
      }

      Spacer(modifier = Modifier.height(30.dp))

      // 3D Login Card Container
      ThreeDCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        borderBrush = Brush.verticalGradient(
          listOf(
            if (errorMessage != null) NeonRed else Color(0x66FFDF7A),
            Color(0x22FF1E56),
            Color(0x11000000)
          )
        ),
        elevation = 18.dp
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Key,
                contentDescription = null,
                tint = NeonGold,
                modifier = Modifier.size(17.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "VIP LICENSE ACTIVATION",
                color = TextWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontFamily = FontFamily.Monospace
              )
            }

            IconButton(
              onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                val clip = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()
                if (!clip.isNullOrBlank()) {
                  keyValue = clip.trim()
                } else {
                  Toast.makeText(context, "Clipboard empty", Toast.LENGTH_SHORT).show()
                }
              },
              modifier = Modifier.size(30.dp)
            ) {
              Icon(
                imageVector = Icons.Default.ContentPaste,
                contentDescription = "Paste Key",
                tint = NeonGold,
                modifier = Modifier.size(17.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Key Input Field
          OutlinedTextField(
            value = keyValue,
            onValueChange = {
              keyValue = it
              if (errorMessage != null) errorMessage = null
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = Color(0xFF0B0E17),
              unfocusedContainerColor = Color(0xFF080A10),
              focusedBorderColor = NeonGold,
              unfocusedBorderColor = Color(0xFF1E2638),
              focusedTextColor = TextWhite,
              unfocusedTextColor = TextWhite,
              cursorColor = NeonGold
            ),
            placeholder = {
              Text(
                text = "Enter VIP License Key",
                color = TextGray.copy(alpha = 0.5f),
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
              )
            },
            singleLine = true,
            visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
              Row(verticalAlignment = Alignment.CenterVertically) {
                if (keyValue.isNotEmpty()) {
                  IconButton(onClick = { keyValue = "" }) {
                    Icon(
                      imageVector = Icons.Default.Clear,
                      contentDescription = "Clear",
                      tint = TextGray,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                }
                IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                  Icon(
                    imageVector = if (isKeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = "Toggle visibility",
                    tint = TextGray,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { handleLogin() })
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Remember License checkbox
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clickable { rememberKey = !rememberKey }
              .padding(vertical = 4.dp)
          ) {
            Checkbox(
              checked = rememberKey,
              onCheckedChange = { rememberKey = it },
              colors = CheckboxDefaults.colors(
                checkedColor = NeonGold,
                uncheckedColor = TextGray,
                checkmarkColor = CyberBlack
              )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Save License on this Device",
              color = TextGray,
              fontSize = 12.sp,
              fontFamily = FontFamily.Monospace
            )
          }

          // Error Message Display
          AnimatedVisibility(
            visible = errorMessage != null,
            enter = fadeIn(),
            exit = fadeOut()
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(NeonRed.copy(alpha = 0.15f))
                .border(1.dp, NeonRed.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                .padding(12.dp)
            ) {
              Text(
                text = errorMessage ?: "",
                color = NeonRed,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // 3D Physical Push Button
          ThreeDButton(
            text = "ACTIVATE VIP",
            onClick = { handleLogin() },
            isLoading = isLoading,
            enabled = !isLoading,
            primaryBrush = CrimsonGoldGradient,
            baseColor = Color(0xFF6B0E2A)
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Direct WhatsApp Official Support Button
      ThreeDButton(
        text = "OFFICIAL SUPPORT • WHATSAPP",
        onClick = {
          openWhatsAppForVipKey(context, keyValue.trim())
        },
        primaryBrush = Brush.horizontalGradient(listOf(Color(0xFF25D366), Color(0xFF128C7E))),
        baseColor = Color(0xFF0B5738),
        height = 46.dp
      )

      Spacer(modifier = Modifier.height(26.dp))

      // Clean Official Footer
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(NeonGreen)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "ROHIT VIP • OFFICIAL EXECUTIVE SUITE",
          color = TextGray,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          letterSpacing = 1.sp
        )
      }
    }
  }
}
