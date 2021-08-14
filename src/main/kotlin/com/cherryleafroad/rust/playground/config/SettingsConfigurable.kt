package com.cherryleafroad.rust.playground.config

import com.cherryleafroad.rust.playground.config.Settings.EDITION_KEY
import com.cherryleafroad.rust.playground.config.Settings.SCRATCH_KEY
import com.cherryleafroad.rust.playground.config.Settings.TOOLCHAIN_KEY
import com.cherryleafroad.rust.playground.config.ui.SettingsForm
import com.cherryleafroad.rust.playground.parser.Edition
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.Disposable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import org.rust.cargo.project.settings.toolchain
import org.rust.cargo.toolchain.RustChannel
import javax.swing.JComponent

class SettingsConfigurable(private val project: Project) : SearchableConfigurable, Disposable {
    private var mySettings: SettingsForm? = SettingsForm()
        get() = field!!
    private val properties: PropertiesComponent = PropertiesComponent.getInstance()
    private val origHash: Int = Settings.getScratchDefault().hashCode()

    override fun createComponent(): JComponent {
        reset()
        return mySettings!!.getPanel()
    }

    override fun isModified(): Boolean {
        val scratch = mySettings!!.getScratch().hashCode() != origHash
        val toolchain = mySettings!!.selectedToolchain.selectedIndex != properties.getInt(TOOLCHAIN_KEY, RustChannel.DEFAULT.index)
        val edition = mySettings!!.selectedEdition.selectedIndex != properties.getInt(EDITION_KEY, Edition.DEFAULT.index)
        return scratch || toolchain || edition
    }

    override fun apply() {
        properties.setValue(SCRATCH_KEY, mySettings!!.getScratch())
        properties.setValue(TOOLCHAIN_KEY, mySettings!!.selectedToolchain.selectedIndex, RustChannel.DEFAULT.index)
        properties.setValue(EDITION_KEY, mySettings!!.selectedEdition.selectedIndex, Edition.DEFAULT.index)
    }

    override fun reset() {
        project.toolchain?.hasCargoExecutable("cargo-play")?.let {
            mySettings!!.cargoPlayInstalled.isEnabled = it
        }
        project.toolchain?.hasCargoExecutable("cargo-expand")?.let {
            mySettings!!.cargoExpandInstalled.isEnabled = it
        }

        mySettings!!.selectedToolchain.selectedIndex = properties.getInt(TOOLCHAIN_KEY, RustChannel.DEFAULT.index)
        mySettings!!.selectedEdition.selectedIndex = properties.getInt(EDITION_KEY, Edition.DEFAULT.index)
        mySettings!!.setScratch(Settings.getScratchDefault())
    }

    override fun getDisplayName(): String {
        return "Playground"
    }

    override fun getId(): String {
        return "Rust.Playground.Settings"
    }

    override fun dispose() {
        mySettings = null
    }
}