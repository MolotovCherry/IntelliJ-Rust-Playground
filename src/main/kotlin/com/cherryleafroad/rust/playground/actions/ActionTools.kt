package com.cherryleafroad.rust.playground.actions

import com.cherryleafroad.rust.playground.parser.Parser
import com.cherryleafroad.rust.playground.runconfig.RustScratchCommandLine
import com.cherryleafroad.rust.playground.utils.Helpers
import com.intellij.ide.scratch.ScratchUtil
import com.intellij.openapi.actionSystem.AnActionEvent
import org.rust.cargo.project.settings.toolchain
import org.rust.lang.core.psi.isRustFile
import org.rust.openapiext.psiFile

object ActionTools {
    fun actionPerformed(event: AnActionEvent, clean: Boolean = false) {
        val project = event.project!!

        val doc = event.dataContext.psiFile?.virtualFile ?: return

        // this could execute with a shortcut for example
        val isRust = doc.isRustFile
        val isScratch = ScratchUtil.isScratch(doc)
        val toolchainExists = project.toolchain != null
        // this COULD trigger if you do a shortcut for example
        if (!isRust || !isScratch || !toolchainExists) {
            return
        }

        // don't execute on unsupported IDE
        if (Helpers.executionUnsupported(project)) {
            return
        }

        val cargoPlayInstalled = Helpers.checkAndNotifyCargoPlayInstallation(project)
        if (project.toolchain != null && cargoPlayInstalled) {
            val fileName = doc.name

            val results = Parser.parseOptions(project, doc, clean) ?: return

            val commandLine = RustScratchCommandLine(results)
            commandLine.run(project, "Play $fileName")
        }
    }
}
