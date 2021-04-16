package com.cherryleafroad.rust.playground.actions

import com.cherryleafroad.rust.playground.Helpers
import com.cherryleafroad.rust.playground.PatchCargoCommandLine
import com.intellij.ide.scratch.ScratchUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import org.rust.cargo.project.model.cargoProjects
import org.rust.lang.core.psi.isRustFile
import org.rust.openapiext.virtualFile

class RunAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project!!

        val cargoPlayInstalled = Helpers.checkCargoPlayInstalled(project)
        if (cargoPlayInstalled) {
            val code = event.getData(LangDataKeys.EDITOR)?.document?.text
            if (code != null) {
                val cargoProject = project.cargoProjects.allProjects.firstOrNull() ?: return

                val doc = FileEditorManager.getInstance(project).selectedTextEditor?.document!!.virtualFile!!
                val cwd = doc.toNioPath().parent
                val fileName = doc.name

                val (args, toolchain) = Helpers.parseScratch(fileName, code)

                val commandLine = PatchCargoCommandLine(
                    "play",
                    cwd,
                    args,
                    channel = toolchain
                )
                commandLine.run(cargoProject, "Play $fileName", saveConfiguration = false)
            }
        } else {
            Helpers.cargoPlayInstallNotification(project)
        }
    }

    override fun update(e: AnActionEvent) {
        // Set the availability based on whether a project is open
        val project: Project? = e.project
        e.presentation.isVisible = true

        if (project != null) {
            val currentDoc = FileEditorManager.getInstance(project).selectedTextEditor?.document
            if (currentDoc != null) {
                val currentFile = FileDocumentManager.getInstance().getFile(currentDoc)
                val isRust = currentFile?.isRustFile ?: false
                val isScratch = ScratchUtil.isScratch(currentFile)

                e.presentation.isEnabled = isRust && isScratch
            }
        }
    }
}