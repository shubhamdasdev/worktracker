package com.example.worktracker.ui

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.worktracker.AppViewModelProvider
import com.example.worktracker.R
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiftScreen(
    topBarTitle: String,
    navigateBack: () -> Unit,
    viewModel: ShiftViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var openDialog by rememberSaveable { mutableStateOf(false) }
    var breakText by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = {
            ShiftTopAppBar(
                title = topBarTitle,
                navigateUp = navigateBack
            )
        }
    ) { paddingValues ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Main content card
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
                    // Shift start section
                    ShiftTimeSection(
                        label = stringResource(R.string.shift_start),
                        date = uiState.startDate,
                        time = uiState.startTime,
                        onDateClick = {
                            val (cYear, cMonth, cDayOfMonth) = viewModel.getDatePickerStart()
                            val listener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                                viewModel.updateStartDate(year, month, dayOfMonth)
                                viewModel.updateEndDate(year, month, dayOfMonth)
                                viewModel.updateTotal()
                            }
                            DatePickerDialog(context, listener, cYear, cMonth, cDayOfMonth).show()
                        },
                        onTimeClick = {
                            val time = getHourAndMinute(uiState.startTime)
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    viewModel.updateStartTime(hour, minute)
                                    viewModel.updateTotal()
                                },
                                time.first, time.second, false
                            ).show()
                        },
                        dateTestTag = "startDate",
                        timeTestTag = "startTime"
                    )
                    
                    Divider()
                    
                    // Shift end section
                    ShiftTimeSection(
                        label = stringResource(R.string.shift_end),
                        date = uiState.endDate,
                        time = uiState.endTime,
                        onDateClick = {
                            val (cYear, cMonth, cDayOfMonth) = viewModel.getDatePickerEnd()
                            val listener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                                viewModel.updateEndDate(year, month, dayOfMonth)
                                viewModel.updateTotal()
                            }
                            DatePickerDialog(context, listener, cYear, cMonth, cDayOfMonth).show()
                        },
                        onTimeClick = {
                            val time = getHourAndMinute(uiState.endTime)
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    viewModel.updateEndTime(hour, minute)
                                    viewModel.updateTotal()
                                },
                                time.first, time.second, false
                            ).show()
                        },
                        dateTestTag = "endDate",
                        timeTestTag = "endTime"
                    )
                    
                    Divider()
                    
                    // Break section
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.break_item),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Text(
                            text = uiState.breakTotal,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        
                        FilledTonalButton(
                            onClick = {
                                openDialog = true
                                breakText = ""
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Text(stringResource(R.string.edit))
                        }
                    }
                    
                    Divider()
                    
                    // Shift total section
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.shift_total),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Text(
                            text = uiState.total,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Save button
            Button(
                onClick = {
                    scope.launch {
                        viewModel.insertShift()
                        navigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("saveButton"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = stringResource(R.string.save_button),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Break time dialog
            if (openDialog) {
                AlertDialog(
                    onDismissRequest = { openDialog = false },
                    title = {
                        Text(
                            text = stringResource(R.string.break_in_minutes),
                            style = MaterialTheme.typography.headlineSmall
                        )
                    },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = breakText,
                                onValueChange = { newText ->
                                    breakText = newText.filter { it.isDigit() }
                                },
                                label = { Text(stringResource(R.string.minutes)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("breakTimeInput")
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                openDialog = false
                                viewModel.updateBreakTotal(breakText)
                                viewModel.updateTotal()
                            }
                        ) {
                            Text(stringResource(R.string.ok))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { openDialog = false }
                        ) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ShiftTimeSection(
    label: String,
    date: String,
    time: String,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    dateTestTag: String,
    timeTestTag: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Date button
            OutlinedButton(
                onClick = onDateClick,
                modifier = Modifier.weight(1f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.testTag(dateTestTag)
                )
            }
            
            // Time button
            OutlinedButton(
                onClick = onTimeClick,
                modifier = Modifier.weight(1f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = time,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .weight(1f)
                            .testTag(timeTestTag),
                        textAlign = TextAlign.Center
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiftTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigateUp: () -> Unit = {},
) {
    TopAppBar(
        title = { Text(title) },
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = navigateUp) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }
    )
}

private fun getHourAndMinute(str: String): Pair<Int, Int> {
    val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
    val time = LocalTime.parse(str, formatter)
    return Pair(time.hour, time.minute)
}

@Preview(showBackground = true, widthDp = 384)
@Composable
fun ShiftScreenPreview() {
    val uiState = ShiftUiState(
        startDate  = "Sun, Jan 29",
        startTime  = "10:00 PM",
        endDate    = "Mon, Jan 30",
        endTime    = "1:00 PM",
        breakTotal = "---",
        total      = "8:30"
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 15.dp)
    ) {
        Spacer(Modifier.padding(80.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
        ) {
            Text(
                text = "Shift Start:",
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
            TextButton(
                onClick = {},
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = uiState.startDate,
                    fontSize = 15.sp
                )
            }
            TextButton(
                onClick = {},
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween//********
                ) {
                    Text(
                        text = uiState.startTime,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Select start time")
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
        ) {
            Text(
                text = "Shift End:",
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
            TextButton(
                onClick = {},
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = uiState.endDate,
                    fontSize = 15.sp
                )
            }
            TextButton(
                onClick = {},
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween//********
                ) {
                    Text(
                        text = uiState.endTime,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Select end time")
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
        ) {
            Text(
                text = "Break:",
                modifier = Modifier.weight(1f)
            )
            Text(
                text = uiState.breakTotal,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            TextButton(
                onClick = {},
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween//********
                ) {
                    Text(
                        text = "",
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Select end time")
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
        ) {
            Text(
                text = "Total:",
                modifier = Modifier.weight(1f)
            )
            Text(
                text = uiState.total,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.padding(vertical = 40.dp))
        Button(onClick = {  }) {
            Text(
                text = "Save",
                fontSize = 15.sp
            )
        }
    }
}