package com.cherryleafroad.rust.playground.settings.plugin

import com.cherryleafroad.rust.playground.runconfig.toolchain.Edition
import com.cherryleafroad.rust.playground.runconfig.toolchain.RustChannel
import com.cherryleafroad.rust.playground.services.Settings
import com.cherryleafroad.rust.playground.settings.plugin.ui.SettingsForm
import com.intellij.openapi.Disposable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import org.rust.cargo.project.settings.toolchain
import javax.swing.JComponent

class SettingsConfigurable(private val project: Project) : SearchableConfigurable, Disposable {
    private var _mySettings: SettingsForm? = SettingsForm()
    private val mySettings: SettingsForm
        get() = _mySettings!!

    private val settings = Settings.getInstance().plugin
    private val origHash: Int = settings.scratchText.hashCode()

    override fun createComponent(): JComponent {
        reset()
        return mySettings.getPanel()
    }

    override fun isModified(): Boolean {
        val scratch = mySettings.scratchTextArea.hashCode() != origHash
        val toolchain = mySettings.selectedToolchain.selectedIndex != settings.toolchain.index
        val edition = mySettings.selectedEdition.selectedIndex != settings.edition.index
        return scratch || toolchain || edition
    }

    override fun apply() {
        settings.scratchText = mySettings.scratchTextArea
        settings.toolchain = mySettings.selectedToolchain.selectedItem as RustChannel
        settings.edition = mySettings.selectedEdition.selectedItem as Edition
    }

    override fun reset() {
        project.toolchain?.hasCargoExecutable("cargo-play")?.let {
            mySettings.cargoPlayInstalled.isEnabled = it
        }
        project.toolchain?.hasCargoExecutable("cargo-expand")?.let {
            mySettings.cargoExpandInstalled.isEnabled = it
        }

        mySettings.selectedToolchain.selectedIndex = settings.toolchain.index
        mySettings.selectedEdition.selectedIndex = settings.edition.index
        mySettings.scratchTextArea = settings.scratchText
    }

    override fun getDisplayName(): String {
        return "Playground"
    }

    override fun getId(): String {
        return "Rust.Playground.Settings"
    }

    override fun dispose() {
        _mySettings = null
    }
}