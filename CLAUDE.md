# Kursor - Claude Code Instructions

## Project Overview

Kursor is an IntelliJ IDEA plugin that displays keyboard layout indicator near the cursor.

## Common Tasks

### Adding Support for a New IntelliJ IDEA Version

When asked to "add support for IntelliJ IDEA 20XX.X":

1. **Update `gradle.properties`:**
   - Bump `pluginVersion` (increment patch version, e.g., 1.4.7 → 1.4.8)
   - Update `pluginUntilBuild` to match new version's build number:
     - 2025.1 = 251.*
     - 2025.2 = 252.*
     - 2025.3 = 253.*
     - 2026.1 = 261.*
     - Pattern: `20XX.Y` → `XXY.*`

2. **Update `CHANGELOG.md`:**
   - Add new version section after `## [Unreleased]`:
   ```markdown
   ## [X.X.X]

   ### Changed

   - Add support for IntelliJ IDEA 20XX.X.
   ```

3. **Verify:**
   - Run `./gradlew buildPlugin`
   - Check artifact exists in `build/distributions/`

## Build Commands

- Build plugin: `./gradlew buildPlugin`
- Run plugin verifier: `./gradlew runPluginVerifier`
- Run IDE with plugin: `./gradlew runIde`
