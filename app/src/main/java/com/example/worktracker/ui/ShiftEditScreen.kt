package com.example.worktracker.ui

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.worktracker.AppViewModelProvider
import com.example.worktracker.R
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object ShiftEditDestination {
    const val route = "shift_edit"
    const val shiftIdArg = "shiftId"
    const val routeWithArgs = "$route/{$shiftIdArg}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiftEditScreen(
    topBarTitle: String,
    navigateBack: () -> Unit,
    viewModel: ShiftEditViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var openBreakDialog by rememberSaveable { mutableStateOf(false) }
    var breakText by rememberSaveable { mutableStateOf("") }

    var openDeleteDialog by rememberSaveable { mutableStateOf(false) }

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
                            val listener = OnDateSetListener { _, year, month, dayOfMonth ->
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
                            val listener = OnDateSetListener { _, year, month, dayOfMonth ->
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
                                openBreakDialog = true
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
            
            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Save button
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.updateShift()
                            navigateBack()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .testTag("saveButton"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = stringResource(R.string.save),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                
                // Delete button
                FilledTonalButton(
                    onClick = { openDeleteDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .testTag("deleteButton"),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text(
                        text = stringResource(R.string.delete),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Break time dialog
            if (openBreakDialog) {
                AlertDialog(
                    onDismissRequest = { openBreakDialog = false },
                    title = {
                        Text(
                            text = stringResource(R.string.break_time),
                            style = MaterialTheme.typography.headlineSmall
                        )
                    },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = breakText,
                                onValueChange = { breakText = it },
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
                                viewModel.updateBreakTotal(breakText)
                                viewModel.updateTotal()
                                openBreakDialog = false
                            }
                        ) {
                            Text(stringResource(R.string.ok))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { openBreakDialog = false }
                        ) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }

            if(openDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { openDeleteDialog = false },
                    title = {
                        Text(text = stringResource(R.string.delete_shift_dialog))
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                openDeleteDialog = false
                                scope.launch {
                                    viewModel.deleteShift()
                                    navigateBack()
                                }
                            }
                        ) {
                            Text(stringResource(R.string.confirm))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { openDeleteDialog = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }
        }
    }
}

private fun getHourAndMinute(str: String): Pair<Int, Int> {
    val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
    val time = LocalTime.parse(str, formatter)
    return Pair(time.hour, time.minute)
}