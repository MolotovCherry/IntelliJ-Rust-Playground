package com.cherryleafroad.rust.playground.listeners

import com.intellij.ide.scratch.ScratchUtil
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import org.rust.lang.core.psi.isRustFile

class EditorCreatedListener : EditorFactoryListener {
    override fun editorCreated(event: EditorFactoryEvent) {
        val file = FileDocumentManager.getInstance().getFile(event.editor.document)
        val project = event.editor.project

        if (file != null && project != null) {
            val isRust = file.isRustFile
            val isScratch = ScratchUtil.isScratch(file)

            if (isRust && isScratch) {
                // disable no cargo project notification for scratches
                val path = file.path
                val key = "org.rust.hideNoCargoProjectNotifications$path"
                PropertiesComponent.getInstance(project).setValue(key, true)
            }
        }
    }
}
