package com.mjaremczuk.motiv

import android.graphics.Bitmap
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import androidx.test.platform.app.InstrumentationRegistry
import com.mjaremczuk.motiv.ui.screens.FeatureGraphic
import com.mjaremczuk.motiv.ui.theme.VerveTheme
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

class FeatureGraphicCaptureTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun captureFeatureGraphic() {
        composeTestRule.setContent {
            VerveTheme {
                // Set density to 1.0f so 1dp = 1px, resulting in exactly 1024x500px capture
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    FeatureGraphic()
                }
            }
        }

        val bitmap = composeTestRule.onRoot().captureToImage().asAndroidBitmap()
        
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val outputDir = context.getExternalFilesDir(null)
        val file = File(outputDir, "feature_graphic.png")
        
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        
        val adbCommand = "adb pull ${file.absolutePath} ."
        println("=========================================================")
        println("Feature Graphic saved to: ${file.absolutePath}")
        println("THE TEST WILL WAIT FOR 30 SECONDS. RUN THIS COMMAND NOW:")
        println(adbCommand)
        println("=========================================================")
        
        // Wait 30 seconds to allow the user to pull the file via ADB before the test uninstalls the app.
        Thread.sleep(30000)
    }
}
