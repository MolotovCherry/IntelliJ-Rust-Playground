package com.cherryleafroad.rust.playground.runconfig.ui

import com.cherryleafroad.rust.playground.runconfig.RustScratchConfiguration
import com.cherryleafroad.rust.playground.runconfig.toolchain.BacktraceMode
import com.intellij.execution.ExecutionBundle
import com.intellij.execution.configuration.EnvironmentVariablesComponent
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.SystemInfo
import com.intellij.ui.components.CheckBox
import com.intellij.ui.components.Label
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.LayoutBuilder
import com.intellij.ui.layout.Row
import com.intellij.ui.layout.panel
import java.nio.file.Paths
import javax.swing.JComponent
import javax.swing.JTextField

class RustScratchConfigurationEditor(val project: Project): SettingsEditor<RustScratchConfiguration>() {
    private val options: JTextField = JTextField()
    private val sources: JTextField = JTextField()
    private val args: JTextField = JTextField()

    private val withSudo = CheckBox(
        if (SystemInfo.isWindows) "Run with Administrator privileges" else "Run with root privileges",
        false
    )

    protected val workingDirectory: LabeledComponent<TextFieldWithBrowseButton> = WorkingDirectoryComponent()

    private val backtraceMode = ComboBox<BacktraceMode>().apply {
        BacktraceMode.values()
            .sortedBy { it.index }
            .forEach { addItem(it) }
    }

    private val environmentVariables = EnvironmentVariablesComponent()

    override fun resetEditorFrom(configuration: RustScratchConfiguration) {
        backtraceMode.selectedIndex = configuration.commandConfiguration.backtraceMode.index
        environmentVariables.envData = configuration.commandConfiguration.env

        args.text = configuration.commandConfiguration.runtime.args.joinToString(" ")
        sources.text = configuration.commandConfiguration.runtime.sources.joinToString(" ")
        options.text = configuration.commandConfiguration.runtime.options.joinToString(" ")

        workingDirectory.component.text = configuration.commandConfiguration.workingDirectory.toString()

        withSudo.isSelected = configuration.commandConfiguration.withSudo
    }

    @Throws(ConfigurationException::class)
    override fun applyEditorTo(configuration: RustScratchConfiguration) {
        configuration.commandConfiguration.backtraceMode = BacktraceMode.fromIndex(backtraceMode.selectedIndex)
        configuration.commandConfiguration.env = environmentVariables.envData

        configuration.commandConfiguration.isPlayRun = true
        configuration.commandConfiguration.command = "play"
        configuration.commandConfiguration.isFromRun = true
        val pArgs = args.text.split(" ").filter { it.isNotEmpty() }.toMutableList()
        if (pArgs.isNotEmpty()) {
            pArgs.add(0, "--")
        }
        val combinedArgs = (options.text.split(" ") + sources.text.split(" ") + pArgs).filter { it.isNotEmpty() }
        configuration.commandConfiguration.args = combinedArgs
        configuration.commandConfiguration.runtime.sources = sources.text.split(" ").filter { it.isNotEmpty() }
        configuration.commandConfiguration.runtime.options = options.text.split(" ").filter { it.isNotEmpty() }
        configuration.commandConfiguration.runtime.args = args.text.split(" ").filter { it.isNotEmpty() }

        configuration.commandConfiguration.workingDirectory = Paths.get(workingDirectory.component.text)

        configuration.commandConfiguration.withSudo = withSudo.isSelected
    }

    override fun createEditor(): JComponent = panel {
        labeledRow("Play &Options:", options) {
            options(CCFlags.pushX, CCFlags.growX)
        }

        labeledRow("Play &Sources:", sources) {
            sources(CCFlags.pushX, CCFlags.growX)
        }

        labeledRow("Prog &Args:", args) {
            args(CCFlags.pushX, CCFlags.growX)
        }

        row(workingDirectory.label) {
            workingDirectory(growX)
        }

        row(environmentVariables.label) {
            environmentVariables(growX)
        }

        labeledRow("Back&trace:", backtraceMode) { backtraceMode() }

        row { withSudo() }
    }

    private fun LayoutBuilder.labeledRow(labelText: String, component: JComponent, init: Row.() -> Unit) {
        val label = Label(labelText)
        label.labelFor = component
        row(label) { init() }
    }
}

private class WorkingDirectoryComponent : LabeledComponent<TextFieldWithBrowseButton>() {
    init {
        component = TextFieldWithBrowseButton().apply {
            val fileChooser = FileChooserDescriptorFactory.createSingleFolderDescriptor().apply {
                title = ExecutionBundle.message("select.working.directory.message")
            }
            addBrowseFolderListener(null, null, null, fileChooser)
        }
        text = ExecutionBundle.message("run.configuration.working.directory.label")
    }
}
