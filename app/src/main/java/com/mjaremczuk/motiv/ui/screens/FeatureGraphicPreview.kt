package com.mjaremczuk.motiv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mjaremczuk.motiv.ui.theme.VerveTheme

@Composable
fun FeatureGraphic() {
    Box(
        modifier = Modifier
            .size(width = 1024.dp, height = 500.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF6750A4), // PrimaryLight
                        Color(0xFF7C4DFF), // EnergeticViolet
                        Color(0xFFFF4081)  // EnergeticMagenta
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Decorative background elements (subtle circles/sparkles)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(48.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.TopStart),
                tint = Color.White.copy(alpha = 0.1f)
            )
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.BottomEnd),
                tint = Color.White.copy(alpha = 0.15f)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Large 'V' Icon with Sparkle
            Box(contentAlignment = Alignment.TopEnd) {
                Text(
                    text = "V",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 200.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = (-10).sp
                    )
                )
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .offset(x = 20.dp, y = (-20).dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // App Name
            Text(
                text = "Verve",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 120.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = (-2).sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tagline
            Text(
                text = "Daily Inspiration & Motivational Quotes",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Medium,
                    fontStyle = FontStyle.Italic,
                    color = Color.White.copy(alpha = 0.9f)
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(widthDp = 1024, heightDp = 500)
@Composable
fun FeatureGraphicPreview() {
    VerveTheme {
        Surface {
            FeatureGraphic()
        }
    }
}
