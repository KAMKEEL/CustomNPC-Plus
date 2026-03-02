package io.github.frostzie.nodex.events

import java.nio.file.Path

class ChooseDirectory
data class DirectorySelected(val directoryPath: Path)

class NewFile
data class MoveFile(val sourcePath: Path, val targetPath: Path)
data class RequestMoveConfirmation(val sourcePath: Path, val targetPath: Path)
data class RequestFileOverride(val sourcePath: Path, val targetPath: Path)

class CopyFile
class CutFile
class PasteFile

class CopyPath
class OpenWith

class SaveFile
class SaveAsFile
class SaveAllFiles

data class OpenFile(val path: Path)


data class FileMoved(val oldPath: Path, val newPath: Path)
data class FileModified(val path: Path)
data class FileDeleted(val path: Path)
data class FileOverrideConfirmed(val sourcePath: Path, val targetPath: Path)