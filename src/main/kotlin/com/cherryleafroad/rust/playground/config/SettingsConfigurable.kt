package com.cherryleafroad.rust.playground.config

import com.cherryleafroad.rust.playground.config.Settings.SCRATCH_KEY
import com.cherryleafroad.rust.playground.config.Settings.TOOLCHAIN
import com.cherryleafroad.rust.playground.config.ui.SettingsForm
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
        val toolchain = mySettings!!.selectedToolchain.selectedIndex != properties.getInt(TOOLCHAIN, RustChannel.DEFAULT.index)
        return scratch || toolchain
    }

    override fun apply() {
        properties.setValue(SCRATCH_KEY, mySettings!!.getScratch())
        properties.setValue(TOOLCHAIN, mySettings!!.selectedToolchain.selectedIndex, RustChannel.DEFAULT.index)
    }

    override fun reset() {
        project.toolchain?.hasCargoExecutable("cargo-play")?.let {
            mySettings!!.cargoPlayInstalled.isEnabled = it
        }

        mySettings!!.selectedToolchain.selectedIndex = properties.getInt(TOOLCHAIN, RustChannel.DEFAULT.index)
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
