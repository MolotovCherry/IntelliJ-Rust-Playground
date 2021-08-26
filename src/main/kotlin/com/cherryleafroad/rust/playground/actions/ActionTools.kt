package com.cherryleafroad.rust.playground.actions

import com.cherryleafroad.rust.playground.kargoplay.KargoPlay
import com.cherryleafroad.rust.playground.runconfig.runtime.CommandConfiguration
import com.cherryleafroad.rust.playground.runconfig.runtime.processPlayOptions
import com.cherryleafroad.rust.playground.services.Settings
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

        val settings = Settings.getInstance()
        val scratch = settings.scratches[doc.path]
        settings.global.runtime.currentScratch = scratch
        settings.global.runtime.clean = clean
        settings.global.runtime.scratchFile = doc

        val cargoPlayInstalled = Helpers.checkAndNotifyCargoPlayInstallation(project)
        // if we selected the expand feature, then cargo-expand needs to be installed
        if (scratch.expand) {
            val installed = Helpers.checkAndNotifyCargoExpandInstalled(project)
            if (!installed) {
                return
            }
        }

        if (project.toolchain != null && cargoPlayInstalled) {
            val fileName = doc.name

            if (settings.plugin.kargoPlay) {
                KargoPlay.run()
            } else {
                processPlayOptions()
            }

            val commandLine = CommandConfiguration.fromScratch(doc).toRustScratchCommandLine()
            commandLine.run(project, "Play $fileName")
        }
    }
}
