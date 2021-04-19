package com.cherryleafroad.rust.playground

import com.cherryleafroad.rust.playground.config.SettingsConfigurable
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import org.apache.commons.io.IOUtils
import org.rust.cargo.toolchain.RustChannel
import org.rust.cargo.project.settings.toolchain
import org.rust.cargo.toolchain.tools.cargo
import java.io.FileOutputStream
import java.nio.charset.Charset
import java.nio.file.Paths

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

    fun parseScratch(filename: String, content: String): ParserResults {
        val lines = content.lines().toMutableList()

        val parserResults = ParserResults()

        var parsedPlayArgs = false
        if (lines.isNotEmpty()) {
            val first = lines[0]
            val second = lines.getOrNull(1)

            if (first.startsWith("//@ ") || first.startsWith("//$ ")) {
                if (first.startsWith("//$ ")) {
                    parseArgs(parserResults, first)
                } else if (first.startsWith("//@ ")) {
                    parsePlayArgs(parserResults, first)
                    parsedPlayArgs = true
                }

                lines.removeAt(0)
                parserResults.lineOffset += 1
            } else if (first == "//@" || first == "//$") {
                // while these are not complete lines, we'll remove them anyways
                lines.removeAt(0)
                parserResults.lineOffset += 1
            }

            if (second != null && second.startsWith("//$ ")) {
                parseArgs(parserResults, second)
                lines.removeAt(0)
                parserResults.lineOffset += 1
            } else if (second != null && second == "//$") {
                lines.removeAt(0)
                parserResults.lineOffset += 1
            }
        }

        if (!parsedPlayArgs) {
            // so we can setup default args that don't require anything
            parsePlayArgs(parserResults, "")
        }

        val tmpfile = Paths.get(System.getProperty("java.io.tmpdir"), filename).toString()
        val outStream = FileOutputStream(tmpfile)
        IOUtils.write(lines.joinToString(System.lineSeparator()), outStream, Charset.defaultCharset())
        outStream.close()

        // add in default script
        parserResults.src.add(0, tmpfile)

        //
        // Process build mode and final cmd
        //
        if (parserResults.mode == null || parserResults.mode == "build") {
            parserResults.buildCmd.add("--mode build")
            parserResults.runBuild = true
            parserResults.runRun = true
        } else if (parserResults.mode == "test" || parserResults.test) {
            // test mode not supported for now
            //parserResults.test = true
            //parserResults.buildCmd.add("--test")
            //parserResults.cargoOption.add("--no-run")
            //parserResults.buildCmd.addAll(parserResults.args)
            //parserResults.runRun = false

            parserResults.runRun = true
            parserResults.runBuild = true
            parserResults.buildCmd.add("--mode build")
            parserResults.runCmd.add(0, "--test")
        } else if (parserResults.mode == "check" || parserResults.check) {
            parserResults.check = true
            parserResults.buildCmd.add("--check")
            parserResults.runBuild = true
        } else if (parserResults.mode == "expand" || parserResults.expand) {
            // expand will be done in the runner
            parserResults.expand = true
            // required to fully build it as `--mode build` won't build cache for expand
            parserResults.buildCmd.add("--expand")
            parserResults.runBuild = true
            parserResults.runRun = true
        }

        if (parserResults.mode == "clean" || parserResults.clean) {
            parserResults.clean = true

            // mode clean ONLY does clean
            if (parserResults.mode!!.isNotEmpty()) {
                parserResults.buildCmd.subList(1, parserResults.buildCmd.size).clear()
                parserResults.buildCmd.add("--mode clean")
                parserResults.cleanSingle = true
                parserResults.runBuild = true
                parserResults.runRun = false
            } else {
                // otherwise --clean is a clean + run op

                // == 1 means that it will only run these defaults if no other option was above
                // otherwise it'll let the other option run its command / defaults

                if (!parserResults.runBuild && !parserResults.runRun) {
                    parserResults.buildCmd.add("--mode build")
                    parserResults.runBuild = true
                    parserResults.runRun = true
                }

                parserResults.runBuild = true
                parserResults.cleanAndRunCmd.add("--mode clean")
                parserResults.cleanAndRun = true
            }
        }

        // no matches above, so do a default run only
        if (!parserResults.runBuild && !parserResults.runRun) {
            // some other mode ? only run it then
            parserResults.runRun = true
            parserResults.noMatches = true
        }

        if (!parserResults.cleanSingle) {
            if (!parserResults.expand) {
                parserResults.cargoOption.add("--message-format=json-diagnostic-rendered-ansi")
            }
            parserResults.buildCmd.add("--cargo-option=\"${parserResults.cargoOption.joinToString(" ")}\"")
        }

        // finally the files to use
        parserResults.buildCmd.add(parserResults.src.joinToString(" "))
        if (parserResults.cleanAndRun) {
            parserResults.cleanAndRunCmd.add(parserResults.src.joinToString(" "))
        }

        // final run command
        parserResults.apply {
            runCmd.addAll(playArgs)
            runCmd.addAll(src)
            runCmd.addAll(args)
        }

        return parserResults
    }

    private fun parsePlayArgs(parseResults: ParserResults, line: String) {
        val shortFlags = listOf(
            "c", "i", "q", "v"
        )
        val flags = listOf(
            "check", "clean", "expand", "infer",
            "quiet", "release", "test", "verbose"
        )

        fun processFlag(flag: String) {
            // case sensitive for short flag
            var passedFlag: String? = null
            if (flags.contains(flag.toLowerCase())) {
                passedFlag = flag.toLowerCase()
            } else if (shortFlags.contains(flag)) {
                passedFlag = flag
            }

            if (passedFlag != null) {
                var addFlag = false
                parseResults.apply {
                    when(passedFlag) {
                             "check"   -> { check = true; mode = "" }
                        "c", "clean"   -> { clean = true; mode = "" }
                             "expand"  -> { expand = true; addFlag = true; mode = "" }
                        "i", "infer"   -> { infer = true }
                        "q", "quiet"   -> { quiet = true; addFlag = true }
                             "release" -> { release = true; addFlag = true }
                             "test"    -> { test = true; mode = "" }
                        "v", "verbose" -> { verbose = true; addFlag = true }
                    }

                    if (addFlag) {
                        if (passedFlag.length == 1) {
                            playArgs.add("-$passedFlag")
                        } else {
                            playArgs.add("--$passedFlag")
                        }
                    }
                }

            }
        }

        val options = listOf(
            "cargo-option", "edition", "mode", "src", "toolchain"
        )
        val optionShort = listOf(
            "e", "m"
        )

        val toolchains = listOf(
            "DEFAULT", "STABLE", "BETA", "NIGHTLY", "DEV"
        )

        fun processOption(option: String) {
            val chunks = option.split(" ").toMutableList()

            chunks[0] = chunks[0].toLowerCase()

            if (chunks[0] == "toolchain" && chunks.size == 2) {
                if (toolchains.contains(chunks[1].toUpperCase())) {
                    parseResults.toolchain = RustChannel.valueOf(chunks[1].toUpperCase())
                }
                return
            } else if (chunks[0] == "src" && chunks.size >= 2) {
                parseResults.src.addAll(chunks.subList(1, chunks.size))
                return
            } else if (chunks[0] == "cargo-option") {
                parseResults.cargoOption.addAll(chunks.subList(1, chunks.size))
                return
            }
            // process all remainder generic 2 chunk size args
            else if ((options.contains(chunks[0]) || optionShort.contains(chunks[0])) && chunks.size == 2) {
                var addFlag = true
                when(chunks[0]) {
                    "e", "edition" -> { parseResults.edition = chunks[1] }
                    "m", "mode"    -> { parseResults.mode = chunks[1].toLowerCase(); addFlag = false }
                }

                if (addFlag) {
                    if (chunks[0].length > 1) {
                        parseResults.playArgs.add("--${chunks[0]}")
                    } else {
                        parseResults.playArgs.add("-${chunks[0]}")
                    }
                    parseResults.playArgs.add(chunks[1])
                }
                return
            }
        }

        // it will either be 4 or more, or empty
        val newLine = if (line.isNotEmpty()) {
            line.substring(4, line.length)
        } else {
            ""
        }

        val firstComma = newLine.indexOf(',')
        val split: MutableList<String>
        val optList: MutableList<String>
        // split based off of comma occurrance
        if (firstComma >= 0) {
            split = newLine.substring(0, firstComma).split(" ").toMutableList()
            optList = newLine.substring(firstComma, newLine.length).split(",").toMutableList()
        } else {
            split = newLine.split(" ").toMutableList()
            optList = mutableListOf()
        }

        // clean up lists
        var iter = split.listIterator()
        for (s in iter) {
            if (s.isEmpty()) {
                iter.remove()
            } else {
                iter.set(s.trim())
            }
        }
        // clean up
        iter = optList.listIterator()
        for (o in iter) {
            if (o.isEmpty()) {
                iter.remove()
            } else {
                iter.set(o.trim())
            }
        }

        //
        // Parse args below
        //

        if (split.isNotEmpty()) {
            // it is still possible that the first value is an opt
            if (options.contains(split[0].toLowerCase())) {
                optList.add(0, split.joinToString(" "))
            } else {
                for (arg in split) {
                    processFlag(arg)
                }
            }
        }

        //
        // Parse Opts below
        //

        // cargo option color will ALWAYS be enabled
        parseResults.cargoOption.add("--color=always")

        for (opt in optList) {
            processOption(opt)
        }

        // for the run command, don't set compiler flags, only colors
        parseResults.playArgs.add(0, "--cargo-option=\"--color=always\"")
    }

    private fun parseArgs(parseResults: ParserResults, line: String) {
        parseResults.args.apply {
            if (isEmpty()) {
                add("--")
            }

            addAll(line.substring(4, line.length).split(" "))
        }
    }
}
