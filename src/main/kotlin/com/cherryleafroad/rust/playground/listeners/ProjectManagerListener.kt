package com.cherryleafroad.rust.playground.listeners

import com.cherryleafroad.rust.playground.services.CargoPlayProject
import com.cherryleafroad.rust.playground.services.Settings
import com.cherryleafroad.rust.playground.utils.Helpers
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.jetbrains.rd.util.Callable
import org.rust.cargo.project.settings.toolchain
import org.rust.cargo.toolchain.tools.rustc
import java.nio.file.Paths

internal class ProjectManagerListener : ProjectManagerListener {
    override fun projectOpened(project: Project) {
        Helpers.checkAndNotifyCargoPlayInstallation(project)

        // this might seem weird here, but there's a reason for it
        // you see, these services aren't cached yet, and they should get cached
        // before they're needed in order to avoid slowdown (particularly right menu loading)
        ApplicationManager.getApplication().executeOnPooledThread(Callable {
            CargoPlayProject.getInstance(project)
            Settings.getInstance()
        })
    }
}
