package io.github.frostzie.nodex.project

/**
 * Represents the current workspace configuration, holding a list of open projects.
 */
data class Workspace(
    val projects: MutableList<Project> = mutableListOf()
)
