package com.cherryleafroad.rust.playground.notifications

import com.intellij.ide.scratch.ScratchUtil
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotifications
import org.rust.cargo.project.settings.toolchain
import org.rust.cargo.runconfig.hasCargoProject
import org.rust.lang.core.psi.isRustFile

class CargoPlayToolchainMissingNotificationProvider(
    private val project: Project
) : EditorNotifications.Provider<EditorNotificationPanel>() {

    companion object {
        const val KEY_VAL = "CargoPlayToolchainMissing"
    }

    @Suppress("PrivatePropertyName")
    private val KEY: Key<EditorNotificationPanel> = Key.create(KEY_VAL)

    private fun disableNotification(file: VirtualFile) {
        PropertiesComponent.getInstance(project).setValue(KEY_VAL + file.path, true)
    }

    private fun isNotificationDisabled(file: VirtualFile): Boolean {
        return PropertiesComponent.getInstance(project).getBoolean(KEY_VAL + file.path)
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
        val toolchainExists = project.toolchain != null
        val hasCargoProject = project.hasCargoProject

        if (!isRust || !isScratch || !hasCargoProject) {
            return null
        }
        if (toolchainExists) {
            return null
        }

        val panel = EditorNotificationPanel()
        panel.text = "Rust Scratches disabled : Toolchain missing or misconfigured"

        return panel
    }

    override fun getKey(): Key<EditorNotificationPanel> {
        return KEY
    }
}
