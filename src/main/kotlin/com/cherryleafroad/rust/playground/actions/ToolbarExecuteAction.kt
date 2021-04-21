package com.cherryleafroad.rust.playground.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction

class ToolbarExecuteAction : DumbAwareAction("Run Scratch", "Run scratch in Rust Playground", AllIcons.Actions.Execute) {
    override fun actionPerformed(e: AnActionEvent) {
        ActionTools.actionPerformed(e)
    }
}
