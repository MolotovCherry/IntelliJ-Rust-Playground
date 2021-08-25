package com.cherryleafroad.rust.playground.runconfig.runtime

import com.cherryleafroad.rust.playground.runconfig.toolchain.Edition
import com.cherryleafroad.rust.playground.runconfig.toolchain.RustChannel
import com.cherryleafroad.rust.playground.services.Settings
import com.cherryleafroad.rust.playground.utils.Helpers
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

fun processPlayOptions(file: VirtualFile, clean: Boolean) {
    val settings = Settings.getInstance().scratches[file.path]

    val check = settings.check
    val cleanProp = settings.clean
    val expand = settings.expand
    val infer = settings.infer
    val quiet = settings.quiet
    val release = settings.release
    val test = settings.test
    val verbose = settings.verbose

    val toolchain = settings.toolchain
    val edition = settings.edition

    val srcs = settings.srcs.toMutableList()
    srcs.add(0, file.name)

    val args = settings.args.toMutableList()
    val mode = settings.mode
    val cargoOptions = settings.cargoOptions.toMutableList()
    val cargoOptionNoDefault = settings.cargoOptionsNoDefault

    val runCmd = mutableListOf<String>()

    // change the toolchain
    if (toolchain != RustChannel.DEFAULT) {
        runCmd.add("+${toolchain.channel}")
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
            cargoOptions.add(0, "--color=always")
        }

        if (cargoOptions.isNotEmpty()) {
            runCmd.add("--cargo-option=${cargoOptions.joinToString(" ")}")
        }
    }

    val finalArgs = runCmd + srcs + args
    settings.generatedArgs = finalArgs
}
