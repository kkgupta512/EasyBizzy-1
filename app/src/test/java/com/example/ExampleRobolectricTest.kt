package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.components.formatToIndianCurrency
import com.example.ui.navigation.CustomerDestination
import com.example.ui.navigation.ShopkeeperDestination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context verifies EasyBizzy app name`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("EasyBizzy", appName)
  }

  @Test
  fun `formatToIndianCurrency formats numbers correctly`() {
    val formatted = formatToIndianCurrency(125000.0, showDecimals = false)
    assertTrue("Formatted currency should contain 1,25,000 or 125,000", formatted.contains("125") || formatted.contains("1,25"))
  }

  @Test
  fun `verify shopkeeper destinations count and non-empty routes`() {
    val destinations = ShopkeeperDestination.all
    assertEquals(10, destinations.size)
    destinations.forEach { dest ->
      assertNotNull("Destination in list must not be null", dest)
      assertTrue("Route must not be empty", dest.route.isNotBlank())
      assertTrue("Title must not be empty", dest.title.isNotBlank())
    }
  }

  @Test
  fun `verify customer destinations count and non-empty routes`() {
    val destinations = CustomerDestination.all
    assertEquals(5, destinations.size)
    destinations.forEach { dest ->
      assertNotNull("Destination in list must not be null", dest)
      assertTrue("Route must not be empty", dest.route.isNotBlank())
      assertTrue("Title must not be empty", dest.title.isNotBlank())
    }
  }
}
