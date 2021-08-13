package com.cherryleafroad.rust.playground.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction

class CleanAction : DumbAwareAction("Clean", "Rebuild the Cargo project without the cache from previous run", AllIcons.Actions.GC) {
    override fun actionPerformed(e: AnActionEvent) {
        ActionTools.actionPerformed(e, true)
    }
}
