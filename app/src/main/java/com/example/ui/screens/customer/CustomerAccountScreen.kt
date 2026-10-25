package com.example.ui.screens.customer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.EasyBizzyCard
import com.example.ui.components.EasyBizzyPrimaryButton
import com.example.ui.components.EasyBizzySecondaryButton
import com.example.ui.components.EasyBizzyTextField
import com.example.ui.components.StatusPill
import com.example.ui.theme.EasyBizzyThemeTokens

/**
 * Customer Account & Profile Settings Screen.
 */
@Composable
fun CustomerAccountScreen(
  onSwitchToShopkeeper: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var customerName by remember { mutableStateOf("Kshitiz Gupta") }
  var customerPhone by remember { mutableStateOf("9811223344") }
  var deliveryArea by remember { mutableStateOf("Pocket B, Mayur Vihar, Delhi") }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(16.dp)
      .testTag("customer_account_screen"),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Text(
      text = "Customer Account",
      style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
    )

    // Profile Details Card
    EasyBizzyCard(modifier = Modifier.fillMaxWidth()) {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "Your Profile",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          )
          StatusPill(
            text = "VERIFIED",
            backgroundColor = EasyBizzyThemeTokens.colors.khataLenaContainer,
            contentColor = EasyBizzyThemeTokens.colors.khataLena,
          )
        }

        EasyBizzyTextField(
          value = customerName,
          onValueChange = { customerName = it },
          label = "Full Name",
          leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
          testTag = "input_cust_name",
        )

        EasyBizzyTextField(
          value = customerPhone,
          onValueChange = { customerPhone = it },
          label = "Mobile Number",
          prefix = "+91 ",
          testTag = "input_cust_phone",
        )

        EasyBizzyTextField(
          value = deliveryArea,
          onValueChange = { deliveryArea = it },
          label = "Default Neighborhood / Address",
          testTag = "input_cust_address",
        )
      }
    }

    // Role Switcher Promo Card
    EasyBizzyCard(modifier = Modifier.fillMaxWidth()) {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Store,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
          )
          Spacer(modifier = Modifier.padding(horizontal = 4.dp))
          Text(
            text = "Own a Local Shop or Business?",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
          )
        }

        Text(
          text = "Switch to Shopkeeper Mode to access offline-first billing, digital Udhar Bahi-Khata, stock tracking, and local customer marketplace broadcasting.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        EasyBizzySecondaryButton(
          text = "Switch to Shopkeeper Mode",
          onClick = onSwitchToShopkeeper,
          modifier = Modifier.fillMaxWidth(),
          testTag = "btn_switch_to_shopkeeper",
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}
