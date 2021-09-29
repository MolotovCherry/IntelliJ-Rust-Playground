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

        service.cargoPlayPath?.cargoPlayDir?.let {
            RevealFileAction.openDirectory(it)
        }
    }

    override fun update(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val service = project.service<CargoPlayProjectService>()

        e.presentation.isVisible = true
        e.presentation.isEnabled = service.cargoPlayPath?.cargoPlayDir?.exists() ?: false
    }
}

class OpenCargoPlayDebugBinaryAction : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val service = project.service<CargoPlayProjectService>()

        service.cargoPlayPath?.debugTarget?.let {
            RevealFileAction.openFile(it)
        }
    }

    override fun update(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val service = project.service<CargoPlayProjectService>()

        e.presentation.isVisible = true
        e.presentation.isEnabled = service.cargoPlayPath?.debugTarget?.exists() ?: false
    }
}

class OpenCargoPlayReleaseBinaryAction : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val service = project.service<CargoPlayProjectService>()

        service.cargoPlayPath?.releaseTarget?.let {
            RevealFileAction.openFile(it)
        }
    }

    override fun update(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val service = project.service<CargoPlayProjectService>()

        e.presentation.isVisible = true
        e.presentation.isEnabled = service.cargoPlayPath?.releaseTarget?.exists() ?: false
    }
}