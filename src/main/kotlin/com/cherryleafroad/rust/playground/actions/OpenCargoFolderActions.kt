package com.cherryleafroad.rust.playground.actions

import com.cherryleafroad.rust.playground.services.CargoPlayProject
import com.intellij.ide.actions.RevealFileAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.util.io.exists
import java.nio.file.Paths


class OpenCargoPlayFolderAction : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val service = CargoPlayProject.getInstance(project)

        service.cargoPlayPath?.cargoPlayDir?.let {
            RevealFileAction.openDirectory(Paths.get(it))
        }
    }

    override fun update(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val service = CargoPlayProject.getInstance(project)

        e.presentation.isVisible = true
        e.presentation.isEnabled = service.cargoPlayPath?.cargoPlayDir?.let {
            Paths.get(it).exists()
        } ?: false
    }
}

class OpenCargoPlayDebugBinaryAction : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val service = CargoPlayProject.getInstance(project)

        service.cargoPlayPath?.debugTarget?.let {
            RevealFileAction.openFile(Paths.get(it))
        }
    }

    override fun update(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val service = CargoPlayProject.getInstance(project)

        e.presentation.isVisible = true
        e.presentation.isEnabled = service.cargoPlayPath?.debugTarget?.let {
            Paths.get(it).exists()
        } ?: false
    }
}

class OpenCargoPlayReleaseBinaryAction : DumbAwareAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val service = CargoPlayProject.getInstance(project)

        service.cargoPlayPath?.releaseTarget?.let {
            RevealFileAction.openFile(Paths.get(it))
        }
    }

    override fun update(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val service = CargoPlayProject.getInstance(project)

        e.presentation.isVisible = true
        e.presentation.isEnabled = service.cargoPlayPath?.releaseTarget?.let {
            Paths.get(it).exists()
        } ?: false
    }
}