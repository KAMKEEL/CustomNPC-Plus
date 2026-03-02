package io.github.frostzie.nodex.events

import io.github.frostzie.nodex.project.Workspace

/**
 * Fired when the workspace configuration changes (project added/removed).
 */
data class WorkspaceUpdated(val workspace: Workspace)

/**
 * Event fired to request a complete reset of the workspace and session history.
 */
class ResetWorkspaceEvent

/**
 * Event fired to open the project manager.
 */
class OpenProjectManagerEvent

/**
 * Event fired to open folder of the currently active workspace
 */
class OpenWorkspaceFolder