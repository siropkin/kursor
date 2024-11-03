# Kursor Changelog

## [Unreleased]


## [1.4.0] - 2024-11-... (TBD)
### Changed
- #18: Added support for "Squirrel Method" (Chinese) (https://rime.im) on macOS.
- #20: Added support for "Russian - PC" on macOS.
- #21: Fixed color settings save bug; color settings now save correctly.


## [1.3.1] - 2024-08-26
### Changed
- Fix NullPointerException for Windows users.


## [1.3.0] - 2024-07-31
### Changed
- Add support of Sogou Pinyin Method (Chinese) for macOS.

### For Contributors and Developers
- Migrate from Gradle IntelliJ Plugin 1.x to 2.0.


## [1.2.0] - 2024-07-29
### Changed
- Plugin settings moved to Tools: `File` > `Settings` > `Tools` > `Kursor`.
- Fix German keyboard layout abbreviation for Windows.


## [1.1.1] - 2024-07-25
### Changed
- Improved keyboard layout detection for Windows users, including detection of different input methods within the same language. Previously, Kursor would not detect different input methods within one language, but now it does.


## [1.1.0] - 2024-07-19
### Changed
- Improved keyboard layout detection across different operating systems, ensuring more accurate language tracking.
- Transitioned the "Default Language" setting from a ComboBox component to an Input component, allowing for custom default language input and enhancing flexibility beyond pre-filled options.
- Added "Detect Keyboard Layout" button to the settings page, enabling users to manually detect their keyboard layout and update the default language accordingly.

### For Contributors and Developers
- Updated Gradle to version 8.9 to align with the latest development practices and ensure compatibility.


## [1.0.5] - 2023-12-07
### Changed
- Unspecify `pluginUntilBuild` (include all future builds)


## [1.0.4] - 2023-10-25
### Changed
- Improve MacOS support


## [1.0.3] - 2023-07-28
### Changed
- Improve Linux support


## [1.0.2] - 2023-07-27
### Changed
- Update `pluginUntilBuild` to include `232.*` (2023.2.*)


## [1.0.1] - 2023-07-13
### Added
- Added Linux support
### Changed
- Turning "Show default language" off by default


## [1.0.0] - 2023-06-16
### Added
- Initial release
