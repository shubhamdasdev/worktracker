package com.example.worktracker.ui

import android.Manifest
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.worktracker.AppViewModelProvider
import com.example.worktracker.Constants
import com.example.worktracker.NotificationHandler
import com.example.worktracker.R
import com.example.worktracker.ui.theme.WorkTrackerAnimations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SnackbarVisualsImpl(
    override val message: String,
) : SnackbarVisuals {
    override val actionLabel: String
        get() = ""
    override val withDismissAction: Boolean
        get() = true
    override val duration: SnackbarDuration
        get() = SnackbarDuration.Short
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewShiftsOnClick: () -> Unit = {},
    navigateToSettings: () -> Unit,
    viewModel: MainViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {

    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        CheckPermissions(context)
    }

    val scope = rememberCoroutineScope()
    val createFileLauncher = rememberLauncherForActivityResult(CreateDocument("text/csv")) { uri ->
        uri?.let {
            scope.launch {
                val shiftData = viewModel.fetchShiftData()
                val csvHeader = "id,date,shiftSpan,breakTotal,shiftTotal\n"
                val csvContent = StringBuilder(csvHeader)

                for (shift in shiftData) {
                    csvContent.append("${shift.id},${shift.date},${shift.shiftSpan},${shift.breakTotal},${shift.shiftTotal}\n")
                }
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    outputStream.write(csvContent.toString().toByteArray())
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context,
                        context.getString(R.string.file_successfully_saved), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val showMenu = remember { mutableStateOf(false) }
    
    // Animation for the counter
    val counterScale by animateFloatAsState(
        targetValue = if (uiState.clockedIn) 1.2f else 1.0f,
        animationSpec = tween(durationMillis = 500),
        label = "counterScale"
    )
    
    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                ElevatedCard(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Snackbar(
                        modifier = Modifier.padding(0.dp),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        dismissAction = {
                            IconButton(onClick = { data.dismiss() }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.dismiss_snackbar)
                                )
                            }
                        }
                    ) {
                        Text(
                            text = data.visuals.message,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.main_screen_top_bar),
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    Box {
                        IconButton(onClick = { showMenu.value = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.settings)
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu.value,
                            onDismissRequest = { showMenu.value = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(text = stringResource(R.string.settings)) },
                                onClick = {
                                    showMenu.value = false
                                    navigateToSettings()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(text = stringResource(R.string.export_csv)) },
                                onClick = {
                                    showMenu.value = false
                                    createFileLauncher.launch("shifts.csv")
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            AnimatedVisibility(
                visible = uiState.clockedIn,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
            ) {
                ElevatedCard(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(0.9f),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Weather widget
                        if (uiState.weatherEnabled) {
                            WeatherWidget(
                                weatherEnabled = uiState.weatherEnabled,
                                weatherState = viewModel.weatherState,
                                onRequestPermission = { viewModel.requestWeatherPermission() },
                                onRefreshWeather = { viewModel.refreshWeather(context) },
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                        
                        // Counter with animation
                        Text(
                            text = uiState.counter,
                            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier
                                .testTag("counter")
                                .scale(counterScale)
                                .padding(vertical = 16.dp)
                        )
                        
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        // Shift info section
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            InfoRow(
                                label = stringResource(R.string.shift_start),
                                value = uiState.shiftStartTime
                            )
                            
                            // Break start time (only shown when on break)
                            AnimatedVisibility(
                                visible = uiState.onBreak,
                                enter = expandVertically() + fadeIn(),
                                exit = slideOutVertically() + fadeOut()
                            ) {
                                InfoRow(
                                    label = stringResource(R.string.break_start),
                                    value = uiState.breakStartTime
                                )
                            }
                            
                            // Break total
                            InfoRow(
                                label = stringResource(R.string.break_total),
                                value = uiState.breakTotal
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f, fill = !uiState.clockedIn))
            
            // Action buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                if (!uiState.clockedIn) {
                    // Clock in button
                    FilledTonalButton(
                        onClick = { viewModel.clockIn(context) },
                        modifier = Modifier
                            .height(56.dp)
                            .fillMaxWidth()
                            .testTag("clockInButton"),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PlayCircle,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = stringResource(R.string.clock_in),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                } else {
                    // Clock out button
                    FilledTonalButton(
                        onClick = {
                            viewModel.clockOut(context)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    SnackbarVisualsImpl(message = context.getString(R.string.shift_saved))
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .testTag("clockOutButton"),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Text(stringResource(R.string.clock_out))
                    }
                    
                    // Break button
                    FilledTonalButton(
                        onClick = {
                            if (!uiState.onBreak) {
                                viewModel.startBreak(context)
                            } else {
                                viewModel.endBreak(context)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .testTag("breakButton"),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (!uiState.onBreak) 
                                MaterialTheme.colorScheme.secondaryContainer 
                            else 
                                MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = if (!uiState.onBreak) 
                                MaterialTheme.colorScheme.onSecondaryContainer 
                            else 
                                MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = if (!uiState.onBreak) 
                                Icons.Outlined.PauseCircle 
                            else 
                                Icons.Outlined.PlayCircle,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = if (!uiState.onBreak) 
                                stringResource(R.string.break_start) 
                            else 
                                stringResource(R.string.break_end),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // View shifts button
            ElevatedButton(
                onClick = viewShiftsOnClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(56.dp)
                    .testTag("viewShiftsButton"),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = stringResource(R.string.view_shifts),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun CheckPermissions(context: Context) {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            NotificationHandler.createNotificationChannel(context)
        }
    }
    LaunchedEffect(key1 = true) {
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}