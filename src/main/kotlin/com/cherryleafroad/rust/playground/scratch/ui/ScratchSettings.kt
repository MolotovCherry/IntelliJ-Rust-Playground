package com.cherryleafroad.rust.playground.scratch.ui

import com.cherryleafroad.rust.playground.config.BooleanSetting
import com.cherryleafroad.rust.playground.config.EditionSetting
import com.cherryleafroad.rust.playground.config.StringSetting
import com.cherryleafroad.rust.playground.config.ToolchainSetting
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.vfs.VirtualFile

@Suppress("PropertyName")
class ScratchSettings(val file: VirtualFile) {
    private val properties = PropertiesComponent.getInstance()

    val ARGS = StringSetting("args.${file.path}", properties)
    val SRC = StringSetting("src.${file.path}", properties)
    val CARGO_OPTIONS = StringSetting("cargoOptions.${file.path}", properties)
    val MODE = StringSetting("mode.${file.path}", properties)
    val CARGO_OPTIONS_NO_DEFAULTS = BooleanSetting("cargoOptionsNoDefaults.${file.path}", properties)
    val EXPAND = BooleanSetting("expand.${file.path}", properties)
    val VERBOSE = BooleanSetting("verbose.${file.path}", properties)
    val TEST = BooleanSetting("test.${file.path}", properties)
    val RELEASE = BooleanSetting("release.${file.path}", properties)
    val QUIET = BooleanSetting("quiet.${file.path}", properties)
    val INFER = BooleanSetting("infer.${file.path}", properties)
    val CLEAN = BooleanSetting("clean.${file.path}", properties)
    val CHECK = BooleanSetting("check.${file.path}", properties)
    val TOOLCHAIN = ToolchainSetting("toolchain.${file.path}", properties)
    val EDITION = EditionSetting("edition.${file.path}", properties)
}
