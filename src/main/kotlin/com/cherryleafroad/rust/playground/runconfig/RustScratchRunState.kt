package com.cherryleafroad.rust.playground.runconfig

import com.cherryleafroad.rust.playground.runconfig.console.RustScratchConsoleBuilder
//import com.cherryleafroad.rust.playground.runconfig.filters.RsBacktraceFilter
import com.cherryleafroad.rust.playground.runconfig.filters.RsConsoleFilter
import com.cherryleafroad.rust.playground.runconfig.filters.RsExplainFilter
import com.cherryleafroad.rust.playground.runconfig.filters.RsPanicFilter
import com.cherryleafroad.rust.playground.utils.createGeneralCommandLine
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
        val rootDir = VirtualFileManager.getInstance().findFileByNioPath(runConfiguration.workingDirectory)!!
        val sourceScratch = if (runConfiguration.isPlayRun) {
            runConfiguration.parserResults.src[0]
        } else {
            ""
        }

        return mutableListOf<Filter>().apply {
            add(RsExplainFilter())
            add(RsConsoleFilter(project, rootDir, runConfiguration.isPlayRun, sourceScratch))
            add(RsPanicFilter(project, rootDir, runConfiguration.isPlayRun, sourceScratch))
            //add(RsBacktraceFilter(project, cargoPlayDir))
        }
    }

    override fun startProcess(): ProcessHandler {
        val commandLine = createGeneralCommandLine(project, runConfiguration)!!
        val handler = RsProcessHandler(commandLine)
        ProcessTerminatedListener.attach(handler)
        return handler
    }
}
