# Rentool

An Android application to extract and create Ren'Py RPA archives and decompile RPYC scripts directly on your device.

## Features

- **Extract RPA Archives**: Unpack Ren'Py game archives to access images, scripts, audio, and other assets
- **Create RPA Archives**: Package files and folders into RPA v3 format archives
- **Decompile RPYC Scripts**: Convert compiled .rpyc files back to readable .rpy source scripts
- **Batch Operations**: Extract multiple RPA files or decompile multiple RPYC files at once
- **Multi-Select Support**: Long-press to select multiple files or folders for batch operations
- **Smart Directory Defaults**: Output directory automatically defaults to the location of selected input files
- **Real-time Progress Tracking**: View extraction/creation/decompilation progress with file counts, speed, and ETA

## Requirements

- **Android 11 or higher**
- **MANAGE_EXTERNAL_STORAGE permission**: Required for direct file system access
- **Storage**: Enough space for extracted/created archives

## Installation

1. Download the latest APK from the [Releases](../../releases) page
2. Enable "Install from Unknown Sources" if needed
3. Install the APK
4. Grant storage permissions when prompted (MANAGE_EXTERNAL_STORAGE)

## Building from Source

### Prerequisites

- Android Studio Arctic Fox or later
- Android SDK with API 34+
- Python 3.8+ (for Chaquopy build)
- Gradle 8.0+

### Build Steps

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/Rentool.git
   cd Rentool
   ```

2. Open the project in Android Studio

3. Sync Gradle files (Android Studio should prompt automatically)

4. Build the APK:
   - Via Android Studio: **Build → Build Bundle(s) / APK(s) → Build APK(s)**
   - Via Command Line:
     ```bash
     ./gradlew assembleDebug
     ```

5. The APK will be located at:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

### Chaquopy Python Configuration

The project uses [Chaquopy](https://chaquo.com/chaquopy/) to integrate Python for RPA archive operations and RPYC decompilation. Chaquopy will automatically detect your Python installation during build.

If auto-detection fails, you can manually specify your Python path in `app/build.gradle`:
```gradle
python {
    buildPython "/path/to/python"
}
```

## Usage

### Extracting RPA Archives

1. Tap the **"Extract RPA"** card
2. Browse and select one or more `.rpa` files:
   - Single tap: Select one file
   - Long-press: Enter multi-select mode to choose multiple files
3. Select the destination folder (defaults to the folder containing the RPA files)
4. Wait for extraction to complete
5. Files will be extracted to the chosen directory (overwrites existing files)

### Creating RPA Archives

1. Tap the **"Create RPA"** card
2. Browse and select files/folders to archive:
   - Single tap: Select one folder
   - Long-press: Enter multi-select mode to choose multiple items
3. Enter a name for the output RPA file (e.g., `archive.rpa`)
4. Wait for creation to complete
5. The RPA file will be created in the parent directory of selected items

### Decompiling RPYC Scripts

1. Tap the **"Decompile RPYC"** card
2. Browse and select `.rpyc` files or folders containing scripts:
   - Single tap: Select individual files or an entire folder (e.g., `/game` folder)
   - Long-press: Enter multi-select mode to choose multiple files/folders
3. Select the destination folder (defaults to the folder containing the RPYC files)
4. Wait for decompilation to complete
5. Decompiled `.rpy` files will be saved to the chosen directory

**Tip**: Selecting a game's `/game` folder is the fastest way to decompile all scripts at once.

## Technical Details

### RPA Format Support

- **RPA Version**: v3 (Ren'Py 7.4+)
- **Format Key**: `0xDEADBEEF` (standard Ren'Py key)
- **Compression**: ZLib

### File Operations

- Uses direct `File` API for maximum performance
- Requires `MANAGE_EXTERNAL_STORAGE` permission on Android 11+
- Files are overwritten without prompting during extraction
- Batch creation uses temporary directory for combining multiple sources

### Progress Tracking

- JSON-based progress file updated in real-time
- Polling interval: 500ms
- Tracks: file count, current file, speed (files/sec), ETA

## Architecture

- **Language**: Java, Python
- **UI Framework**: Material Design 3
- **Python Integration**: Chaquopy
- **RPA Library**: rpatool.py
- **Decompiler**: unrpyc
- **File Picker**: Custom RecyclerView-based picker with multi-select

## Credits

- **RPA Format**: Based on [Ren'Py](https://www.renpy.org/) archive specification
- **Python Integration**: [Chaquopy](https://chaquo.com/chaquopy/)
- **Folder Icons**: [Icons8](https://icons8.com/)
- **Rpatool**: [Shizmob](https://codeberg.org/shiz/rpatool)
- **Unrpyc**: [CensoredUsername](https://github.com/CensoredUsername/unrpyc)

## License

This project is licensed under the GNU General Public License v3.0 - see the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Disclaimer

This tool is intended for educational purposes and for modding/backing up Ren'Py games you own. Respect game developers' intellectual property and terms of service.

## Support

For issues, questions, or feature requests, please open an issue on the [GitHub Issues](../../issues) page.
