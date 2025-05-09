# File Organizer

An Android application that automatically organizes files based on customizable rules and conditions.

## Overview

File Organizer is a powerful Android app designed to help users keep their device storage organized by automatically moving files to designated folders based on customizable rules. The app uses a rule-based system that can match files based on various criteria such as file type, name pattern, date, and size.

## Features

- **Rule-Based File Organization**: Create custom rules to automatically organize files
- **Multiple Condition Types**:
  - File type/extension matching
  - Filename pattern matching
  - File date conditions
  - File size conditions
- **Logical Operators**: Combine multiple conditions using AND/OR logic
- **Scheduled Execution**: Schedule rules to run at specific times:
  - Daily
  - Weekly
  - Monthly
  - Custom intervals
- **Rule Management**: Enable/disable rules, edit existing rules, or delete rules
- **User-Friendly Interface**: Modern Material Design UI built with Jetpack Compose
- **Folder Selection**: Intuitive folder picker for selecting source and destination directories
- **Background Processing**: Rules can run in the background even when the app is closed
- **Notification Support**: Get notified when file operations complete

## Screenshots

[Screenshots will be added here]

## Installation

### Requirements
- Android 8.0 (API level 26) or higher
- Storage permissions

### Download
- Download the latest APK from the [Releases](https://github.com/username/file_organizer/releases) page
- Or install from Google Play Store [link to be added]

## Usage

### Getting Started

1. **Grant Permissions**: When first launching the app, grant the necessary storage permissions
2. **Create a Rule**:
   - Tap the "+" button to create a new rule
   - Enter a name for the rule
   - Select a destination folder where files will be moved
   - Add conditions to match files (file type, name pattern, date, size)
   - Choose a logical operator (AND/OR) if using multiple conditions
   - Save the rule

3. **Manage Rules**:
   - Enable/disable rules using the toggle switch
   - Edit rules by tapping on them
   - Delete rules by using the delete button
   - Schedule rules to run automatically

4. **Run Rules**:
   - Rules can be run manually by tapping the "Run" button
   - Or set up schedules for automatic execution

### Advanced Features

- **Scheduling**: Set rules to run daily, weekly, monthly, or at custom intervals
- **Condition Combinations**: Create complex rules by combining multiple conditions with AND/OR logic
- **File Preview**: Browse and preview files before organizing them

## Project Structure

The application follows a modern Android architecture:

- **UI Layer**: Jetpack Compose UI components
- **ViewModel Layer**: Manages UI state and business logic
- **Service Layer**: Handles file operations and rule execution
- **Repository Layer**: Manages data access and persistence
- **Model Layer**: Defines data structures for rules and conditions

## Dependencies

- Jetpack Compose for UI
- Room for database storage
- Koin for dependency injection
- Kotlin Coroutines for asynchronous operations
- Android Storage Access Framework for file operations

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- TutorialsAndroid/FilePicker for the folder selection implementation
- Material Design guidelines for UI inspiration
