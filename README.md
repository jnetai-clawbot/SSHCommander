# SSH Commander

**A polished SSH dashboard for managing Linux servers on Android.**

[![Build Debug APK](https://github.com/jnetaol/SSHCommander/actions/workflows/build-debug.yml/badge.svg)](https://github.com/jnetaol/SSHCommander/actions/workflows/build-debug.yml)
[![Build Release APK](https://github.com/jnetaol/SSHCommander/actions/workflows/build-release.yml/badge.svg)](https://github.com/jnetaol/SSHCommander/actions/workflows/build-release.yml)

## Features

- **One-tap SSH Connect** - Quick connection to your Linux servers
- **Saved Commands** - Store and quickly execute frequent commands
- **Live System Stats** - Real-time CPU, memory, disk, and load monitoring
- **Docker Controls** - View and manage Docker containers
- **SFTP Browser** - Browse remote filesystems
- **Terminal Tabs** - Interactive terminal with command history

## Requirements

- Android 10+ (API 29+)
- ARM64 device
- SSH server with password authentication

## Installation

Download the latest APK from [Releases](https://github.com/jnetaol/SSHCommander/releases).

## Building

```bash
./gradlew assembleDebug    # Debug build
./gradlew assembleRelease  # Release build (requires keystore)
```

## Tech Stack

- Kotlin 1.9.22, Jetpack Compose
- Room Database
- Material Design 3 (Dark Theme)
- Gradle Kotlin DSL, AGP 8.2.2

## License

MIT License - see [LICENSE](LICENSE)

---

**Made By [jnetai.com](https://jnetai.com)**
