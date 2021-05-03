package com.cherryleafroad.rust.playground

import com.cherryleafroad.rust.playground.config.Settings
import com.cherryleafroad.rust.playground.config.SettingsConfigurable
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import org.apache.commons.io.IOUtils
import org.rust.cargo.project.settings.toolchain
import org.rust.cargo.toolchain.RustChannel
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
            val toolchain = project.toolchain
            if (toolchain != null) {
                toolchain!!.cargo().installBinaryCrate(project, "cargo-play")
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

    // <Path to saved files, Pair<playArgs, args>, toolchain>
    fun parseScratch(filename: String, content: String): Pair<List<String>, RustChannel> {
        val lines = content.lines().toMutableList()

        val args = mutableListOf<String>()
        val playArgs = mutableListOf<String>()
        val src = mutableListOf<String>()

        var toolchain = Settings.getSelectedToolchain()

        var parsedPlayArgs = false
        if (lines.isNotEmpty()) {
            val first = lines[0]
            val second = lines.getOrNull(1)

            if (first.startsWith("//@ ") || first.startsWith("//$ ")) {
                if (first.startsWith("//$ ")) {
                    parseArgs(args, first)
                } else if (first.startsWith("//@ ")) {
                    toolchain = parsePlayArgs(playArgs, src, first)
                    parsedPlayArgs = true
                }

                lines.removeAt(0)
            } else if (first == "//@" || first == "//$") {
                // while these are not complete lines, we'll remove them anyways
                lines.removeAt(0)
            }

            if (second != null && second.startsWith("//$ ")) {
                parseArgs(args, second)
                lines.removeAt(0)
            } else if (second != null && second == "//$") {
                lines.removeAt(0)
            }
        }

        if (!parsedPlayArgs) {
            // so we can setup default args that don't require anything
            toolchain = parsePlayArgs(playArgs, src, "")
        }

        // change the toolchain then
        if (toolchain != RustChannel.DEFAULT) {
            playArgs.add(0, "+${toolchain.channel!!}")
        }

        val tmpfile = Paths.get(System.getProperty("java.io.tmpdir"), filename).toString()
        val outStream = FileOutputStream(tmpfile)
        IOUtils.write(lines.joinToString(System.lineSeparator()), outStream, Charset.defaultCharset())
        outStream.close()

        val files = mutableListOf(tmpfile)
        files.addAll(src)

        val finalArgs = mutableListOf<String>()
        finalArgs.addAll(playArgs)
        finalArgs.addAll(files)
        finalArgs.addAll(args)

        return Pair(finalArgs, toolchain)
    }

    private fun parsePlayArgs(args: MutableList<String>, src: MutableList<String>, line: String): RustChannel {
        val shortFlags = listOf(
            "c", "i", "q", "v"
        )
        val flags = listOf(
            "check", "clean", "expand", "infer",
            "quiet", "release", "test", "verbose"
        )
        val options = listOf(
            "cargo-option", "edition", "mode", "src", "toolchain"
        )
        val optionShort = listOf(
            "e", "m"
        )
        val toolchains = listOf(
            "DEFAULT", "STABLE", "BETA", "NIGHTLY", "DEV"
        )

        val newLine = if (line.length > 4) {
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

        if (split.isNotEmpty()) {
            // it is still possible that the first value is an opt
            if (options.contains(split[0].toLowerCase())) {
                optList.add(0, split.joinToString(" "))
            } else {
                for (arg in split) {
                    // invalid flags are ignored
                    if (flags.contains(arg.toLowerCase())) {
                        args.add("--$arg")
                    } else if (shortFlags.contains(arg)) {
                        args.add("-$arg")
                    }
                }
            }
        }

        return parseOpts(args, src, optList, options, optionShort, toolchains)
    }

    private fun parseOpts(
        args: MutableList<String>,
        src: MutableList<String>,
        optList: List<String>,
        options: List<String>,
        shortOpts: List<String>,
        toolchains: List<String>): RustChannel
    {
        val builder = StringBuilder()

        var toolchain = Settings.getSelectedToolchain()

        // cargo option color will always be enabled
        val cargoOptions = mutableListOf("--color=always")

        for (opt in optList) {
            val chunks = opt.split(" ").toMutableList()

            chunks[0] = chunks[0].toLowerCase()

            if (chunks[0] == "toolchain" && chunks.size == 2) {
                if (toolchains.contains(chunks[1].toUpperCase())) {
                    toolchain = RustChannel.valueOf(chunks[1].toUpperCase())
                }
                continue
            } else if (chunks[0] == "src" && chunks.size >= 2) {
                src.addAll(chunks.subList(1, chunks.size))
                continue
            } else if (chunks[0] == "cargo-option") {
                cargoOptions.addAll(chunks.subList(1, chunks.size))
                continue
            }
            // process all remainder generic 2 chunk size args
            else if ((options.contains(chunks[0]) || shortOpts.contains(chunks[0])) && chunks.size == 2) {
                if (chunks[0].length > 1) {
                    args.add("--${chunks[0]}")
                } else {
                    args.add("-${chunks[0]}")
                }
                args.add(chunks[1])
                continue
            }

            args.add(builder.toString())
            builder.clear()
        }

        args.add(0, "--cargo-option=\"${cargoOptions.joinToString(" ")}\"")

        return toolchain
    }

    private fun parseArgs(args: MutableList<String>, line: String) {
        if (args.isEmpty()) {
            args.add("--")
        }

        args.addAll(line.substring(4, line.length).split(" "))
    }
}
