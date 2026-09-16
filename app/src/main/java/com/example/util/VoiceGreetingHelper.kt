package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

/**
 * Text-to-speech assistant configured for sweet female voice welcome greeting.
 * Greets logged-in users with: "Welcome [Name] baby".
 */
object VoiceGreetingHelper {
  private const val TAG = "VoiceGreetingHelper"
  private var textToSpeech: TextToSpeech? = null
  private var isReady: Boolean = false
  private var pendingSpeechText: String? = null

  fun initialize(context: Context) {
    if (textToSpeech != null) return
    try {
      textToSpeech = TextToSpeech(context.applicationContext) { status ->
        if (status == TextToSpeech.SUCCESS) {
          isReady = true
          tuneSweetFemaleVoice()
          val queued = pendingSpeechText
          if (!queued.isNullOrBlank()) {
            speakInternal(queued)
            pendingSpeechText = null
          }
        } else {
          Log.w(TAG, "TTS Initialization failed with status $status")
        }
      }
    } catch (e: Exception) {
      Log.w(TAG, "TTS Init exception: ${e.message}")
    }
  }

  private fun tuneSweetFemaleVoice() {
    val tts = textToSpeech ?: return
    try {
      tts.language = Locale.US

      // Higher pitch gives a natural, melodious sweet female vocal tone
      tts.setPitch(1.35f)
      tts.setSpeechRate(0.92f)

      // Query device voices to pick female voice if installed
      val voices = tts.voices
      if (!voices.isNullOrEmpty()) {
        val femaleVoice = voices.firstOrNull { v ->
          v.locale.language == "en" && (
            v.name.contains("female", ignoreCase = true) ||
            v.name.contains("woman", ignoreCase = true) ||
            v.name.contains("#female", ignoreCase = true) ||
            v.features?.any { it.contains("female", ignoreCase = true) } == true
          )
        } ?: voices.firstOrNull { v -> v.locale.language == "en" }

        if (femaleVoice != null) {
          tts.voice = femaleVoice
        }
      }
    } catch (e: Exception) {
      Log.d(TAG, "Voice tuning notice: ${e.message}")
    }
  }

  /**
   * Speaks: "Welcome [Name] baby" in sweet girl voice.
   */
  fun speakLoginWelcome(context: Context, rawName: String) {
    initialize(context)
    val name = rawName.trim()
    val textToSpeak = if (name.isNotBlank()) {
      "Welcome $name baby"
    } else {
      "Welcome baby"
    }

    if (isReady && textToSpeech != null) {
      speakInternal(textToSpeak)
    } else {
      pendingSpeechText = textToSpeak
    }
  }

  private fun speakInternal(text: String) {
    try {
      textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "rohit_vip_voice_id")
    } catch (e: Exception) {
      Log.w(TAG, "TTS Speak error: ${e.message}")
    }
  }

  fun shutdown() {
    try {
      textToSpeech?.stop()
      textToSpeech?.shutdown()
    } catch (_: Exception) {}
    textToSpeech = null
    isReady = false
    pendingSpeechText = null
  }
}
