package com.mjaremczuk.motiv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mjaremczuk.motiv.ui.screens.QuoteScreen
import com.mjaremczuk.motiv.ui.theme.VerveTheme
import com.mjaremczuk.motiv.ui.viewmodel.QuoteViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VerveTheme {
                val viewModel: QuoteViewModel = viewModel()
                QuoteScreen(viewModel = viewModel)
            }
        }
    }
}
