package com.cherryleafroad.rust.playground.utils

import com.cherryleafroad.rust.playground.runconfig.runtime.CommandConfiguration
import com.intellij.execution.ExternalizablePath
import com.intellij.openapi.project.Project
import org.jdom.Element
import org.rust.cargo.project.settings.toolchain
import java.nio.file.Path
import java.nio.file.Paths


fun installBinaryCrate(project: Project, crateName: String) {
    project.toolchain?.let {
        val commandLine = CommandConfiguration(
            "install",
            listOf("--force", crateName)
        ).toRustScratchCommandLine()
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
