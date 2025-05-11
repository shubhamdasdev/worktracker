package com.example.worktracker.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.worktracker.Constants
import com.example.worktracker.R
import com.example.worktracker.network.WeatherData
import kotlinx.coroutines.flow.StateFlow

/**
 * Weather widget to display current weather conditions.
 */
@Composable
fun WeatherWidget(
    weatherEnabled: Boolean,
    weatherState: StateFlow<WeatherState>,
    onRequestPermission: () -> Unit,
    onRefreshWeather: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!weatherEnabled) return
    
    val context = LocalContext.current
    val weatherUiState by weatherState.collectAsState()
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onRefreshWeather()
        }
    }
    
    LaunchedEffect(weatherEnabled) {
        if (weatherEnabled) {
            checkLocationPermission(context, permissionLauncher, onRequestPermission)
        }
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        when (weatherUiState) {
            is WeatherState.Loading -> {
                WeatherLoadingContent()
            }
            is WeatherState.Success -> {
                val weatherData = (weatherUiState as WeatherState.Success).data
                WeatherContent(weatherData)
            }
            is WeatherState.Error -> {
                val errorMessage = (weatherUiState as WeatherState.Error).message
                WeatherErrorContent(errorMessage) {
                    onRefreshWeather()
                }
            }
            is WeatherState.PermissionRequired -> {
                WeatherPermissionContent {
                    permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                }
            }
        }
    }
}

@Composable
private fun WeatherLoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp
        )
        Text(
            text = stringResource(R.string.weather_loading),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 36.dp)
        )
    }
}

@Composable
private fun WeatherContent(weatherData: WeatherData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(
                R.string.weather_temperature,
                weatherData.temperature.toString()
            ),
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(
                R.string.weather_description,
                weatherData.description
            ),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(
                R.string.weather_location,
                weatherData.location
            ),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun WeatherErrorContent(errorMessage: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onRetry) {
            Text(text = "Retry")
        }
    }
}

@Composable
private fun WeatherPermissionContent(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.weather_permission_required),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onRequestPermission) {
            Text(text = "Grant Permission")
        }
    }
}

private fun checkLocationPermission(
    context: Context,
    permissionLauncher: ActivityResultLauncher<String>,
    onRequestPermission: () -> Unit
) {
    when {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED -> {
            onRequestPermission()
        }
        else -> {
            permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }
}

/**
 * State for the weather UI.
 */
sealed class WeatherState {
    object Loading : WeatherState()
    data class Success(val data: WeatherData) : WeatherState()
    data class Error(val message: String) : WeatherState()
    object PermissionRequired : WeatherState()
}
