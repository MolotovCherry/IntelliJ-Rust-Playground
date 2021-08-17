package com.cherryleafroad.rust.playground.runconfig

import com.cherryleafroad.rust.playground.runconfig.console.RustScratchConsoleBuilder
//import com.cherryleafroad.rust.playground.runconfig.filters.RsBacktraceFilter
import com.cherryleafroad.rust.playground.runconfig.filters.RsConsoleFilter
import com.cherryleafroad.rust.playground.runconfig.filters.RsExplainFilter
import com.cherryleafroad.rust.playground.runconfig.filters.RsPanicFilter
import com.cherryleafroad.rust.playground.utils.CargoPlayPath
import com.cherryleafroad.rust.playground.utils.createGeneralCommandLine
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.filters.Filter
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.ide.scratch.ScratchFileService
import com.intellij.ide.scratch.ScratchRootType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.search.GlobalSearchScopes
import java.nio.file.Paths

class RustScratchRunState(
    environment: ExecutionEnvironment,
    val runConfiguration: RustScratchConfiguration
) : CommandLineState(environment) {
    val command = runConfiguration.command
    val project = environment.project

    init {
        val scope = GlobalSearchScopes.executionScope(environment.project, environment.runProfile)
        consoleBuilder = RustScratchConsoleBuilder(environment.project, scope)
        createFilters(environment.project).forEach { consoleBuilder.addFilter(it) }
    }

    fun createFilters(project: Project): List<Filter> {
        val list = mutableListOf<Filter>()
        val cargoPlay = CargoPlayPath(runConfiguration.parserResults.src, runConfiguration.workingDirectory.toString())
        val cargoPlayDir = VirtualFileManager.getInstance().findFileByNioPath(cargoPlay.cargoPlayDir) ?:
        VirtualFileManager.getInstance().findFileByNioPath(
            Paths.get(ScratchFileService.getInstance().getRootPath(ScratchRootType.getInstance()))
        )!!

        list.apply {
            add(RsExplainFilter())
            add(RsConsoleFilter(project, cargoPlayDir))
            add(RsPanicFilter(project, cargoPlayDir))
            //add(RsBacktraceFilter(project, cargoPlayDir))
        }

        return list
    }

    override fun startProcess(): ProcessHandler {
        val commandLine = createGeneralCommandLine(project, runConfiguration)!!
        val handler = RsProcessHandler(commandLine)
        ProcessTerminatedListener.attach(handler)
        return handler
    }
}
