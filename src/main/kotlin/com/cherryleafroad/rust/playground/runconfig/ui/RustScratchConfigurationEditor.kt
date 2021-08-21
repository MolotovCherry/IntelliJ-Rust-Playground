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
        backtraceMode.selectedIndex = configuration.commandConfiguration.backtraceMode.index
        environmentVariables.envData = configuration.commandConfiguration.env

        configuration.commandConfiguration.runtime.apply {
            val quotedSources = sources.map {
                if (it.contains(" ")) {
                    "\"$it\""
                } else {
                    it
                }
            }

            command.text = "${options.joinToString(" ")} ${quotedSources.joinToString(" ")}".trim()
            if (args.isNotEmpty()) {
                command.text += " ${args.joinToString(" ")}"
            }
        }

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

        val (options, sources, args) = splitPlayCommand(command.text)
        configuration.commandConfiguration.runtime.options = options
        configuration.commandConfiguration.runtime.sources = sources
        configuration.commandConfiguration.runtime.args = args
        configuration.commandConfiguration.args = options + sources + args

        configuration.commandConfiguration.workingDirectory = Paths.get(workingDirectory.component.text)

        configuration.commandConfiguration.withSudo = withSudo.isSelected
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

    // don't do any regex parsing on actual args since it may remove chars
    val unparsedArgs = command.split(" ")

    val r = Regex("[^\\s\"']+|\"[^\"]*\"|'[^']*'")
    run beg@ {
        r.findAll(command).forEach { match ->
            if ((match.value.startsWith("--") ||
                match.value.startsWith("-")) &&
                !argSwitch && !argSwitchInside(match.value)
            ) {
                options.add(match.value)
            } else if (argSwitchInside(match.value)) {
                return@beg
            } else if (options.lastIndex != -1 && options[options.lastIndex].endsWith("=")) {
                // anything ending in = means the last option should be tied with it
                // also solves --foobar="foo" where "foo" got split separately
                // so recombine them
                options[options.lastIndex] += match.value
            } else {
                sources.add(match.value.removeSurrounding("\"").removeSurrounding("\'"))
            }
        }
    }

    // add unparsed args
    unparsedArgs.forEach {
        if (it == "--") {
            argSwitch = true
        }

        if (argSwitch || it == "--") {
            args.add(it)
        }
    }

    return Triple(options, sources, args)
}
