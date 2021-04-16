package com.cherryleafroad.rust.playground.listeners

import com.intellij.ide.DataManager
import com.intellij.ide.scratch.ScratchUtil
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManagerListener
import com.intellij.openapi.vfs.VirtualFile
import org.rust.lang.core.psi.isRustFile
import org.rust.openapiext.project

class FileListener : FileDocumentManagerListener {
    override fun fileContentLoaded(file: VirtualFile, document: Document) {
        DataManager.getInstance().dataContextFromFocusAsync.onSuccess {
            val project = it.project ?: return@onSuccess

            val properties = PropertiesComponent.getInstance(project)

            val isRust = file.isRustFile
            val isScratch = ScratchUtil.isScratch(file)

            // disable no cargo project notification for scratches
            if (isRust && isScratch) {
                val path = file.path
                val key = "org.rust.hideNoCargoProjectNotifications$path"
                // hide no cargo project notification for scratches
                val k = properties.getBoolean(key)
                if (!k) {
                    properties.setValue(key, true)
                }
            }
        }
    }
}
