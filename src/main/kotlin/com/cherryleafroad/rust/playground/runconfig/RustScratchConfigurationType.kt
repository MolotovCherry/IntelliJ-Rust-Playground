package com.cherryleafroad.rust.playground.runconfig

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationTypeBase
import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.project.Project
import org.rust.ide.icons.RsIcons

class RustScratchConfigurationType : ConfigurationTypeBase(
    "RustScratchRunConfiguration",
    "Rust Scratch",
    "Rust scratch run configuration",
    RsIcons.RUST
) {
    init {
        addFactory(RustScratchRunConfigurationFactory(this))
    }

    val factory: ConfigurationFactory get() = configurationFactories.single()

    companion object {
        fun getInstance(): RustScratchConfigurationType =
            ConfigurationTypeUtil.findConfigurationType(RustScratchConfigurationType::class.java)
    }
}

class RustScratchRunConfigurationFactory(type: RustScratchConfigurationType) : ConfigurationFactory(type) {
    override fun getId(): String = ID

    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        return RustScratchConfiguration(project, "Rust Scratch", this)
    }

    companion object {
        const val ID: String = "Rust Scratch"
    }

    override fun createConfiguration(name: String?, template: RunConfiguration): RunConfiguration {
        val config = super.createConfiguration(name, template) as RustScratchConfiguration
        config.commandConfiguration = config.commandConfiguration.clone()
        return config
    }
}
