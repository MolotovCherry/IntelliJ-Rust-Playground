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
import com.intellij.openapi.actionSystem.ActionPlaces
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

    fun actionPerformed(event: AnActionEvent, cleanRun: Boolean = false) {
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
            val cargoProject = project.cargoProjects.allProjects.firstOrNull() ?: return

            val cwd = doc.toNioPath().parent
            val fileName = doc.name

            val results = Helpers.parseOptions(doc)

            if (cleanRun) {
                val cleanCmd = mutableListOf(
                    "play", "--mode", "clean", "--cargo-option=\"--color=always\""
                )
                cleanCmd.addAll(results.src)

                val cleanRunCmd = mutableListOf(
                    "--mode", "clean", "--cargo-option=\"--color=always\""
                )
                cleanRunCmd.addAll(results.src)

                val cleanCommandLine = CargoCommandLine(
                    "play",
                    cwd,
                    cleanRunCmd,
                    channel = results.toolchain
                )

                val cleanBuilder = makeBuilder(
                    project,
                    cleanCommandLine,
                    fileName,
                    cleanCmd
                )

                val buildProject = CargoProject(
                    project = project,
                    workingDirectory = cwd
                )

                GlobalScope.launch(Dispatchers.Main) {
                    if (results.onlyRun) {
                        cleanCommandLine.run(cargoProject, "Play $fileName", saveConfiguration = false)
                    } else {
                        val job = GlobalScope.launch(Dispatchers.IO) {
                            CargoBuildManager.build(buildProject, cleanBuilder).get()
                        }
                        job.join()
                    }
                }
                return
            }

            // used sidebar to execute it
            if (event.place == ActionPlaces.PROJECT_VIEW_POPUP) {
                val buildCmd = mutableListOf(
                    "play", "--mode", "build", "--cargo-option=\"--color=always --message-format=json-diagnostic-rendered-ansi\""
                )
                // just in case, to keep the run cmd working
                if (results.infer) {
                    buildCmd.add(3, "--infer")
                }
                buildCmd.addAll(results.src)

                val runCmd = mutableListOf(
                    "--cargo-option=\"--color=always\""
                )
                // just in case, to keep the run cmd working
                if (results.infer) {
                    runCmd.add(0, "--infer")
                }
                runCmd.addAll(results.src)

                val runCommandLine = CargoCommandLine(
                    "play",
                    cwd,
                    runCmd,
                    channel = results.toolchain
                )

                val runBuilder = makeBuilder(
                    project,
                    runCommandLine,
                    fileName,
                    buildCmd
                )

                val buildProject = CargoProject(
                    project = project,
                    workingDirectory = cwd
                )

                GlobalScope.launch(Dispatchers.Main) {
                    if (results.onlyRun) {
                        runCommandLine.run(cargoProject, "Play $fileName", saveConfiguration = false)
                    } else {
                        val job = GlobalScope.launch(Dispatchers.IO) {
                            CargoBuildManager.build(buildProject, runBuilder).get()
                        }
                        job.join()

                        runCommandLine.run(cargoProject, "Play $fileName", saveConfiguration = false)
                    }
                }
                return
            }

            // we need our own to patch a bug in the cmd arg processing # see setCmd
            val commandLine = CargoCommandLine(
                "play",
                cwd,
                results.finalRunCmd,
                channel = results.toolchain
            )

            val buildProject = CargoProject(
                project = project,
                workingDirectory = cwd
            )

            val builder = makeBuilder(
                project,
                commandLine,
                fileName,
                results.finalBuildCmd
            )

            val builder2 = makeBuilder(
                project,
                commandLine,
                fileName,
                results.finalBuildCmd2
            )

            var cleanRunBuild: CargoBuildConfiguration? = null
            if (results.cleanAndRun || results.cleanSingle) {
                cleanRunBuild = makeBuilder(
                    project,
                    commandLine,
                    fileName,
                    results.cleanCmd
                )
            }

            GlobalScope.launch(Dispatchers.Main) {
                var future: CargoBuildResult? = null
                if (results.runBuild || results.runBuild2) {
                    if (results.cleanAndRun || results.cleanSingle) {
                        val job = GlobalScope.launch(Dispatchers.IO) {
                            CargoBuildManager.build(buildProject, cleanRunBuild!!).get()
                        }
                        job.join()

                        if (results.cleanSingle) {
                            return@launch
                        }
                    }

                    if (results.runBuild) {
                        val job = GlobalScope.launch(Dispatchers.IO) {
                            future = CargoBuildManager.build(buildProject, builder).get()
                        }
                        job.join()
                    }

                    if (results.runBuild2) {
                        val job = GlobalScope.launch(Dispatchers.IO) {
                            future = CargoBuildManager.build(buildProject, builder2).get()
                        }
                        job.join()
                    }
                }

                if (results.runRun) {
                    // if skipped build, there's no results, but we still want the run
                    if ((future != null && future!!.succeeded) || !results.runBuild) {
                        commandLine.run(cargoProject, "Play $fileName", saveConfiguration = false)
                    }
                }
            }
        } else {
            Helpers.cargoPlayInstallNotification(project)
        }
    }
}