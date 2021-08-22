package com.cherryleafroad.rust.playground.config

import com.cherryleafroad.rust.playground.config.ui.SettingsForm
import com.intellij.openapi.Disposable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import org.rust.cargo.project.settings.toolchain
import javax.swing.JComponent

class SettingsConfigurable(private val project: Project) : SearchableConfigurable, Disposable {
    private var mySettings: SettingsForm? = SettingsForm()
        get() = field!!
    private val origHash: Int = Settings.getScratchOrDefault().hashCode()

    override fun createComponent(): JComponent {
        reset()
        return mySettings!!.getPanel()
    }

    override fun isModified(): Boolean {
        val scratch = mySettings!!.getScratch().hashCode() != origHash
        val toolchain = mySettings!!.selectedToolchain.selectedIndex != Settings.TOOLCHAIN.get().index
        val edition = mySettings!!.selectedEdition.selectedIndex != Settings.EDITION.get().index
        return scratch || toolchain || edition
    }

    override fun apply() {
        Settings.SCRATCH.set(mySettings!!.getScratch())
        Settings.TOOLCHAIN.set(mySettings!!.selectedToolchain.selectedIndex)
        Settings.EDITION.set(mySettings!!.selectedEdition.selectedIndex)
    }

    override fun reset() {
        project.toolchain?.hasCargoExecutable("cargo-play")?.let {
            mySettings!!.cargoPlayInstalled.isEnabled = it
        }
        project.toolchain?.hasCargoExecutable("cargo-expand")?.let {
            mySettings!!.cargoExpandInstalled.isEnabled = it
        }

        mySettings!!.selectedToolchain.selectedIndex = Settings.TOOLCHAIN.get().index
        mySettings!!.selectedEdition.selectedIndex = Settings.EDITION.get().index
        mySettings!!.setScratch(Settings.getScratchOrDefault())
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