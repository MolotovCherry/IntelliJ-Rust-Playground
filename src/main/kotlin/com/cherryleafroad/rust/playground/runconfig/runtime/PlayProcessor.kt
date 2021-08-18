package com.cherryleafroad.rust.playground.runconfig.runtime

import com.cherryleafroad.rust.playground.config.Settings
import com.cherryleafroad.rust.playground.runconfig.RustScratchCommandLine
import com.cherryleafroad.rust.playground.runconfig.toolchain.Edition
import com.cherryleafroad.rust.playground.runconfig.toolchain.RustChannel
import com.cherryleafroad.rust.playground.scratch.ui.ScratchSettings
import com.cherryleafroad.rust.playground.utils.Helpers
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

object PlayProcessor {
    fun processPlayOptions(project: Project, file: VirtualFile, clean: Boolean): RustScratchCommandLine? {
        val settings = ScratchSettings(file)

        val check = settings.CHECK.getBoolean()
        val cleanProp = settings.CLEAN.getBoolean()
        val expand = settings.EXPAND.getBoolean()
        val infer = settings.INFER.getBoolean()
        val quiet = settings.QUIET.getBoolean()
        val release = settings.RELEASE.getBoolean()
        val test = settings.TEST.getBoolean()
        val verbose = settings.VERBOSE.getBoolean()

        val toolchain = RustChannel.fromIndex(settings.TOOLCHAIN.getInt(Settings.getSelectedToolchain().index))
        val edition = Edition.fromIndex(settings.EDITION.getInt(Edition.DEFAULT.index))

        val src = mutableListOf(file.name)
        src.addAll(settings.SRC.getValue().split(" ").filter { it.isNotEmpty() })
        val args = settings.ARGS.getValue().split(" ").filter { it.isNotEmpty() }.toMutableList()
        val mode = settings.MODE.getValue()
        val cargoOption = settings.CARGO_OPTIONS.getValue().split(" ").filter { it.isNotEmpty() }.toMutableList()
        val cargoOptionNoDefault = settings.CARGO_OPTIONS_NO_DEFAULTS.getBoolean()

        val runCmd = mutableListOf<String>()

        // check for cargo-expand installation
        if (expand) {
            val installed = Helpers.checkAndNotifyCargoExpandInstalled(project)
            if (!installed) {
                return null
            }
        }

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

            // this option could interfere with other cargo options
            if (!cargoOptionNoDefault) {
                cargoOption.add(0, "--color=always")
            }

            if (cargoOption.isNotEmpty()) {
                runCmd.add("--cargo-option=${cargoOption.joinToString(" ")}")
            }
        }

        val finalArgs = runCmd + src + args

        return PlayConfiguration(
            check, cleanProp, expand, infer,
            quiet, release, test, verbose, toolchain,
            cargoOption, edition, mode, src, finalArgs
        ).toRustScratchCommandLine()
    }
}