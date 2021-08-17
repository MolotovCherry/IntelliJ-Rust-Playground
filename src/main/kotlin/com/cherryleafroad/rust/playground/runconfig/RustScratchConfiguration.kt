package com.cherryleafroad.rust.playground.runconfig

import com.cherryleafroad.rust.playground.parser.ParserResults
import com.cherryleafroad.rust.playground.runconfig.ui.RustScratchConfigurationEditor
import com.cherryleafroad.rust.playground.utils.readPath
import com.cherryleafroad.rust.playground.utils.readString
import com.cherryleafroad.rust.playground.utils.writePath
import com.cherryleafroad.rust.playground.utils.writeString
import com.intellij.execution.Executor
import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.execution.configurations.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.ide.scratch.ScratchFileService
import com.intellij.ide.scratch.ScratchRootType
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import org.jdom.Element
import java.nio.file.Path
import java.nio.file.Paths

class RustScratchConfiguration(
    project: Project,
    name: String,
    factory: ConfigurationFactory
) : LocatableConfigurationBase<RunProfileState>(project, factory, name) {
    var command: List<String> = listOf()
    // the scratch root directory is always the working directory
    var workingDirectory: Path = Paths.get(ScratchFileService.getInstance().getRootPath(ScratchRootType.getInstance()))
    var env: EnvironmentVariablesData = EnvironmentVariablesData.DEFAULT
    lateinit var parserResults: ParserResults
    var isPlayRun: Boolean = false

    override fun suggestedName(): String = command.joinToString(" ").substringBefore(" ").capitalize()

    override fun writeExternal(element: Element) {
        super.writeExternal(element)
        element.writeString("command", command.joinToString(" "))
        env.writeExternal(element)
    }

    override fun readExternal(element: Element) {
        super.readExternal(element)
        element.readString("command")?.let { command = it.split(" ") }
        env = EnvironmentVariablesData.readExternal(element)
    }

    fun setFromCmd(cmd: RustScratchCommandLine) {
        command = cmd.parserResults.finalCmd
        env = cmd.environmentVariables
        parserResults = cmd.parserResults
        workingDirectory = cmd.parserResults.workingDirectory
        isPlayRun = cmd.parserResults.isPlayRun
    }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
        return RustScratchRunState(environment, this)
    }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> =
        RustScratchConfigurationEditor()
}
