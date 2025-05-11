# Work Tracker - App Details

## Overview

Work Tracker is a comprehensive and user-friendly Android application designed to help users keep accurate records of their work hours. Built with Jetpack Compose and following modern Android development practices, the app provides an intuitive interface for tracking work shifts, breaks, and generating detailed reports.

## Key Features

### 1. Time Tracking
- **Clock In/Out**: Users can easily clock in at the beginning of their shift and clock out when they finish.
- **Break Tracking**: The app allows users to log breaks during their shifts, which are automatically deducted from the total work time.
- **Real-time Counter**: When clocked in, a real-time counter displays the current shift duration.

### 2. Shift Management
- **Shift Creation**: Users can manually create shifts with custom start and end times.
- **Shift Editing**: Existing shifts can be modified if corrections are needed.
- **Break Duration**: Break times are recorded and factored into the total shift duration.

### 3. Reporting and Analysis
- **Shift History**: View a complete history of all recorded shifts.
- **Time Period Filtering**: Filter shifts by week, month, year, or view all shifts.
- **Total Hours Calculation**: The app automatically calculates total hours worked for the selected time period.
- **CSV Export**: Export shift data to CSV format for use in spreadsheets or other applications.

### 4. User Preferences
- **Time Zone Support**: The app supports different time zones, allowing users to accurately track work hours regardless of location.
- **Notification System**: Receive notifications about active shifts and breaks.

## How It Works

### Main Screen
The main screen serves as the central hub for tracking your current shift:

1. **Clock In Button**: Tap to start tracking a new shift.
2. **Clock Out Button**: Tap to end your current shift and save it to your history.
3. **Break Button**: Tap to start or end a break during your shift.
4. **Shift Timer**: Displays the current duration of your shift (minus break time).
5. **Menu Options**: Access settings and export functionality.

### Shift History Screen
The shift history screen provides a comprehensive view of your work records:

1. **Shift List**: Displays all shifts with date, time span, break duration, and total hours.
2. **Time Period Navigation**: Switch between viewing shifts by week, month, year, or all time.
3. **Total Hours**: Shows the cumulative hours worked for the selected time period.
4. **Add Shift Button**: Manually add a new shift to your records.

### Shift Creation/Editing Screen
This screen allows you to create or edit shift details:

1. **Date Selection**: Choose the start and end dates for your shift.
2. **Time Selection**: Set the precise start and end times.
3. **Break Duration**: Enter the total break time to be deducted from the shift.
4. **Total Calculation**: The app automatically calculates the total shift duration.

### Settings Screen
The settings screen allows you to customize the app's behavior:

1. **Time Zone Selection**: Choose your preferred time zone for accurate time tracking.
2. **Notification Settings**: Configure how and when you receive notifications about your shifts.

## Technical Implementation

The app is built using:

- **Kotlin**: Modern programming language for Android development.
- **Jetpack Compose**: UI toolkit for building native Android interfaces.
- **Room Database**: Local database for storing shift information.
- **MVVM Architecture**: Separation of concerns with ViewModels managing UI state.
- **Coroutines**: For handling asynchronous operations.
- **SharedPreferences**: For storing user preferences and active shift information.

## Data Storage

The app stores shift data in a local Room database with the following structure:

- **Shift Entity**:
  - `id`: Unique identifier for each shift
  - `date`: The date of the shift
  - `shiftSpan`: The start and end times of the shift
  - `breakTotal`: Total break time during the shift
  - `shiftTotal`: Total duration of the shift (excluding breaks)

## Use Cases

### For Hourly Workers
- Track exact hours worked for accurate payment
- Ensure breaks are properly accounted for
- Generate reports for submission to employers

### For Freelancers
- Track billable hours for different clients
- Export time records for invoicing purposes
- Analyze work patterns to optimize productivity

### For Remote Workers
- Maintain consistent work schedules across different time zones
- Document work hours for employers
- Balance work and personal time effectively

## Benefits

1. **Accuracy**: Eliminate human error in time tracking with automated calculations.
2. **Convenience**: Clock in and out with a single tap, anywhere you are.
3. **Transparency**: Maintain clear records of all work hours and breaks.
4. **Accountability**: Generate reports to demonstrate work completion.
5. **Time Management**: Analyze work patterns to improve productivity.

## Conclusion

Work Tracker simplifies the process of tracking work hours, making it easy to maintain accurate records without the hassle of manual timesheets. Whether you're clocking in at the office or logging hours from home, the app provides a reliable solution for all your time-tracking needs.
