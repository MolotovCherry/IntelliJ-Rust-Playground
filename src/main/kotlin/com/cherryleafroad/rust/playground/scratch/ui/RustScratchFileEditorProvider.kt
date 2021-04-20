package com.cherryleafroad.rust.playground.scratch.ui

import com.cherryleafroad.rust.playground.actions.ToolbarExecuteAction
import com.intellij.ide.scratch.ScratchUtil
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.rust.lang.core.psi.isNotRustFile

private const val RUST_SCRATCH_EDITOR_PROVIDER: String = "RustScratchFileEditorProvider"

class RustScratchFileEditorProvider : FileEditorProvider, DumbAware {
    override fun accept(project: Project, file: VirtualFile): Boolean {
        if (!file.isValid) return false
        if (file.isNotRustFile) return false
        if (!ScratchUtil.isScratch(file)) return false
        return true
    }

    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        return RustScratchFileEditor(project, file)
    }

    override fun getEditorTypeId(): String = RUST_SCRATCH_EDITOR_PROVIDER

    override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.HIDE_DEFAULT_EDITOR
}

class RustScratchFileEditor(
    project: Project,
    file: VirtualFile
) : ToolbarTextEditor(project, file) {

    override fun addActions(toolbarGroup: DefaultActionGroup) {
        toolbarGroup.apply {
            add(ToolbarExecuteAction())
            addSeparator()
        }
    }
}
