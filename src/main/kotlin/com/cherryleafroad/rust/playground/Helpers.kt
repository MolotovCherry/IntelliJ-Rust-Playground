package com.cherryleafroad.rust.playground

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
import org.rust.cargo.toolchain.tools.cargo

object Helpers {
    fun checkCargoPlayInstalled(project: Project): Boolean {
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
            project.toolchain!!.cargo().installBinaryCrate(project, "cargo-play")
            notification.hideBalloon()
        }
        val settings = NotificationAction.createSimple("Settings") {
            ShowSettingsUtil.getInstance().showSettingsDialog(project, SettingsConfigurable::class.java)
        }
        notification.addAction(install)
        notification.addAction(settings)
        notification.notify(project)
    }

    fun parseOptions(file: VirtualFile) {
        val properties = PropertiesComponent.getInstance()

        val check = properties.getBoolean("check/${file.path}")
        val clean = properties.getBoolean("clean/${file.path}")
        val expand = properties.getBoolean("expand/${file.path}")
        val infer = properties.getBoolean("infer/${file.path}")
        val quiet = properties.getBoolean("quiet/${file.path}")
        val release = properties.getBoolean("release/${file.path}")
        val test = properties.getBoolean("test/${file.path}")
        val verbose = properties.getBoolean("verbose/${file.path}")
        val onlyrun = properties.getBoolean("userun/${file.path}")

        val toolchain = RustChannel.fromIndex(properties.getInt("toolchain/${file.path}", RustChannel.DEFAULT.index))
        val edition = Edition.fromIndex(properties.getInt("edition/${file.path}", 0))

        val src = mutableListOf(file.toNioPath().fileName.toString())
        src.addAll(properties.getValue("src/${file.path}", "").split(" "))
        val args = properties.getValue("args/${file.path}", "").split(" ")
        val mode = properties.getValue("mode/${file.path}", "")
        val cargoOption = properties.getValue("cargoOptions/${file.path}", "")

        var runRun = true
        var runBuild = !onlyrun
        val playArgs = mutableListOf<String>()
        val buildCmd = mutableListOf<String>()

        var cleanAndRun = false
        var cleanSingle = false
        var cleanAndRunCmd = mutableListOf<String>()
        if (check) {
            if (!onlyrun) {
                buildCmd.add("--check")
                runRun = false
            } else {
                playArgs.add("--check")
            }
        }
        if (clean) {
            cleanAndRun = true
            if (!onlyrun) {
                buildCmd.add("--clean")
                runRun = false
            } else {
                playArgs.add("--clean")
            }
        }
        if (expand) {
            playArgs.add("--expand")
        }
        if (infer) {
            buildCmd.add("--infer")
            playArgs.add("--infer")
        }
        if (quiet) {
            buildCmd.add("--quiet")
            playArgs.add("--quiet")
        }
        if (release) {
            buildCmd.add("--release")
            playArgs.add("--release")
        }
        if (test) {
            playArgs.add("--test")
            runBuild = false
        }
        if (verbose) {
            buildCmd.add("--verbose")
            playArgs.add("--verbose")
        }
        if (edition != Edition.DEFAULT) {
            buildCmd.add("--edition")
            buildCmd.add(edition.myName)
            playArgs.add("--edition")
            playArgs.add(edition.myName)
        }
        if (mode.isNotEmpty()) {
            playArgs.add("--mode")
            playArgs.add(mode)

            if (mode == "clean") {
                cleanSingle = true
            }
        }

        playArgs.add(0, "--cargo-option=\"--color=always\"")
        buildCmd.add(0, "--cargo-option=\"--color=always --message-format=json-diagnostic-rendered-ansi\"")


        val parserResults = ParserResults(
            check,
            clean,
            expand,
            infer,
            quiet,
            release,
            test,
            verbose,
            toolchain,
            cargoOption.split(" ").toMutableList(),
            edition,
            mode,
            src,
            args,
            playArgs,
            runCmd.joinTo,
            buildCmd,
            runBuild, runRun


        )
    }
}
