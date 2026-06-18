package com.mjaremczuk.motiv.ui.screens

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mjaremczuk.motiv.domain.model.Quote
import com.mjaremczuk.motiv.ui.theme.VerveTheme
import com.mjaremczuk.motiv.ui.viewmodel.QuoteUiState
import com.mjaremczuk.motiv.ui.viewmodel.QuoteViewModel

@Composable
fun QuoteScreen(viewModel: QuoteViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val canGoBack by viewModel.canGoBack.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val notificationHour by viewModel.notificationHour.collectAsState()
    val notificationMinute by viewModel.notificationMinute.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val categories by viewModel.categories.collectAsState()

    val context = LocalContext.current
    var showSettingsDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.setNotificationsEnabled(true)
            Toast.makeText(context, "Daily notifications enabled!", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.setNotificationsEnabled(false)
            Toast.makeText(context, "Notification permission denied.", Toast.LENGTH_SHORT).show()
        }
    }

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
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Settings header at the top (takes own height to prevent card overlap)
            SettingsHeader(onSettingsClick = { showSettingsDialog = true })

            // Content area (clicks trigger random quote)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
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
                        is QuoteUiState.Success -> QuoteContent(
                            quote = state.quote,
                            isOffline = state.isOffline,
                            canGoBack = canGoBack,
                            onBackClick = { viewModel.goBack() }
                        )
                        is QuoteUiState.Error -> ErrorContent(state.message, onRetry = { viewModel.fetchRandomQuote() })
                    }
                }
            }
        }

        // Settings Dialog with custom dim background overlay
        if (showSettingsDialog) {
            SettingsDialog(
                notificationsEnabled = notificationsEnabled,
                notificationHour = notificationHour,
                notificationMinute = notificationMinute,
                selectedCategory = selectedCategory,
                categories = categories,
                onDismiss = { showSettingsDialog = false },
                onNotificationToggle = { checked ->
                    if (checked) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.setNotificationsEnabled(true)
                            Toast.makeText(context, "Daily notifications enabled!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        viewModel.setNotificationsEnabled(false)
                        Toast.makeText(context, "Notifications disabled", Toast.LENGTH_SHORT).show()
                    }
                },
                onTimeSelect = { hour, minute ->
                    viewModel.setNotificationTime(hour, minute)
                },
                onCategorySelect = { category ->
                    viewModel.selectCategory(category)
                }
            )
        }
    }
}

@Composable
fun SettingsHeader(onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.25f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Rounded.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun SettingsDialog(
    notificationsEnabled: Boolean,
    notificationHour: Int,
    notificationMinute: Int,
    selectedCategory: String,
    categories: List<Pair<String, String>>,
    onDismiss: () -> Unit,
    onNotificationToggle: (Boolean) -> Unit,
    onTimeSelect: (Int, Int) -> Unit,
    onCategorySelect: (String) -> Unit
) {
    val context = LocalContext.current

    // Dim overlay backdrop
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
            shape = RoundedCornerShape(28.dp)
        ),
        title = {
            Text(
                text = "Quick Settings",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CategorySelectionRow(
                    selectedCategory = selectedCategory,
                    categories = categories,
                    onCategorySelected = onCategorySelect
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                NotificationToggleRow(
                    enabled = notificationsEnabled,
                    onCheckedChange = onNotificationToggle
                )

                if (notificationsEnabled) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    TimePickerRow(
                        hour = notificationHour,
                        minute = notificationMinute,
                        onTimeSelected = { hour, minute ->
                            onTimeSelect(hour, minute)
                            Toast.makeText(
                                context,
                                String.format("Reminder set to %02d:%02d", hour, minute),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelectionRow(
    selectedCategory: String,
    categories: List<Pair<String, String>>,
    onCategorySelected: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Quote Category",
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyLarge
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { (key, label) ->
                val isSelected = selectedCategory == key
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelected(key) },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    }
}

@Composable
fun NotificationToggleRow(
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Daily Inspiration",
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Get a reminder quote every day",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = enabled,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun TimePickerRow(
    hour: Int,
    minute: Int,
    onTimeSelected: (Int, Int) -> Unit
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                TimePickerDialog(
                    context,
                    { _, selectedHour, selectedMinute ->
                        onTimeSelected(selectedHour, selectedMinute)
                    },
                    hour,
                    minute,
                    true
                ).show()
            }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Reminder Time",
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Tap to change scheduled time",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.AccessTime,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = String.format("%02d:%02d", hour, minute),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodyMedium
            )
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
fun QuoteContent(
    quote: Quote,
    isOffline: Boolean,
    canGoBack: Boolean,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isOffline) {
            OfflineIndicator()
            Spacer(modifier = Modifier.height(16.dp))
        }

        QuoteCard(
            quote = quote,
            canGoBack = canGoBack,
            onBackClick = onBackClick
        )

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
            text = "Verve Inspiration",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun QuoteCard(
    quote: Quote,
    canGoBack: Boolean,
    onBackClick: () -> Unit
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

            Spacer(modifier = Modifier.height(24.dp))
            
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 1.dp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            QuoteActionButtons(
                quote = quote,
                canGoBack = canGoBack,
                onBackClick = onBackClick
            )
        }
    }
}

@Composable
fun QuoteActionButtons(
    quote: Quote,
    canGoBack: Boolean,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button (left aligned)
        IconButton(
            onClick = onBackClick,
            enabled = canGoBack,
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.primary,
                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Previous Quote"
            )
        }

        // Copy & Share buttons (right aligned)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString("\"${quote.text}\" — ${quote.author}"))
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                        Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.ContentCopy,
                    contentDescription = "Copy Quote"
                )
            }

            IconButton(
                onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "\"${quote.text}\" — ${quote.author}")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Quote"))
                },
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Share,
                    contentDescription = "Share Quote"
                )
            }
        }
    }
}

@Composable
fun OfflineIndicator() {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f),
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = CircleShape,
        modifier = Modifier.wrapContentSize()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.CloudOff,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Offline Inspiration",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
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
                ),
                isOffline = true,
                canGoBack = true,
                onBackClick = {}
            )
        }
    }
}
