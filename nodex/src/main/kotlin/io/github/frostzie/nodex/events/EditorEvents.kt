package io.github.frostzie.nodex.events

import java.nio.file.Path

class EditorCopy
class EditorCut
class EditorPaste
class EditorUndo
class EditorRedo
class EditorSelectAll
class EditorCloseTab
class EditorFind

data class EditorCursorPosition(val line: Int, val column: Int)
class ActiveTabChangedEvent(val path: Path?)