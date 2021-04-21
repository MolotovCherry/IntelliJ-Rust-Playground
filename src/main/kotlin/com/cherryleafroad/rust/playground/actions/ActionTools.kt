package com.cherryleafroad.rust.playground.actions

import com.cherryleafroad.rust.playground.Helpers
import com.cherryleafroad.rust.playground.cargo.runconfig.CargoCommandRunner
import com.cherryleafroad.rust.playground.cargo.runconfig.buildtool.CargoBuildConfiguration
import com.cherryleafroad.rust.playground.cargo.runconfig.buildtool.CargoBuildManager
import com.cherryleafroad.rust.playground.cargo.runconfig.buildtool.CargoProject
import com.cherryleafroad.rust.playground.cargo.toolchain.CargoCommandLine
import com.intellij.execution.ExecutorRegistry
import com.intellij.execution.RunManager
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.impl.RunManagerImpl
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.ide.scratch.ScratchUtil
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.rust.cargo.project.model.cargoProjects
import org.rust.cargo.runconfig.buildtool.CargoBuildResult
import org.rust.cargo.runconfig.command.CargoCommandConfiguration
import org.rust.cargo.runconfig.command.CargoCommandConfigurationType
import org.rust.lang.core.psi.isRustFile
import org.rust.openapiext.document
import org.rust.openapiext.psiFile

object ActionTools {
    private fun makeBuilder(project: Project, cmdLine: CargoCommandLine, fileName: String, cmd: List<String>): CargoBuildConfiguration {
        val configurationType = CargoCommandConfigurationType.getInstance()
        val factory = configurationType.factory
        val config = factory.createTemplateConfiguration(project) as CargoCommandConfiguration

        config.apply {
            name = "Build $fileName"
            channel = cmdLine.channel
            command = cmd.joinToString(" ")
            requiredFeatures = cmdLine.requiredFeatures
            allFeatures = cmdLine.allFeatures
            emulateTerminal = cmdLine.emulateTerminal
            backtrace = cmdLine.backtraceMode
            workingDirectory = cmdLine.workingDirectory
            env = cmdLine.environmentVariables
            isRedirectInput = cmdLine.redirectInputFrom != null
            redirectInputPath = cmdLine.redirectInputFrom?.path
        }

        val executor = ExecutorRegistry.getInstance().getExecutorById(DefaultRunExecutor.EXECUTOR_ID)!!
        val runner = ProgramRunner.findRunnerById(CargoCommandRunner.RUNNER_ID)!!
        val runManager = RunManager.getInstance(project) as RunManagerImpl

        val settings = RunnerAndConfigurationSettingsImpl(runManager, config)
        val environment = ExecutionEnvironment(executor, runner, settings, project)
        return CargoBuildConfiguration(config, environment)
    }

    fun actionPerformed(event: AnActionEvent) {
        val project = event.project!!

        val doc = event.dataContext.psiFile?.virtualFile ?: return

        // this could execute with a shortcut for example
        val isRust = doc.isRustFile
        val isScratch = ScratchUtil.isScratch(doc)
        // this COULD trigger if you do a shortcut for example
        if (!isRust || !isScratch) {
            return
        }

        val cargoPlayInstalled = Helpers.checkCargoPlayInstalled(project)
        if (cargoPlayInstalled) {
            val code = doc.document!!.text
            val cargoProject = project.cargoProjects.allProjects.firstOrNull() ?: return

            val cwd = doc.toNioPath().parent
            val fileName = doc.name

            Helpers.parseOptions(doc)
            /*val results = Helpers.parseScratch(fileName, code)

            // we need our own to patch a bug in the cmd arg processing # see setCmd
            val commandLine = CargoCommandLine(
                "play",
                cwd,
                results.runCmd,
                channel = results.toolchain
            )

            // make a copy of our own to be compatible with default builder


            val buildProject = CargoProject(
                project = project,
                workingDirectory = cwd
            )

            val builder = makeBuilder(
                project,
                commandLine,
                fileName,
                results.buildCmd
            )

            var cleanRunBuild: CargoBuildConfiguration? = null
            if (results.cleanAndRun) {
                cleanRunBuild = makeBuilder(
                    project,
                    commandLine,
                    fileName,
                    results.cleanAndRunCmd
                )
            }

            GlobalScope.launch(Dispatchers.Main) {
                var future: CargoBuildResult? = null
                if (results.runBuild) {
                    if (results.cleanAndRun) {
                        val job = GlobalScope.launch(Dispatchers.IO) {
                            CargoBuildManager.build(buildProject, cleanRunBuild!!).get()
                        }
                        job.join()
                    }

                    if (!results.skipBuild) {
                        val job = GlobalScope.launch(Dispatchers.IO) {
                            future = CargoBuildManager.build(buildProject, builder).get()
                        }
                        job.join()
                    }
                }

                if (results.runRun) {
                    // if skipped build, there's no results, but we still want the run
                    if ((future != null && future!!.succeeded) || results.skipBuild) {
                        commandLine.run(cargoProject, "Play $fileName", saveConfiguration = false)
                    }
                }*/


        } else {
            Helpers.cargoPlayInstallNotification(project)
        }
    }
}