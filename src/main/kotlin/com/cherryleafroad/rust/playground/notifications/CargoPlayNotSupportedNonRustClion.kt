package com.cherryleafroad.rust.playground.notifications

import com.intellij.ide.scratch.ScratchUtil
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotifications
import com.intellij.util.PlatformUtils
import org.rust.cargo.runconfig.hasCargoProject
import org.rust.lang.core.psi.isRustFile

class CargoPlayNotSupportedNonRustClionNotificationProvider(
        private val project: Project
    ) : EditorNotifications.Provider<EditorNotificationPanel>() {

        companion object {
            const val KEY_VAL = "CargoPlayNotSupportedNonRustClion"
        }

        @Suppress("PrivatePropertyName")
        private val KEY: Key<EditorNotificationPanel> = Key.create(KEY_VAL)

        private fun disableNotification(file: VirtualFile) {
            project.service<PropertiesComponent>().setValue(KEY_VAL + file.path, true)
        }

        private fun isNotificationDisabled(file: VirtualFile): Boolean {
            return project.service<PropertiesComponent>().getBoolean(KEY_VAL + file.path)
        }

        private fun updateAllNotifications() {
            EditorNotifications.getInstance(project).updateAllNotifications()
        }

        override fun createNotificationPanel(
            file: VirtualFile,
            fileEditor: FileEditor,
            project: Project
        ): EditorNotificationPanel? {
            val isRust = file.isRustFile
            val isScratch = ScratchUtil.isScratch(file)
            val hasCargoProject = project.hasCargoProject

            if (!hasCargoProject && PlatformUtils.isCLion()) {
                if (isRust && isScratch) {
                    val panel = EditorNotificationPanel()
                    panel.text = "Clion doesn't support Rust scratches in non-Cargo projects"

                    return panel
                }
            }

            return null
        }

        override fun getKey(): Key<EditorNotificationPanel> {
            return KEY
        }
    }
