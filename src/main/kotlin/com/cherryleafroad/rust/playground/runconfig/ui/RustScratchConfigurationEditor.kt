package com.cherryleafroad.rust.playground.runconfig.ui

import com.cherryleafroad.rust.playground.runconfig.RustScratchConfiguration
import com.intellij.openapi.options.SettingsEditor
import com.intellij.ui.components.Label
import com.intellij.ui.layout.panel
import javax.swing.JComponent
import javax.swing.JTextField

class RustScratchConfigurationEditor(): SettingsEditor<RustScratchConfiguration>() {
    private val command: JTextField = JTextField()

    override fun resetEditorFrom(configuration: RustScratchConfiguration) {
        command.text = configuration.commandConfiguration.command + configuration.commandConfiguration.args.joinToString(" ")
    }

    override fun applyEditorTo(configuration: RustScratchConfiguration) {
        configuration.commandConfiguration.command = command.text.substringBefore(" ")
        configuration.commandConfiguration.args = command.text.substringAfter(" ").split(" ")
    }

    override fun createEditor(): JComponent = panel {
        val label = Label("Command")
        label.labelFor = command
    }
}
