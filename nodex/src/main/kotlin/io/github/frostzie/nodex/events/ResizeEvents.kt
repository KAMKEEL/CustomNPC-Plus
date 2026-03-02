package io.github.frostzie.nodex.events

/**
 * Event for WindowControls
 */
//TODO: check usage of each event and if they are still needed
class MainWindowClose
class MainWindowMinimize
class MainWindowMaximize
class MainWindowToggleMaximize
class MainWindowRestore

data class MainWindowMaximizedStateChanged(val isMaximized: Boolean)