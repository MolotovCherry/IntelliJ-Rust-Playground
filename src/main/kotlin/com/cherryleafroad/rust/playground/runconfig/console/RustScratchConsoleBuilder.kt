/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package com.cherryleafroad.rust.playground.runconfig.console

import com.intellij.execution.filters.TextConsoleBuilderImpl
import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope

open class RustScratchConsoleBuilder(project: Project, scope: GlobalSearchScope) : TextConsoleBuilderImpl(project, scope) {
    override fun createConsole(): ConsoleView = RustScratchConsoleView(project, scope, isViewer, true)
}
