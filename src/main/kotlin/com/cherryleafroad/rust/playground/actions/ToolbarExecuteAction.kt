package com.cherryleafroad.rust.playground.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class ToolbarExecuteAction : AnAction("Run Scratch", "Run scratch in Rust Playground", AllIcons.Actions.Execute) {
    override fun actionPerformed(e: AnActionEvent) {
        ActionTools.actionPerformed(e)
    }
}
