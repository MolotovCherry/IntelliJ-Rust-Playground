package com.cherryleafroad.rust.playground

import com.cherryleafroad.rust.playground.config.Settings
import com.cherryleafroad.rust.playground.config.SettingsConfigurable
import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.rust.cargo.toolchain.RustChannel
import org.rust.cargo.project.settings.toolchain
import org.rust.cargo.runconfig.hasCargoProject
import org.rust.cargo.toolchain.tools.cargo

object Helpers {
    fun checkCargoPlayInstalled(project: Project): Boolean {
        // ignore for non-Rust projects
        val hasCargoProject = project.hasCargoProject
        if (!hasCargoProject) {
            return true
        }

        return project.toolchain?.hasCargoExecutable("cargo-play") ?: false
    }

    fun checkAndNotifyCargoPlayInstallation(project: Project) {
        checkCargoPlayInstalled(project).let {
            if (!it) {
                cargoPlayInstallNotification(project)
            }
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
            val toolchain = project.toolchain
            if (toolchain != null) {
                toolchain.cargo().installBinaryCrate(project, "cargo-play")
                notification.hideBalloon()
            }
        }
        val settings = NotificationAction.createSimple("Settings") {
            ShowSettingsUtil.getInstance().showSettingsDialog(project, SettingsConfigurable::class.java)
        }
        notification.addAction(install)
        notification.addAction(settings)
        notification.notify(project)
    }

    fun parseOptions(file: VirtualFile, clean: Boolean): ParserResults {
        val properties = PropertiesComponent.getInstance()

        val check = properties.getBoolean("check/${file.path}")
        val cleanProp = properties.getBoolean("clean/${file.path}")
        val expand = properties.getBoolean("expand/${file.path}")
        val infer = properties.getBoolean("infer/${file.path}")
        val quiet = properties.getBoolean("quiet/${file.path}")
        val release = properties.getBoolean("release/${file.path}")
        val test = properties.getBoolean("test/${file.path}")
        val verbose = properties.getBoolean("verbose/${file.path}")

        val toolchain = RustChannel.fromIndex(properties.getInt("toolchain/${file.path}", Settings.getSelectedToolchain().index))
        val edition = Edition.fromIndex(properties.getInt("edition/${file.path}", Edition.DEFAULT.index))

        val src = mutableListOf(file.name)
        src.addAll(properties.getValue("src/${file.path}", "").split(" ").filter { it.isNotEmpty() })
        val args = properties.getValue("args/${file.path}", "").split(" ").filter { it.isNotEmpty() }.toMutableList()
        val mode = properties.getValue("mode/${file.path}", "")
        val cargoOption = properties.getValue("cargoOptions/${file.path}", "").split(" ").filter { it.isNotEmpty() }.toMutableList()

        val runCmd = mutableListOf<String>()
        cargoOption.add(0, "--color=always")

        // change the toolchain
        if (toolchain != RustChannel.DEFAULT) {
            runCmd.add("+${toolchain.channel!!}")
        }

        if (clean) {
            // one time clean and exit
            runCmd.add("--mode")
            runCmd.add("clean")
        } else {
            if (args.isNotEmpty()) {
                args.add(0, "--")
            }

            if (check) {
                runCmd.add("--check")
            }
            if (cleanProp) {
                runCmd.add("--clean")
            }
            if (expand) {
                runCmd.add("--expand")
            }
            if (infer) {
                runCmd.add("--infer")
            }
            if (quiet) {
                runCmd.add("--quiet")
            }
            if (release) {
                runCmd.add("--release")
            }
            if (test) {
                runCmd.add("--test")
            }
            if (verbose) {
                runCmd.add("--verbose")
            }
            if (edition != Edition.DEFAULT) {
                runCmd.add("--edition")
                runCmd.add(edition.myName)
            }
            if (mode.isNotEmpty()) {
                runCmd.add("--mode")
                runCmd.add(mode)
            }

            runCmd.add(runCmd.size, "--cargo-option=\"${cargoOption.joinToString(" ")}\"")
        }

        val finalCmd = runCmd + src + args

        return ParserResults(
            check, cleanProp, expand, infer,
            quiet, release, test, verbose, toolchain,
            cargoOption, edition, mode, src, args,
            runCmd, finalCmd
        )
    }
}
