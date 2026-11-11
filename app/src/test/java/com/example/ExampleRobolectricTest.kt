package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("MyLycée", appName)
  }

  @Test
  fun `launch SplashActivity`() {
    org.robolectric.Robolectric.buildActivity(SplashActivity::class.java).setup()
  }

  @Test
  fun `launch MainActivity`() {
    org.robolectric.Robolectric.buildActivity(MainActivity::class.java).setup()
  }
}
