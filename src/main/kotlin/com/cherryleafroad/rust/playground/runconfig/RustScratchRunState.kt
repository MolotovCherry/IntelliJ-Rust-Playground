package com.cherryleafroad.rust.playground.runconfig

//import com.cherryleafroad.rust.playground.runconfig.filters.RsBacktraceFilter
import com.cherryleafroad.rust.playground.runconfig.console.RustScratchConsoleBuilder
import com.cherryleafroad.rust.playground.runconfig.filters.RsConsoleFilter
import com.cherryleafroad.rust.playground.runconfig.filters.RsExplainFilter
import com.cherryleafroad.rust.playground.runconfig.filters.RsPanicFilter
import com.cherryleafroad.rust.playground.services.Settings
import com.cherryleafroad.rust.playground.utils.toPath
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.filters.Filter
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.search.GlobalSearchScopes

class RustScratchRunState(
    environment: ExecutionEnvironment,
    private val runConfiguration: RustScratchConfiguration
) : CommandLineState(environment) {
    val project = environment.project

    init {
        val scope = GlobalSearchScopes.executionScope(environment.project, environment.runProfile)
        consoleBuilder = RustScratchConsoleBuilder(environment.project, scope)
        createFilters(environment.project).forEach { consoleBuilder.addFilter(it) }
    }

    private fun createFilters(project: Project): List<Filter> {
        VirtualFileManager.getInstance().findFileByNioPath(
            if (!runConfiguration.commandConfiguration.isManualRun)
                Settings.getInstance().global.runtime.scratchRoot.toPath()
            else
                runConfiguration.commandConfiguration.workingDirectory.toPath()
        )?.let {
            if (!runConfiguration.commandConfiguration.directRun) {
                return mutableListOf<Filter>().apply {
                    add(RsExplainFilter())
                    add(RsConsoleFilter(project, it, true, runConfiguration.commandConfiguration.filterSrcs))
                    add(RsPanicFilter(project, it, true, runConfiguration.commandConfiguration.filterSrcs))
                    //add(RsBacktraceFilter(project, rootDir, runConfiguration.commandConfiguration.isPlayRun, sourceScratches))
                }
            }
        }

        return listOf()
    }

    override fun startProcess(): ProcessHandler {
        val commandLine = runConfiguration.toGeneralCommandLine(project)!!
        val handler = RsProcessHandler(commandLine, runConfiguration.commandConfiguration.processColors)
        ProcessTerminatedListener.attach(handler)
        return handler
    }
}
