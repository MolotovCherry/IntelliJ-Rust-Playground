package com.cherryleafroad.rust.playground.utils

import com.cherryleafroad.rust.playground.parser.BacktraceMode
import com.cherryleafroad.rust.playground.runconfig.CargoConstants
import com.cherryleafroad.rust.playground.runconfig.RustScratchCommandLine
import com.cherryleafroad.rust.playground.runconfig.RustScratchConfiguration
import com.intellij.execution.ExternalizablePath
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.project.Project
import com.intellij.util.io.systemIndependentPath
import org.jdom.Element
import org.rust.cargo.project.settings.toolchain
import org.rust.cargo.toolchain.tools.rustc
import java.nio.file.Path
import java.nio.file.Paths


fun createGeneralCommandLine(
    project: Project,
    rustScratchConfiguration: RustScratchConfiguration
): GeneralCommandLine? {
    val toolchain = project.toolchain

    if (toolchain != null) {
        val rustcExecutable = toolchain.rustc().executable.toString()
        val cargoExecutable = toolchain.pathToExecutable(CargoConstants.CARGO)

        val generalCommandLine = GeneralCommandLine(cargoExecutable.systemIndependentPath)
            .withWorkDirectory(rustScratchConfiguration.parserResults.workingDirectory.systemIndependentPath)
            .withEnvironment("TERM", "ansi")
            .withParameters(rustScratchConfiguration.parserResults.finalCmd)
            .withCharset(Charsets.UTF_8)
            .withRedirectErrorStream(true)
            .withEnvironment("RUSTC", rustcExecutable)

        when (rustScratchConfiguration.parserResults.backtraceMode) {
            BacktraceMode.SHORT -> generalCommandLine.withEnvironment(CargoConstants.RUST_BACKTRACE_ENV_VAR, "short")
            BacktraceMode.FULL -> generalCommandLine.withEnvironment(CargoConstants.RUST_BACKTRACE_ENV_VAR, "full")
            BacktraceMode.NO -> Unit
        }

        rustScratchConfiguration.env.configureCommandLine(generalCommandLine, true)

        return generalCommandLine
    }

    return null
}

fun installBinaryCrate(project: Project, crateName: String) {
    project.toolchain?.let {
        val commandLine = RustScratchCommandLine(
            listOf("install", "--color=always", "--force", crateName),
            isPlayRun = false
        )
        commandLine.run(project, "Install $crateName")
    }
}

fun Element.writeString(name: String, value: String) {
    val opt = Element("option")
    opt.setAttribute("name", name)
    opt.setAttribute("value", value)
    addContent(opt)
}

fun Element.readString(name: String): String? =
    children
        .find { it.name == "option" && it.getAttributeValue("name") == name }
        ?.getAttributeValue("value")

fun Element.writeBool(name: String, value: Boolean) {
    writeString(name, value.toString())
}

fun Element.readBool(name: String): Boolean? =
    readString(name)?.toBoolean()

fun <E : Enum<*>> Element.writeEnum(name: String, value: E) {
    writeString(name, value.name)
}

fun Element.writePath(name: String, value: Path?) {
    if (value != null) {
        val s = ExternalizablePath.urlValue(value.toString())
        writeString(name, s)
    }
}

fun Element.readPath(name: String): Path? {
    return readString(name)?.let { Paths.get(ExternalizablePath.localPathValue(it)) }
}
