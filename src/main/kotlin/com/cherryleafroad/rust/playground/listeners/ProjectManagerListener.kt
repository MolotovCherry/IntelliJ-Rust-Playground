package com.cherryleafroad.rust.playground.listeners

import com.cherryleafroad.rust.playground.Helpers
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener

internal class ProjectManagerListener : ProjectManagerListener {
    override fun projectOpened(project: Project) {
        Helpers.checkAndNotifyCargoPlayInstallation(project)
    }
}
