package com.cherryleafroad.rust.playground.actions

import com.cherryleafroad.rust.playground.services.CargoPlayProjectService
import com.intellij.ide.actions.RevealFileAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.util.io.exists


class OpenCargoPlayFolderAction : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val service = project.service<CargoPlayProjectService>()

        val f = service.cargoPlayPath.cargoPlayDir
        RevealFileAction.openDirectory(f)
    }

    override fun update(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val service = project.service<CargoPlayProjectService>()

        e.presentation.isVisible = true
        e.presentation.isEnabled = service.cargoPlayPath.cargoPlayDir.exists()
    }
}

class OpenCargoPlayDebugBinaryAction : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val service = project.service<CargoPlayProjectService>()

        val f = service.cargoPlayPath.debugTarget
        RevealFileAction.openFile(f)
    }

    override fun update(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val service = project.service<CargoPlayProjectService>()

        e.presentation.isVisible = true
        e.presentation.isEnabled = service.cargoPlayPath.debugTarget.exists()
    }
}

class OpenCargoPlayReleaseBinaryAction : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val service = project.service<CargoPlayProjectService>()

        val f = service.cargoPlayPath.releaseTarget
        RevealFileAction.openFile(f)
    }

    override fun update(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val service = project.service<CargoPlayProjectService>()

        e.presentation.isVisible = true
        e.presentation.isEnabled = service.cargoPlayPath.releaseTarget.exists()
    }
}