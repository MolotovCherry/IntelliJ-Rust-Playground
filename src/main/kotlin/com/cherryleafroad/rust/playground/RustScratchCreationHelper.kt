package com.cherryleafroad.rust.playground

import com.cherryleafroad.rust.playground.config.Settings
import com.intellij.ide.scratch.ScratchFileCreationHelper
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.project.Project
import org.rust.lang.RsLanguage

class RustScratchCreationHelper : ScratchFileCreationHelper() {
    override fun prepareText(project: Project, context: Context, dataContext: DataContext): Boolean {
        context.language = RsLanguage
        context.text = Settings.getScratchDefault()
        return true
    }
}
