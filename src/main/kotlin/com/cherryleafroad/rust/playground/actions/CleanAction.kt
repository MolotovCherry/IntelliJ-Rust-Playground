package com.cherryleafroad.rust.playground.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import icons.VcsCodeReviewIcons

class CleanAction : AnAction("Clean", "Rebuild the Cargo project without the cache from previous run", VcsCodeReviewIcons.DeleteHovered) {
    override fun actionPerformed(e: AnActionEvent) {
        // empty
    }
}