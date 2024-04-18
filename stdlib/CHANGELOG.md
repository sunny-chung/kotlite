# Kotlite Stdlib Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- UUID library module
- Global function `setKotliteStdlibLogMinLevel(severity: Severity)`

### Removed

- `MultipleLibModules` -- it is moved into the kotlite-interpreter library

### Fixed

- Logger is no longer a shared instance but a dedicated one

## [1.0.0] - 2024-04-08

### Added

- First release
