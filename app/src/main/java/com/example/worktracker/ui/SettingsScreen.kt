package com.example.worktracker.ui


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.worktracker.AppViewModelProvider
import com.example.worktracker.R
import com.example.worktracker.TimeZoneInfo
import com.example.worktracker.TimeZoneInfo.letterToIndexMap
import com.example.worktracker.TimeZoneInfo.timeZoneList
import com.example.worktracker.ui.theme.WorkTrackerAnimations
import com.example.worktracker.ui.theme.LocalIsDarkMode
import com.example.worktracker.ui.theme.blue1
import com.example.worktracker.ui.theme.blue2
import com.example.worktracker.ui.theme.blue3
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SettingsScreen(
    navigateBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.settings),
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Settings sections in a card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Time zone settings
                    TimeZoneSettings(
                        selectedItem = uiState.timeZoneDisplay,
                        onItemSelected = { timeZone ->
                            viewModel.updateTimeZone(timeZone)
                        }
                    )
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Week settings
                    WeekSettings(
                        selectedItem = uiState.startOfWeek,
                        onItemSelected = { startOfWeek ->
                            viewModel.updateStartOfWeek(startOfWeek)
                        }
                    )
                    
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    // Weather settings
                    WeatherSettings(
                        weatherEnabled = uiState.weatherEnabled,
                        onWeatherEnabledChanged = { enabled ->
                            viewModel.updateWeatherEnabled(enabled)
                        }
                    )
                }
            }
            
            // About section
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "About",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = "WorkTracker helps you track your work hours and breaks easily.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = "Version 1.1",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun TimeZoneSettings(
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {
    val showDialog = remember { mutableStateOf(false) }

    SettingsItem(
        settingName = stringResource(R.string.time_zone_setting),
        selectedItem = selectedItem,
        onClick = { showDialog.value = true },
    )

    TimeZoneSelectionDialog(
        showDialog = showDialog.value,
        onDismissRequest = { showDialog.value = false },
        onTimeZoneSelected = onItemSelected
    )
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TimeZoneSelectionDialog(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    onTimeZoneSelected: (String) -> Unit
) {
    if (showDialog) {
        val searchQuery = remember { mutableStateOf("") }
        val listState = rememberLazyListState()
        
        // Hardcoded timezone data that will be immediately available as fallback
        val fallbackTimezones = remember {
            listOf(
                Triple("America/New_York", "New York / USA", "GMT-5"),
                Triple("Europe/London", "London / UK", "GMT+0"),
                Triple("Asia/Tokyo", "Tokyo / Japan", "GMT+9"),
                Triple("Australia/Sydney", "Sydney / Australia", "GMT+10"),
                Triple("Europe/Paris", "Paris / France", "GMT+1"),
                Triple("Europe/Berlin", "Berlin / Germany", "GMT+1"),
                Triple("Asia/Shanghai", "Shanghai / China", "GMT+8"),
                Triple("America/Los_Angeles", "Los Angeles / USA", "GMT-8"),
                Triple("America/Chicago", "Chicago / USA", "GMT-6"),
                Triple("Asia/Dubai", "Dubai / UAE", "GMT+4"),
                Triple("Europe/Rome", "Rome / Italy", "GMT+1"),
                Triple("America/Toronto", "Toronto / Canada", "GMT-5"),
                Triple("Pacific/Auckland", "Auckland / New Zealand", "GMT+12"),
                Triple("Asia/Singapore", "Singapore", "GMT+8"),
                Triple("Africa/Cairo", "Cairo / Egypt", "GMT+2"),
                Triple("Europe/Madrid", "Madrid / Spain", "GMT+1"),
                Triple("Africa/Johannesburg", "Johannesburg / South Africa", "GMT+2"),
                Triple("America/Mexico_City", "Mexico City / Mexico", "GMT-6"),
                Triple("Australia/Melbourne", "Melbourne / Australia", "GMT+10"),
                Triple("Etc/UTC", "UTC (Coordinated Universal Time)", "GMT+0")
            )
        }
        
        // Try to load actual timezone data in the background
        LaunchedEffect(Unit) {
            if (timeZoneList.isEmpty()) {
                TimeZoneInfo.fetchTimeZones()
            }
        }
        
        // Use API data if available, otherwise use fallback
        val timezones = if (timeZoneList.isNotEmpty()) timeZoneList else fallbackTimezones

        Dialog(
            onDismissRequest = onDismissRequest,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("TimeZoneSelectionDialog"),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    SearchBar(
                        onDismissRequest = onDismissRequest,
                        searchQuery = searchQuery
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        // Direct implementation of timezone list
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = listState,
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            val filteredList = timezones.filter { 
                                it.second.contains(searchQuery.value, ignoreCase = true) 
                            }
                            
                            if (filteredList.isEmpty() && searchQuery.value.isNotEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No matching timezones found",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                items(filteredList) { timeZone ->
                                    TimeZoneItem(
                                        timeZoneText = timeZone.second,
                                        offsetText = timeZone.third,
                                        onItemSelected = {
                                            onTimeZoneSelected(timeZone.first)
                                            onDismissRequest()
                                        }
                                    )
                                }
                            }
                        }
                        
                        // Alphabet navigation removed to fix compilation issues
                        
                        // Display a scroll handle instead
                        ScrollToTopButton(listState, Modifier.align(Alignment.CenterEnd))
                        ScrollToTopButton(listState, Modifier.align(Alignment.BottomCenter))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    onDismissRequest: () -> Unit,
    searchQuery: MutableState<String>
) {
    CenterAlignedTopAppBar(
        title = { Text(text = stringResource(R.string.time_zone_setting)) },
        navigationIcon = {
            IconButton(onClick = onDismissRequest) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.time_zone_select_back),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
    
    // Search field
    @OptIn(ExperimentalMaterial3Api::class)
    androidx.compose.material3.SearchBar(
        query = searchQuery.value,
        onQueryChange = { searchQuery.value = it },
        onSearch = { },
        active = false,
        onActiveChange = { },
        placeholder = { Text(stringResource(R.string.search)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (searchQuery.value.isNotEmpty()) {
                IconButton(onClick = { searchQuery.value = "" }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.clear_text)
                    )
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = SearchBarDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            inputFieldColors = TextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    ) { }
}

// TimeZoneLazyColumn function removed as its functionality is now directly in TimeZoneSelectionDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeZoneItem(
    modifier: Modifier = Modifier,
    timeZoneText: String,
    offsetText: String,
    onItemSelected: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        onClick = onItemSelected
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Location icon
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 16.dp)
            )
            
            // Text content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = timeZoneText,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = offsetText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Code fragment removed to fix compilation error

// SelectedLetterDisplay function removed to fix compilation issues

@Composable
fun ScrollToTopButton(
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val showButton by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0
        }
    }

    AnimatedVisibility(
        visible = showButton,
        enter = WorkTrackerAnimations.fadeIn + WorkTrackerAnimations.springInFromBottom,
        exit = WorkTrackerAnimations.fadeOut + WorkTrackerAnimations.springOutToBottom,
        modifier = modifier.padding(bottom = 16.dp)
    ) {
        FloatingActionButton(
            onClick = {
                scope.launch {
                    listState.animateScrollToItem(0)
                }
            },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            elevation = FloatingActionButtonDefaults.elevation(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowUp,
                contentDescription = stringResource(R.string.go_to_top)
            )
        }
    }
}

@Composable
fun WeekSettings(
    selectedItem: String,
    onItemSelected: (String) -> Unit,
) {
    val showDialog = remember { mutableStateOf(false) }

    SettingsItem(
        settingName = stringResource(R.string.start_of_week_setting),
        selectedItem = selectedItem,
        onClick = { showDialog.value = true },
    )

    WeekSelectionDialog(
        showDialog = showDialog.value,
        onDismissRequest = { showDialog.value = false },
        onStartOfWeekSelected = onItemSelected
    )
}

@Composable
fun WeekSelectionDialog(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    onStartOfWeekSelected: (String) -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = { Text(text = stringResource(R.string.select_start_of_week)) },
            text = {
                Column {
                    WeekItem(day = stringResource(R.string.saturday), onDismissRequest, onStartOfWeekSelected)
                    Divider(color = Color.Black)
                    WeekItem(day = stringResource(R.string.sunday), onDismissRequest, onStartOfWeekSelected)
                    Divider(color = Color.Black)
                    WeekItem(day = stringResource(R.string.monday), onDismissRequest, onStartOfWeekSelected)
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
fun WeekItem(
    day: String,
    onDismissRequest: () -> Unit,
    onStartOfWeekSelected: (String) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Release -> {
                    delay(100)
                    onDismissRequest()
                }
                else -> {}
            }
        }
    }
    Box(
        modifier = Modifier
            .height(50.dp)
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(),
                onClick = { onStartOfWeekSelected(day.uppercase()) }
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = day,
            fontSize = 20.sp
        )
    }
}

@Composable
fun SettingsItem(
    settingName: String,
    selectedItem: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = settingName,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = selectedItem,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun WeatherSettings(
    weatherEnabled: Boolean,
    onWeatherEnabledChanged: (Boolean) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(
            text = stringResource(R.string.weather_settings),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.enable_weather),
                style = MaterialTheme.typography.bodyLarge
            )
            Switch(
                checked = weatherEnabled,
                onCheckedChange = onWeatherEnabledChanged
            )
        }
        
        if (weatherEnabled) {
            Text(
                text = stringResource(R.string.weather_info),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}