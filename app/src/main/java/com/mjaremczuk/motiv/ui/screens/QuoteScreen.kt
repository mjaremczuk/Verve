package com.mjaremczuk.motiv.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mjaremczuk.motiv.data.model.Quote
import com.mjaremczuk.motiv.ui.theme.VerveTheme
import com.mjaremczuk.motiv.ui.viewmodel.QuoteUiState
import com.mjaremczuk.motiv.ui.viewmodel.QuoteViewModel

@Composable
fun QuoteScreen(viewModel: QuoteViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.systemBars
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .clickable { viewModel.fetchRandomQuote() }
            ) {
                AnimatedContent(
                    targetState = uiState,
                    transitionSpec = {
                        (fadeIn(animationSpec = spring()) + scaleIn(initialScale = 0.92f, animationSpec = spring()))
                            .togetherWith(fadeOut(animationSpec = spring()) + scaleOut(targetScale = 1.08f, animationSpec = spring()))
                    },
                    label = "QuoteStateTransition",
                    modifier = Modifier.fillMaxSize()
                ) { state ->
                    when (state) {
                        is QuoteUiState.Loading -> LoadingContent()
                        is QuoteUiState.Success -> QuoteContent(state.quote)
                        is QuoteUiState.Error -> ErrorContent(state.message, onRetry = { viewModel.fetchRandomQuote() })
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 6.dp,
            modifier = Modifier.size(64.dp)
        )
    }
}

@Composable
fun QuoteContent(quote: Quote) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(40.dp)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Rounded.FormatQuote,
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .align(Alignment.Start),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                )
                
                Text(
                    text = quote.text,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 44.sp
                    ),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "— ${quote.author}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Tap for new inspiration",
            style = MaterialTheme.typography.labelLarge.copy(
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            text = "Inspirational quotes provided by ZenQuotes.io",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Warning,
            contentDescription = "Error",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Rounded.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Try Again")
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun QuoteContentPreview() {
    VerveTheme {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primaryContainer)) {
            QuoteContent(
                quote = Quote(
                    text = "Success is not final, failure is not fatal: it is the courage to continue that counts.",
                    author = "Winston Churchill",
                    html = ""
                )
            )
        }
    }
}
