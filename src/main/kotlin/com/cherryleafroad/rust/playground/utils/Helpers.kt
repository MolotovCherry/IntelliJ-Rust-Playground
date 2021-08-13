package com.cherryleafroad.rust.playground.utils

import com.cherryleafroad.rust.playground.config.SettingsConfigurable
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import org.rust.cargo.project.settings.toolchain
import org.rust.cargo.toolchain.tools.cargo

object Helpers {
    fun checkCargoPlayInstalled(project: Project): Boolean {
        return project.toolchain!!.hasCargoExecutable("cargo-play")
    }

    fun checkAndNotifyCargoExpandInstalled(project: Project): Boolean {
        val installed = project.toolchain!!.hasCargoExecutable("cargo-expand")
        if (!installed) {
            val notification = NotificationGroupManager.getInstance().getNotificationGroup("Rust Playground")
                .createNotification(
                    "Rust Playground",
                    "cargo-expand is required to use the expand feature",
                    NotificationType.INFORMATION
                )

            val install = NotificationAction.createSimple("Install") {
                val toolchain = project.toolchain!!
                toolchain.cargo().installBinaryCrate(project, "cargo-expand")
                notification.hideBalloon()
            }
            notification.addAction(install)
            notification.notify(project)
        }

        return installed
    }

    fun checkAndNotifyCargoPlayInstallation(project: Project) {
        if (!checkCargoPlayInstalled(project)) {
            cargoPlayInstallNotification(project)
        }
    }

    @Suppress("DialogTitleCapitalization")
    fun cargoPlayInstallNotification(project: Project) {
        val notification = NotificationGroupManager.getInstance().getNotificationGroup("Rust Playground")
            .createNotification(
                "Rust Playground",
                "Playground requires cargo-play binary crate",
                NotificationType.INFORMATION
            )

        val install = NotificationAction.createSimple("Install") {
            val toolchain = project.toolchain!!
            toolchain.cargo().installBinaryCrate(project, "cargo-play")
            notification.hideBalloon()
        }
        val settings = NotificationAction.createSimple("Settings") {
            ShowSettingsUtil.getInstance().showSettingsDialog(project, SettingsConfigurable::class.java)
        }
        notification.addAction(install)
        notification.addAction(settings)
        notification.notify(project)
    }
}
