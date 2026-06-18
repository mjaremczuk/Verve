package com.mjaremczuk.motiv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mjaremczuk.motiv.ui.screens.QuoteScreen
import com.mjaremczuk.motiv.ui.theme.VerveTheme
import com.mjaremczuk.motiv.ui.viewmodel.QuoteViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            VerveTheme {
                val viewModel: QuoteViewModel = koinViewModel()
                QuoteScreen(viewModel = viewModel)
            }
        }
    }
}
