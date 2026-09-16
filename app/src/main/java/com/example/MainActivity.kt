package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.models.VipKey
import com.example.security.SessionManager
import com.example.ui.screens.AccessGrantedScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.MyApplicationTheme

object Routes {
  const val SPLASH = "splash"
  const val LOGIN = "login"
  const val ACCESS_GRANTED = "access_granted"
  const val DASHBOARD = "dashboard"
  const val SETTINGS = "settings"
}

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = CyberBlack
        ) {
          RohitVipAppNavigation()
        }
      }
    }
  }
}

@Composable
fun RohitVipAppNavigation() {
  val navController = rememberNavController()
  val context = LocalContext.current
  val sessionManager = remember { SessionManager(context) }
  var currentVipKey by remember { mutableStateOf(sessionManager.getSessionKey() ?: VipKey()) }

  NavHost(
    navController = navController,
    startDestination = Routes.SPLASH
  ) {
    composable(Routes.SPLASH) {
      SplashScreen(
        onNavigateToLogin = {
          navController.navigate(Routes.LOGIN) {
            popUpTo(Routes.SPLASH) { inclusive = true }
          }
        },
        onNavigateToDashboard = {
          currentVipKey = sessionManager.getSessionKey() ?: VipKey()
          navController.navigate(Routes.DASHBOARD) {
            popUpTo(Routes.SPLASH) { inclusive = true }
          }
        }
      )
    }

    composable(Routes.LOGIN) {
      LoginScreen(
        onLoginSuccess = { validatedKey ->
          currentVipKey = validatedKey
          navController.navigate(Routes.ACCESS_GRANTED) {
            popUpTo(Routes.LOGIN) { inclusive = true }
          }
        }
      )
    }

    composable(Routes.ACCESS_GRANTED) {
      AccessGrantedScreen(
        vipKey = currentVipKey,
        onNavigateToDashboard = {
          navController.navigate(Routes.DASHBOARD) {
            popUpTo(Routes.ACCESS_GRANTED) { inclusive = true }
          }
        }
      )
    }

    composable(Routes.DASHBOARD) {
      DashboardScreen(
        vipKey = currentVipKey,
        onNavigateToSettings = {
          navController.navigate(Routes.SETTINGS)
        }
      )
    }

    composable(Routes.SETTINGS) {
      SettingsScreen(
        onNavigateBack = {
          navController.popBackStack()
        },
        onLogoutCompleted = {
          navController.navigate(Routes.LOGIN) {
            popUpTo(0) { inclusive = true }
          }
        }
      )
    }
  }
}
