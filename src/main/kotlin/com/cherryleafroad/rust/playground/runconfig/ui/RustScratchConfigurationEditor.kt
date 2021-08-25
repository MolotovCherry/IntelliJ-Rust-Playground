package com.cherryleafroad.rust.playground.runconfig.ui

import com.cherryleafroad.rust.playground.runconfig.RustScratchConfiguration
import com.cherryleafroad.rust.playground.runconfig.toolchain.BacktraceMode
import com.intellij.execution.ExecutionBundle
import com.intellij.execution.configuration.EnvironmentVariablesComponent
import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
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
import javax.swing.JComponent
import javax.swing.JTextField

class RustScratchConfigurationEditor(val project: Project): SettingsEditor<RustScratchConfiguration>() {
    private val command: JTextField = JTextField()

    private val withSudo = CheckBox(
        if (SystemInfo.isWindows) "Run with Administrator privileges" else "Run with root privileges",
        false
    )

    private val workingDirectory: LabeledComponent<TextFieldWithBrowseButton> = WorkingDirectoryComponent()

    private val backtraceMode = ComboBox<BacktraceMode>().apply {
        BacktraceMode.values()
            .sortedBy { it.index }
            .forEach { addItem(it) }
    }

    private val environmentVariables = EnvironmentVariablesComponent()

    override fun resetEditorFrom(configuration: RustScratchConfiguration) {
        backtraceMode.item = configuration.runConfig.backtraceMode
        environmentVariables.envData = EnvironmentVariablesData.DEFAULT.with(configuration.runConfig.env)

        val quotedSources = configuration.runConfig.srcs.map {
            if (it.contains(" ")) {
                "\"$it\""
            } else {
                it
            }
        }

        val quotedArgs = configuration.runConfig.args.map {
            if (it.contains(" ")) {
                "\"$it\""
            } else {
                it
            }
        }

        command.text = "${configuration.runConfig.options.joinToString(" ")} ${quotedSources.joinToString(" ")}".trim()
        command.text += " ${quotedArgs.joinToString(" ")}".trimEnd()
        
        workingDirectory.component.text = configuration.runConfig.workingDirectory

        withSudo.isSelected = configuration.runConfig.withSudo
    }

    override fun applyEditorTo(configuration: RustScratchConfiguration) {
        configuration.runConfig.command = "play"
        configuration.runConfig.backtraceMode = backtraceMode.item
        configuration.runConfig.env = environmentVariables.envData.envs

        val (options, sources, args) = splitPlayCommand(command.text)
        configuration.runConfig.options = options
        configuration.runConfig.srcs = sources
        configuration.runConfig.args = args

        configuration.runConfig.workingDirectory = workingDirectory.component.text
        configuration.runConfig.withSudo = withSudo.isSelected
        
        configuration.commandConfiguration.fromRunConfiguration(configuration.runConfig)
    }

    override fun createEditor(): JComponent = panel {
        labeledRow("Play &Command:", command) {
            command(CCFlags.pushX, CCFlags.growX)
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

fun argSwitchInside(value: String): Boolean {
    val s = value.split(" ")
    return s.contains("--")
}

fun splitPlayCommand(command: String): Triple<List<String>, List<String>, List<String>> {
    val options = mutableListOf<String>()
    val sources = mutableListOf<String>()
    val args = mutableListOf<String>()

    var argSwitch = false

    val r = Regex("[^\\s\"']+|\"[^\"]*\"|'[^']*'")
    r.findAll(command).forEach beg@ {
        if (argSwitch) {
            // special handling to allow "multiple space" quotation args since by default quotes are put in \"
            if (it.value.startsWith("\"") && it.value.endsWith("\"")) {
                // add as a chunk
                args.add(it.value.removeSurrounding("\""))
            } else {
                // don't change the contents
                args.add(it.value)
            }

            return@beg
        }

        if ((it.value.startsWith("--") ||
            it.value.startsWith("-")) &&
            !argSwitch && !argSwitchInside(it.value)
        ) {
            options.add(it.value)
        } else if (argSwitchInside(it.value)) {
            argSwitch = true
            args.add(it.value)
            return@beg
        } else if (options.lastIndex != -1 && options[options.lastIndex].endsWith("=")) {
            // anything ending in = means the last option should be tied with it
            // also solves --foobar="foo" where "foo" got split separately
            // so recombine them
            options[options.lastIndex] += it.value
        } else {
            sources.add(it.value.removeSurrounding("\"").removeSurrounding("\'"))
        }
    }

    return Triple(options, sources, args)
}
