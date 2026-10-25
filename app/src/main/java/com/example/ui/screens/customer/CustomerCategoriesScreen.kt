package com.example.ui.screens.customer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.EasyBizzyThemeTokens

data class CategoryItem(val name: String, val hindiName: String, val icon: ImageVector)

val sampleCategories = listOf(
  CategoryItem("Kirana & Grocery", "किराना एवं जनरल स्टोर", Icons.Default.Store),
  CategoryItem("Pharmacy & Chemist", "दवाई दुकान", Icons.Default.LocalPharmacy),
  CategoryItem("Fresh Sabzi & Fruits", "सब्जी एवं फल मंडी", Icons.Default.ShoppingBasket),
  CategoryItem("Dairy & Sweets", "दूध एवं मिष्ठान", Icons.Default.WaterDrop),
  CategoryItem("Electrical & Hardware", "इलेक्ट्रिकल एवं हार्डवेयर", Icons.Default.ElectricBolt),
  CategoryItem("Bakery & Snacks", "बेकरी एवं स्नैक्स", Icons.Default.Restaurant),
)

/**
 * Screen displaying local business retail categories for customer navigation.
 */
@Composable
fun CustomerCategoriesScreen(
  onCategorySelected: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp)
      .testTag("customer_categories_screen"),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Text(
      text = "Explore Categories",
      style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
    )

    LazyVerticalGrid(
      columns = GridCells.Fixed(2),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      items(sampleCategories) { category ->
        Card(
          onClick = { onCategorySelected(category.name) },
          shape = MaterialTheme.shapes.medium,
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
          ),
          modifier = Modifier.height(115.dp).testTag("cat_card_${category.name}"),
        ) {
          Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween,
          ) {
            Icon(
              imageVector = category.icon,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(28.dp),
            )
            Column {
              Text(
                text = category.name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = category.hindiName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
              )
            }
          }
        }
      }
    }
  }
}
