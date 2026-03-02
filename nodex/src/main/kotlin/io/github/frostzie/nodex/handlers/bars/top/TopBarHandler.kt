package io.github.frostzie.nodex.handlers.bars.top

import io.github.frostzie.nodex.events.*
import io.github.frostzie.nodex.modules.bars.top.TopBarViewModel
import io.github.frostzie.nodex.settings.annotations.SubscribeEvent
import io.github.frostzie.nodex.utils.OpenLinks

@Suppress("unused")
class TopBarHandler(private val topBarViewModel: TopBarViewModel) {

    @SubscribeEvent
    fun onMinimize(event: MainWindowMinimize) {
        topBarViewModel.minimize()
    }

    @SubscribeEvent
    fun onMaximize(event: MainWindowMaximize) {
        topBarViewModel.maximize()
    }

    @SubscribeEvent
    fun onToggleMaximize(event: MainWindowToggleMaximize) {
        topBarViewModel.toggleMaximize()
    }

    @SubscribeEvent
    fun onRestoreBack(event: MainWindowRestore) {
        topBarViewModel.restore()
    }

    @SubscribeEvent
    fun onClose(event: MainWindowClose) {
        topBarViewModel.hideWindow()
    }

    @SubscribeEvent
    fun openDatapackFolder(event: OpenWorkspaceFolder) {
        topBarViewModel.openWorkspaceFolder()
    }

    @SubscribeEvent
    fun onDiscordLink(event: DiscordLink) {
        OpenLinks.discordLink()
    }

    @SubscribeEvent
    fun onGitHubLink(event: GitHubLink) {
        OpenLinks.gitHubLink()
    }

    @SubscribeEvent
    fun onReportBugLink(event: ReportBugLink) {
        OpenLinks.reportBugLink()
    }
}