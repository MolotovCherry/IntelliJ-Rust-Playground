package com.cherryleafroad.rust.playground.runconfig.runtime

import com.cherryleafroad.rust.playground.runconfig.toolchain.Edition
import com.cherryleafroad.rust.playground.runconfig.toolchain.RustChannel
import com.cherryleafroad.rust.playground.services.Settings

fun processPlayOptions() {
    val settings = Settings.getInstance()
    val scratchSettings = settings.global.runtime.currentScratch

    val check = scratchSettings.check
    val clean = scratchSettings.clean
    val expand = scratchSettings.expand
    val infer = scratchSettings.infer
    val quiet = scratchSettings.quiet
    val release = scratchSettings.release
    val test = scratchSettings.test
    val verbose = scratchSettings.verbose

    val toolchain = scratchSettings.toolchain
    val edition = scratchSettings.edition

    val srcs = scratchSettings.srcs.toMutableList()
    srcs.add(0, settings.global.runtime.scratchFile.name)

    val args = scratchSettings.args.toMutableList()
    val mode = scratchSettings.mode
    val cargoOptions = scratchSettings.cargoOptions.toMutableList()
    val cargoOptionNoDefault = scratchSettings.cargoOptionsNoDefault

    val runCmd = mutableListOf<String>()

    // change the toolchain
    if (toolchain != RustChannel.DEFAULT) {
        runCmd.add("+${toolchain.channel}")
    }

    if (settings.global.runtime.clean) {
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
        if (clean) {
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
    scratchSettings.generatedArgs = finalArgs
    scratchSettings.filterSrcs = srcs
}
