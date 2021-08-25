package com.cherryleafroad.rust.playground.scratch

import com.cherryleafroad.rust.playground.services.Settings
import com.intellij.ide.scratch.ScratchFileCreationHelper
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.project.Project
import org.rust.lang.RsFileType
import org.rust.lang.RsLanguage

class RustScratchCreationHelper : ScratchFileCreationHelper() {
    override fun prepareText(project: Project, context: Context, dataContext: DataContext): Boolean {
        val settings = Settings.getInstance().plugin
        context.language = RsLanguage
        context.text = settings.scratchText
        context.fileExtension = RsFileType.defaultExtension
        return true
    }
}
