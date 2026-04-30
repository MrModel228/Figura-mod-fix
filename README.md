# Figura-Fix v1.0.1

Client-side optimization and GitHub sync for Figura Minecraft mod

## Features

✅ **Auto-Backend Sync**: Syncs avatars with GitHub repository on game start/exit  
✅ **Real-Time Delta Sync**: Automatically detects and uploads file changes  
✅ **Delta Sync**: Only sends changed files using SHA256 hash comparison  
✅ **Compact Storage**: Optimized JSON format for smaller avatar sizes  
✅ **No Feature Disable**: All Figura functionality preserved  
✅ **Lightweight**: ~22KB jar size

## GitHub Repositories

- **Source Code**: https://github.com/MrModel228/Figura-mod-fix
- **Models Store**: https://github.com/MrModel228/figura-fix-models

## Download

Download from: https://github.com/MrModel228/figura-fix/releases

## Installation

1. Copy `figura-fix-1.0.1.jar` to your `mods/` folder
2. Launch Minecraft with Fabric Loader 0.18.4+
3. Mod will auto-sync on startup and exit
4. **File changes are detected every 5 seconds and auto-synced to GitHub**

## Automatic Sync

- **On Start**: Automatically syncs all local avatars to GitHub
- **On Exit**: Automatically syncs changes from GitHub
- **Real-Time**: File watcher checks for changes every 5 seconds

## File Watcher

The mod includes a real-time file watcher that:
- Scans `config/figura/avatars/` every 5 seconds
- Compares SHA256 hashes of each `model.json` file
- Automatically uploads new or modified avatars
- Tracks local file states to prevent duplicate uploads

## Configuration

Config file: `config/figurafix/config.properties`

```properties
# GitHub repository owner
owner=MrModel228
# Repository name
repo=figura-fix-models
# Enable sync on game start
autoStart=true
# Enable sync on game exit
autoExit=true
```

## File Structure

```
config/figura/avatars/
├── {playerName}/
│   └── {avatarName}/
│       ├── model.json
│       └── textures/
```

Models are stored as: `avatars/{player}/{avatar}/model.json`

## GitHub API

- **Authentication**: Personal Access Token (stored in `token.dat`)
- **Storage**: Raw GitHub files
- **API**: REST API v3
- **Commit**: Auto-generated commit messages

## Building

```bash
./gradlew build
```

Output: `build/libs/figura-fix-{version}.jar`

## Changelog

### v1.0.1
- Fixed entrypoint error
- Added auto-start/exit sync
- Implemented delta sync
- Compact JSON compression
- **Real-time file watcher with auto-sync on changes**
- English & Russian localization

### v1.0.0
- Initial release
- GitHub API client
- Avatar manager
- Configuration system

## License

MIT License

## Author

MrModel228

## Thanks

- FiguraMC for the amazing mod
- Fabric team for the API
