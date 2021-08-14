package com.cherryleafroad.rust.playground.scratch.ui

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.vfs.VirtualFile

@Suppress("PropertyName")
class ScratchSettings(val file: VirtualFile) {
    private val properties = PropertiesComponent.getInstance()

    val ARGS = Setting("args.", file.path, properties)
    val SRC = Setting("src.", file.path, properties)
    val CARGO_OPTIONS = Setting("cargoOptions.", file.path, properties)
    val MODE = Setting("mode.", file.path, properties)
    val CARGO_OPTIONS_NO_DEFAULTS = Setting("cargoOptionsNoDefaults.", file.path, properties)
    val EXPAND = Setting("expand.", file.path, properties)
    val VERBOSE = Setting("verbose.", file.path, properties)
    val TEST = Setting("test.", file.path, properties)
    val RELEASE = Setting("release.", file.path, properties)
    val QUIET = Setting("quiet.", file.path, properties)
    val INFER = Setting("infer.", file.path, properties)
    val CLEAN = Setting("clean.", file.path, properties)
    val CHECK = Setting("check.", file.path, properties)
    val TOOLCHAIN = Setting("toolchain.", file.path, properties)
    val EDITION = Setting("edition.", file.path, properties)
}

class Setting(prefix: String, filePath: String, private val properties: PropertiesComponent) {
    private val setting = "$prefix${filePath}"

    fun getValue(defaultValue: String = ""): String {
        return properties.getValue(setting, defaultValue)
    }

    fun getBoolean(defaultValue: Boolean = false): Boolean {
        return properties.getBoolean(setting, defaultValue)
    }

    fun getInt(defaultValue: Int = 0): Int {
        return properties.getInt(setting, defaultValue)
    }

    fun setValue(value: String) {
        properties.setValue(setting, value)
    }

    fun setValue(value: Boolean) {
        properties.setValue(setting, value)
    }

    fun setValue(value: Int) {
        // this is needed to avoid "unset if value equals default" feature
        // basically, always save the setting as it should be, don't do any unset nonsense
        var defaultValue = 0
        if (value == defaultValue) {
            defaultValue++
        }
        properties.setValue(setting, value, defaultValue)
    }

    fun unsetValue() {
        properties.unsetValue(setting)
    }
}
