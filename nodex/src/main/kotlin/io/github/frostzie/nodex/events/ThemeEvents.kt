package io.github.frostzie.nodex.events

import java.nio.file.Path

class ReloadThemeEvent
class ThemeChangeEvent(val themeName: String)
class ImportThemeEvent
class OpenThemeEvent
class ThemeEditingSessionClosedEvent
class SaveFileEvent(val path: Path)