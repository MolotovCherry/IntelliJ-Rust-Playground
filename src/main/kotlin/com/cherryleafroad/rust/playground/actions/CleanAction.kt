package com.cherryleafroad.rust.playground.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import icons.VcsCodeReviewIcons

class CleanAction : DumbAwareAction("Clean", "Rebuild the Cargo project without the cache from previous run", VcsCodeReviewIcons.DeleteHovered) {
    override fun actionPerformed(e: AnActionEvent) {
        // empty
    }
}