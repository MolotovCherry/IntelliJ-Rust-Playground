/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package com.cherryleafroad.rust.playground.cargo.runconfig.buildtool

import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolderEx
import com.intellij.openapiext.isUnitTestMode
import com.intellij.util.concurrency.FutureResult
import org.rust.cargo.runconfig.RsExecutableRunner.Companion.artifact
import org.rust.cargo.runconfig.buildtool.CargoBuildManager.showBuildNotification
import org.rust.cargo.runconfig.buildtool.CargoBuildResult
import org.rust.cargo.runconfig.buildtool.notifyProcessNotStarted
import org.rust.cargo.toolchain.impl.CompilerArtifactMessage
import java.nio.file.Path
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class CargoBuildContext(
    val cargoProject: CargoProject,
    val environment: ExecutionEnvironment,
    val taskName: String,
    val progressTitle: String,
    val isTestBuild: Boolean
) {
    val project = cargoProject.project
    val buildId: Any = if (isUnitTestMode) CargoBuildManager.testBuildId ?: Any() else Any()
    val workingDirectory: Path get() = cargoProject.workingDirectory

    val result: FutureResult<CargoBuildResult> = FutureResult()

    private val buildSemaphore: Semaphore = project.getUserData(BUILD_SEMAPHORE_KEY)
        ?: (project as UserDataHolderEx).putUserDataIfAbsent(BUILD_SEMAPHORE_KEY, Semaphore(1))

    @Volatile
    var indicator: ProgressIndicator? = null
    @Volatile
    var processHandler: ProcessHandler? = null

    val started: Long = System.currentTimeMillis()
    @Volatile
    var finished: Long = started
    private val duration: Long get() = finished - started

    val errors: AtomicInteger = AtomicInteger()
    val warnings: AtomicInteger = AtomicInteger()

    @Volatile
    var artifact: CompilerArtifactMessage? = null

    fun waitAndStart(): Boolean {
        indicator?.pushState()
        try {
            indicator?.text = "Waiting for the current build to finish..."
            indicator?.text2 = ""
            while (true) {
                indicator?.checkCanceled()
                try {
                    if (buildSemaphore.tryAcquire(100, TimeUnit.MILLISECONDS)) break
                } catch (e: InterruptedException) {
                    throw ProcessCanceledException()
                }
            }
        } catch (e: ProcessCanceledException) {
            canceled()
            return false
        } finally {
            indicator?.popState()
        }
        return true
    }

    fun finished(isSuccess: Boolean) {
        val isCanceled = indicator?.isCanceled ?: false

        environment.artifact = artifact.takeIf { isSuccess && !isCanceled }

        finished = System.currentTimeMillis()
        buildSemaphore.release()

        val finishMessage: String
        val finishDetails: String?

        val errors = errors.get()
        val warnings = warnings.get()

        // We report successful builds with errors or warnings correspondingly
        val messageType = if (isCanceled) {
            finishMessage = "$taskName canceled"
            finishDetails = null
            MessageType.INFO
        } else {
            val hasWarningsOrErrors = errors > 0 || warnings > 0
            finishMessage = if (isSuccess) "$taskName finished" else "$taskName failed"
            finishDetails = if (hasWarningsOrErrors) {
                val errorsString = if (errors == 1) "error" else "errors"
                val warningsString = if (warnings == 1) "warning" else "warnings"
                "$errors $errorsString and $warnings $warningsString"
            } else {
                null
            }

            when {
                !isSuccess -> MessageType.ERROR
                hasWarningsOrErrors -> MessageType.WARNING
                else -> MessageType.INFO
            }
        }

        result.set(CargoBuildResult(
            succeeded = isSuccess,
            canceled = isCanceled,
            started = started,
            duration = duration,
            errors = errors,
            warnings = warnings,
            message = finishMessage
        ))

        showBuildNotification(project, messageType, finishMessage, finishDetails, duration)
    }

    fun canceled() {
        finished = System.currentTimeMillis()

        result.set(CargoBuildResult(
            succeeded = false,
            canceled = true,
            started = started,
            duration = duration,
            errors = errors.get(),
            warnings = warnings.get(),
            message = "$taskName canceled"
        ))

        environment.notifyProcessNotStarted()
    }

    companion object {
        private val BUILD_SEMAPHORE_KEY: Key<Semaphore> = Key.create("BUILD_SEMAPHORE_KEY")
    }
}
