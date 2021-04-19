package com.cherryleafroad.rust.playground.cargo.toolchain

import com.intellij.execution.RunManager
import com.intellij.execution.RunManagerEx
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.configuration.EnvironmentVariablesData
import org.rust.cargo.project.model.CargoProject
import org.rust.cargo.project.workspace.CargoWorkspace
import org.rust.cargo.runconfig.command.CargoCommandConfiguration
import org.rust.cargo.runconfig.command.CargoCommandConfigurationType
import org.rust.cargo.runconfig.command.workingDirectory
import org.rust.cargo.toolchain.BacktraceMode
import org.rust.cargo.toolchain.RsCommandLineBase
import org.rust.cargo.toolchain.RustChannel
import java.io.File
import java.nio.file.Path

data class CargoCommandLine(
    override val command: String, // Can't be `enum` because of custom subcommands
    override val workingDirectory: Path, // Note that working directory selects Cargo project as well
    override val additionalArguments: List<String> = emptyList(),
    override val redirectInputFrom: File? = null,
    val backtraceMode: BacktraceMode = BacktraceMode.DEFAULT,
    val channel: RustChannel = RustChannel.DEFAULT,
    val environmentVariables: EnvironmentVariablesData = EnvironmentVariablesData.DEFAULT,
    val requiredFeatures: Boolean = true,
    val allFeatures: Boolean = false,
    val emulateTerminal: Boolean = false
) : RsCommandLineBase() {
    override fun createRunConfiguration(runManager: RunManagerEx, name: String?): RunnerAndConfigurationSettings =
        runManager.createCargoCommandRunConfiguration(this, name)

    /**
     * Splits [additionalArguments] into parts before and after `--`.
     * For `cargo run --release -- foo bar`, returns (["--release"], ["foo", "bar"])
     */
    fun splitOnDoubleDash(): Pair<List<String>, List<String>> =
        org.rust.cargo.util.splitOnDoubleDash(additionalArguments)

    fun prependArgument(arg: String): CargoCommandLine =
        copy(additionalArguments = listOf(arg) + additionalArguments)

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        fun forTargets(
            targets: List<CargoWorkspace.Target>,
            command: String,
            additionalArguments: List<String> = emptyList(),
            usePackageOption: Boolean = true
        ): CargoCommandLine {
            val pkgs = targets.map { it.pkg }
            // Make sure the selection does not span more than one package.
            assert(pkgs.map { it.rootDirectory }.distinct().size == 1)
            val pkg = pkgs.first()

            val targetArgs = targets.distinctBy { it.name }.flatMap { target ->
                when (target.kind) {
                    CargoWorkspace.TargetKind.Bin -> listOf("--bin", target.name)
                    CargoWorkspace.TargetKind.Test -> listOf("--test", target.name)
                    CargoWorkspace.TargetKind.ExampleBin, is CargoWorkspace.TargetKind.ExampleLib ->
                        listOf("--example", target.name)
                    CargoWorkspace.TargetKind.Bench -> listOf("--bench", target.name)
                    is CargoWorkspace.TargetKind.Lib -> listOf("--lib")
                    CargoWorkspace.TargetKind.CustomBuild,
                    CargoWorkspace.TargetKind.Unknown -> emptyList()
                }
            }

            val workingDirectory = if (usePackageOption) {
                pkg.workspace.contentRoot
            } else {
                pkg.rootDirectory
            }

            val commandLineArguments = buildList<String> {
                if (usePackageOption) {
                    add("--package")
                    add(pkg.name)
                }
                addAll(targetArgs)
                addAll(additionalArguments)
            }

            return CargoCommandLine(command, workingDirectory, commandLineArguments)
        }

        fun forTarget(
            target: CargoWorkspace.Target,
            command: String,
            additionalArguments: List<String> = emptyList(),
            usePackageOption: Boolean = true
        ): CargoCommandLine = forTargets(listOf(target), command, additionalArguments, usePackageOption)

        fun forProject(
            cargoProject: CargoProject,
            command: String,
            additionalArguments: List<String> = emptyList(),
            channel: RustChannel = RustChannel.DEFAULT
        ): CargoCommandLine = CargoCommandLine(
            command,
            workingDirectory = cargoProject.workingDirectory,
            additionalArguments = additionalArguments,
            channel = channel
        )

        fun forPackage(
            cargoPackage: CargoWorkspace.Package,
            command: String,
            additionalArguments: List<String> = emptyList()
        ): CargoCommandLine = CargoCommandLine(
            command,
            workingDirectory = cargoPackage.workspace.manifestPath.parent,
            additionalArguments = listOf("--package", cargoPackage.name) + additionalArguments
        )
    }

}

fun RunManager.createCargoCommandRunConfiguration(cmd: CargoCommandLine, name: String? = null): RunnerAndConfigurationSettings {
    val runnerAndConfigurationSettings = createConfiguration(name ?: cmd.command,
        CargoCommandConfigurationType.getInstance().factory)
    val configuration = runnerAndConfigurationSettings.configuration as CargoCommandConfiguration

    configuration.apply {
        channel = cmd.channel

        // patch to not do \" \" which ruins the options
        val newCmd = mutableListOf(cmd.command)
        newCmd.addAll(cmd.additionalArguments)
        command = newCmd.joinToString(" ")
        // end patch

        requiredFeatures = cmd.requiredFeatures
        allFeatures = cmd.allFeatures
        emulateTerminal = cmd.emulateTerminal
        backtrace = cmd.backtraceMode
        workingDirectory = cmd.workingDirectory
        env = cmd.environmentVariables
        isRedirectInput = cmd.redirectInputFrom != null
        redirectInputPath = cmd.redirectInputFrom?.path
    }

    return runnerAndConfigurationSettings
}
