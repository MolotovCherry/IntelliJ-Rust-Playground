/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package com.cherryleafroad.rust.playground.runconfig

import com.cherryleafroad.rust.playground.kargoplay.KargoPlay
import com.cherryleafroad.rust.playground.services.Settings
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.AnsiEscapeDecoder
import com.intellij.execution.process.KillableProcessHandler
import com.intellij.openapi.util.Key

/**
 * Same as [com.intellij.execution.process.KillableColoredProcessHandler], but uses [RsAnsiEscapeDecoder].
 */
class RsProcessHandler(
    commandLine: GeneralCommandLine,
    processColors: Boolean = true
) : KillableProcessHandler(commandLine), AnsiEscapeDecoder.ColoredTextAcceptor {
    private val decoder: AnsiEscapeDecoder? = if (processColors) RsAnsiEscapeDecoder() else null

    init {
        setShouldDestroyProcessRecursively(true)
    }

    override fun notifyTextAvailable(text: String, outputType: Key<*>) {
        decoder?.escapeText(text, outputType, this) ?: super.notifyTextAvailable(text, outputType)
    }

    override fun coloredTextAvailable(text: String, attributes: Key<*>) {
        super.notifyTextAvailable(text, attributes)
    }

    override fun notifyProcessTerminated(exitCode: Int) {
        super.notifyProcessTerminated(exitCode)

        // if non-0 exit code, the command had an error
        // so let's make sure project will recompile next time instead of a dry run
        val settings = Settings.getInstance()
        if (settings.plugin.kargoPlay && !settings.global.runtime.currentScratch.directRun) {
            KargoPlay.lastExitCode = exitCode
        }
    }
}
