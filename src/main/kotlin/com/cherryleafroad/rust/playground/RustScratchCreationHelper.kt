package com.cherryleafroad.rust.playground

import com.cherryleafroad.rust.playground.config.Settings
import com.intellij.ide.scratch.ScratchFileCreationHelper
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import org.rust.ide.notifications.NoCargoProjectNotificationProvider
import org.rust.lang.RsLanguage
import org.rust.openapiext.virtualFile

class RustScratchCreationHelper : ScratchFileCreationHelper() {
    override fun prepareText(project: Project, context: Context, dataContext: DataContext): Boolean {
        context.language = RsLanguage
        context.text = Settings.getScratchDefault()
        return true
    }
}
