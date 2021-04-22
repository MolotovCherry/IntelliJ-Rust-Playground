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
import org.rust.cargo.toolchain.tools.cargo
import kotlin.math.max

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

    fun parseOptions(file: VirtualFile): ParserResults {
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

        val toolchain = RustChannel.fromIndex(properties.getInt("toolchain/${file.path}", Settings.getSelectedToolchain().index))
        val edition = Edition.fromIndex(properties.getInt("edition/${file.path}", Edition.DEFAULT.index))

        val src = mutableListOf(file.name)
        src.addAll(properties.getValue("src/${file.path}", "").split(" "))
        val args = properties.getValue("args/${file.path}", "").split(" ").toMutableList()
        val mode = properties.getValue("mode/${file.path}", "")
        val cargoOption = properties.getValue("cargoOptions/${file.path}", "").split(" ").toMutableList()

        if (args.isNotEmpty()) {
            args.add(0, "--")
        }

        var runRun = true
        var runBuild = !onlyrun
        var runBuild2 = false
        val runCmd = mutableListOf<String>()
        val buildCmd = mutableListOf("play")
        val buildCmd2 = mutableListOf("play")

        buildCmd.add("--mode")
        buildCmd.add("build")

        var cleanAndRun = false
        var cleanSingle = false
        val cleanCmd = mutableListOf<String>()
        if (check) {
            if (!onlyrun) {
                buildCmd2.add("--check")
                runRun = false
                runBuild2 = true
            } else {
                runCmd.add("--check")
            }
        }
        if (clean) {
            if (!onlyrun) {
                cleanAndRun = true
                cleanCmd.add("--mode")
                cleanCmd.add("clean")
            } else {
                // this will do a clean + run in one go
                runCmd.add("--clean")
            }
        }
        if (expand) {
            buildCmd.subList(1, buildCmd.size).clear()
            buildCmd.add("--expand")
            runCmd.add("--expand")
        }
        if (infer) {
            buildCmd.add("--infer")
            runCmd.add("--infer")
        }
        if (quiet) {
            runCmd.add("--quiet")
        }
        if (release) {
            buildCmd.add("--release")
            runCmd.add("--release")
        }
        if (test) {
            // we don't currently have a special test runner
            runCmd.add("--test")
            runBuild = false
        }
        if (verbose) {
            buildCmd.add("--verbose")
            runCmd.add("--verbose")
        }
        if (edition != Edition.DEFAULT) {
            buildCmd.add("--edition")
            buildCmd.add(edition.myName)
            runCmd.add("--edition")
            runCmd.add(edition.myName)
        }
        if (mode.isNotEmpty()) {
            if (mode == "clean") {
                if (!onlyrun) {
                    cleanCmd.add("--mode")
                    cleanCmd.add("clean")
                    cleanSingle = true
                    runRun = false
                } else {
                    runCmd.add("--mode")
                    runCmd.add("clean")
                }
            } else {
                runCmd.add("--mode")
                runCmd.add(mode)
            }
        }

        cargoOption.add(0, "--color=always")
        if (!expand) {
            cargoOption.add(1, "--message-format=json-diagnostic-rendered-ansi")
        }
        runCmd.add(runCmd.size, "--cargo-option=\"--color=always\"")
        buildCmd.add(buildCmd.size, "--cargo-option=\"${cargoOption.joinToString(" ")}\"")
        buildCmd2.add(buildCmd2.size, "--cargo-option=\"--color=always --message-format=json-diagnostic-rendered-ansi\"")

        val finalBuildCmd = mutableListOf<String>()
        finalBuildCmd.addAll(buildCmd)
        finalBuildCmd.addAll(src)

        val finalBuildCmd2 = mutableListOf<String>()
        finalBuildCmd2.addAll(buildCmd2)
        finalBuildCmd2.addAll(src)

        val finalRunCmd = mutableListOf<String>()
        finalRunCmd.addAll(runCmd)
        finalRunCmd.addAll(src)
        finalRunCmd.addAll(args)

        return ParserResults(
            check, clean, expand, infer,
            quiet, release, test, verbose, toolchain, onlyrun,
            cargoOption, edition, mode, src, args,
            runCmd, buildCmd, buildCmd2, runBuild, runBuild2,
            runRun, cleanSingle, cleanAndRun, cleanCmd,
            finalBuildCmd, finalRunCmd, finalBuildCmd2
        )
    }
}
