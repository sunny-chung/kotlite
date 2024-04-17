# Kotlite Interpreter Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- `AnyType` property to `SymbolTable` for API consistence
- `String.toDataType` convenient extension function
- Property `index` to the class `SourcePosition`
- Property `endExclusive` to the class `Token`
- Function `Lexer.currentMode`
- Optional constructor parameter `isParseComment` to `Lexer`. If it is `true`, `Token` instances would be created for comments. Default is `false`.

### Changed

- New enum entry `Comment` to the enum class `TokenType`

### Fixed

- Lambda arguments cannot be accessed if the lambda is used as a function value parameter which is used as another function value parameter, e.g. `func(list.first { it >= 7 })`
- Incorrect nullable resolution of extension properties
- `IntRange.joinToString` yields incorrect string value
- Function signature of `MutableMapValue`

## [1.0.0] - 2024-04-08

### Added

- First release
