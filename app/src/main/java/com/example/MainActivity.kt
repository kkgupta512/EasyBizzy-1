package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.ui.navigation.EasyBizzyAppScaffold
import com.example.ui.theme.EasyBizzyTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val systemInDark = isSystemInDarkTheme()
      var isDarkTheme by remember { mutableStateOf(systemInDark) }

      EasyBizzyTheme(darkTheme = isDarkTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
          EasyBizzyAppScaffold(
            isDarkTheme = isDarkTheme,
            onToggleTheme = { isDarkTheme = !isDarkTheme },
          )
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun EasyBizzyAppPreview() {
  EasyBizzyTheme(darkTheme = false) {
    EasyBizzyAppScaffold(
      isDarkTheme = false,
      onToggleTheme = {},
    )
  }
}
