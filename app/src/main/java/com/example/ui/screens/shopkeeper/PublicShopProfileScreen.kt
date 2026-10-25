package com.example.ui.screens.shopkeeper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import com.example.ui.components.EasyBizzyTextField
import com.example.ui.components.StatusPill
import com.example.ui.theme.EasyBizzyThemeTokens

/**
 * Screen for managing the merchant's public storefront profile on the local marketplace.
 * Enforces strict security boundary: private records are never published.
 */
@Composable
fun PublicShopProfileScreen(
  onSaveProfile: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var shopName by remember { mutableStateOf("Sharma General Store") }
  var ownerName by remember { mutableStateOf("Ramesh Sharma") }
  var publicPhone by remember { mutableStateOf("9876543210") }
  var category by remember { mutableStateOf("Kirana & Grocery") }
  var address by remember { mutableStateOf("Shop 14, Main Market, Sector 21") }
  var pincode by remember { mutableStateOf("110001") }
  var upiId by remember { mutableStateOf("sharma.kirana@upi") }
  var isMarketplaceLive by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(16.dp)
      .testTag("public_shop_profile_screen"),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Text(
      text = "Public Shop Profile",
      style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
    )

    // Security & Privacy Guarantee Banner
    Card(
      shape = RoundedCornerShape(EasyBizzyThemeTokens.spacing.cardRadius),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
      ),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row(
        modifier = Modifier.padding(14.dp),
        verticalAlignment = Alignment.Top,
      ) {
        Icon(
          imageVector = Icons.Default.Lock,
          contentDescription = "Encrypted Airgap",
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(top = 2.dp),
        )
        Spacer(modifier = Modifier.padding(horizontal = 6.dp))
        Column {
          Text(
            text = "Strict Privacy Airgap",
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
          )
          Text(
            text = "Only the details below and products you explicitly toggle for marketplace will be visible to nearby customers. Your customer Khata, bills, costs, and profits never leave this device.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
          )
        }
      }
    }

    // Marketplace Status Toggle Card
    EasyBizzyCard(modifier = Modifier.fillMaxWidth()) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Live on Local Marketplace",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          )
          Text(
            text = if (isMarketplaceLive) {
              "Nearby customers can discover your store and check published prices."
            } else {
              "Storefront is hidden. Customers cannot view your catalog."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
          )
        }
        Switch(
          checked = isMarketplaceLive,
          onCheckedChange = { isMarketplaceLive = it },
          modifier = Modifier.testTag("switch_marketplace_live"),
        )
      }
    }

    // Public Profile Form
    EasyBizzyCard(modifier = Modifier.fillMaxWidth()) {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
          text = "Storefront Information",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        )

        EasyBizzyTextField(
          value = shopName,
          onValueChange = { shopName = it },
          label = "Shop / Business Name",
          placeholder = "e.g., Mahadev Kirana Store",
          testTag = "input_shop_name",
        )

        EasyBizzyTextField(
          value = ownerName,
          onValueChange = { ownerName = it },
          label = "Owner Name",
          testTag = "input_owner_name",
        )

        EasyBizzyTextField(
          value = publicPhone,
          onValueChange = { publicPhone = it },
          label = "Public Order Phone / WhatsApp",
          prefix = "+91 ",
          testTag = "input_public_phone",
        )

        EasyBizzyTextField(
          value = category,
          onValueChange = { category = it },
          label = "Store Category",
          testTag = "input_store_category",
        )

        EasyBizzyTextField(
          value = address,
          onValueChange = { address = it },
          label = "Full Street Address",
          testTag = "input_store_address",
        )

        EasyBizzyTextField(
          value = pincode,
          onValueChange = { pincode = it },
          label = "Pincode",
          testTag = "input_store_pincode",
        )

        EasyBizzyTextField(
          value = upiId,
          onValueChange = { upiId = it },
          label = "Store UPI ID (For Direct Payments)",
          placeholder = "shopname@okaxis",
          testTag = "input_store_upi",
        )
      }
    }

    EasyBizzyPrimaryButton(
      text = "Save Shop Profile",
      onClick = onSaveProfile,
      modifier = Modifier.fillMaxWidth(),
      testTag = "btn_save_shop_profile",
    )

    Spacer(modifier = Modifier.height(16.dp))
  }
}
