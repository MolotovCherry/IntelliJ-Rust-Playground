package com.cherryleafroad.rust.playground.utils

import com.cherryleafroad.rust.playground.runconfig.runtime.CommandConfiguration
import com.cherryleafroad.rust.playground.settings.PlayRunConfiguration
import com.cherryleafroad.rust.playground.settings.ScratchConfiguration
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.annotations.MapAnnotation
import org.rust.cargo.project.settings.toolchain
import java.nio.file.Path
import java.nio.file.Paths


fun installBinaryCrate(project: Project, crateName: String) {
    project.toolchain?.let {
        val commandLine = CommandConfiguration(
            command = "install",
            args = listOf("--force", crateName)
        ).toRustScratchCommandLine()
        commandLine.run(project, "Install $crateName")
    }
}

// creates a default value if it doesn't exist
// source code stolen from kotlin with love :) <3
@MapAnnotation(
    keyAttributeName = "file",
    valueAttributeName = "settings", entryTagName = "scratch"
)
class ScratchConfigMap : HashMap<String, ScratchConfiguration>() {
    override fun get(key: String): ScratchConfiguration {
        val value = super.get(key)
        return if (value == null) {
            val default = ScratchConfiguration()
            put(key, default)
            default
        } else {
            value
        }
    }
}

// creates a default value if it doesn't exist
@MapAnnotation(
    keyAttributeName = "run",
    valueAttributeName = "settings", entryTagName = "config"
)
class PlayRunConfigMap : HashMap<Int, PlayRunConfiguration>() {
    override fun get(key: Int): PlayRunConfiguration {
        val value = super.get(key)
        return if (value == null) {
            val default = PlayRunConfiguration()
            put(key, default)
            default
        } else {
            value
        }
    }
}

// why does .split() even create so many empty items? I mean, really
fun CharSequence.splitIgnoreEmpty(vararg delimiters: String): List<String> {
    return this.split(*delimiters).filter {
        it.isNotEmpty()
    }
}

fun String.toPath(): Path {
    return Paths.get(this)
}
