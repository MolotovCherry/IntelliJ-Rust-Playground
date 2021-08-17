/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package com.cherryleafroad.rust.playground.runconfig

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.AnsiEscapeDecoder
import com.intellij.execution.process.KillableProcessHandler
import com.intellij.openapi.util.Key

/**
 * Same as [com.intellij.execution.process.KillableColoredProcessHandler], but uses [RsAnsiEscapeDecoder].
 */
class RsProcessHandler(
    commandLine: GeneralCommandLine
) : KillableProcessHandler(commandLine), AnsiEscapeDecoder.ColoredTextAcceptor {
    private val decoder: AnsiEscapeDecoder = RsAnsiEscapeDecoder()

    init {
        setShouldDestroyProcessRecursively(true)
    }

    override fun notifyTextAvailable(text: String, outputType: Key<*>) {
        decoder.escapeText(text, outputType, this)
    }

    override fun coloredTextAvailable(text: String, attributes: Key<*>) {
        super.notifyTextAvailable(text, attributes)
    }
}
