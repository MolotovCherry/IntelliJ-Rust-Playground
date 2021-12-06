/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package com.cherryleafroad.rust.playground.runconfig.filters

import com.cherryleafroad.rust.playground.runconfig.constants.RsConstants
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.rust.cargo.project.workspace.CargoWorkspace
import org.rust.lang.core.psi.RsCodeFragmentFactory
import org.rust.lang.core.psi.RsFile
import org.rust.lang.core.psi.ext.RsNamedElement
import org.rust.lang.core.resolve.indexes.RsLangItemIndex
import org.rust.lang.core.resolve.splitAbsolutePath
import org.rust.lang.core.resolve2.defMapService
import org.rust.lang.core.resolve2.getOrUpdateIfNeeded
import org.rust.lang.core.resolve2.isNewResolveEnabled
import org.rust.openapiext.findFileByMaybeRelativePath
import java.io.File

object FilterUtils {
    /**
     * Normalizes function path:
     * - Removes angle brackets from the element path, including enclosed contents when necessary.
     * - Removes closure markers.
     * Examples:
     * - <core::option::Option<T>>::unwrap -> core::option::Option::unwrap
     * - std::panicking::default_hook::{{closure}} -> std::panicking::default_hook
     */
    fun normalizeFunctionPath(function: String): String {
        var str = function
        while (str.endsWith("::{{closure}}")) {
            str = str.substringBeforeLast("::")
        }
        while (true) {
            val range = str.findAngleBrackets() ?: break
            val idx = str.indexOf("::", range.first + 1)
            str = if (idx < 0 || idx > range.last) {
                str.removeRange(range)
            } else {
                str.removeRange(IntRange(range.last, range.last))
                    .removeRange(IntRange(range.first, range.first))
            }
        }
        return str
    }

    /**
     * Finds the range of the first matching angle brackets within the string.
     */
    private fun String.findAngleBrackets(): IntRange? {
        var start = -1
        var counter = 0
        loop@ for ((index, char) in this.withIndex()) {
            when (char) {
                '<' -> {
                    if (start < 0) {
                        start = index
                    }
                    counter += 1
                }
                '>' -> counter -= 1
                else -> continue@loop
            }
            if (counter == 0) {
                return IntRange(start, index)
            }
        }
        return null
    }

    fun rewriteCargoPlayPaths(project: Project, path: String, sourceScratches: List<String>, isPlayRun: Boolean, cargoProjectDir: VirtualFile): Pair<String, VirtualFile> {
        var nPath = path
        var vfile: VirtualFile = cargoProjectDir
        var matched = false

        // rewrite main.rs to correct local scratch file if isPlayRun
        if (isPlayRun) {
            // main.rs == sourceScratch
            // src/main.rs, strip path first
            val split = path.split("/")
            val name = File(path).name

            // most likely it's the actual src cargo dir
            if (split.size == 2 && split[0] == "src") {
                if (name == RsConstants.MAIN_RS_FILE) {
                    // get the directory for the file, try absolute first then relative
                    File(sourceScratches[0]).parent?.let {
                        vfile = cargoProjectDir.findFileByMaybeRelativePath(it) ?: cargoProjectDir
                    }
                    nPath = File(sourceScratches[0]).name
                    matched = true
                } else {
                    // skip the main.rs file
                    for (i in 1 until sourceScratches.size) {
                        val fname = File(sourceScratches[i])
                        if (fname.name == name) {
                            fname.parent?.let {
                                vfile = cargoProjectDir.findFileByMaybeRelativePath(it) ?: cargoProjectDir
                            }
                            matched = true
                            break
                        }
                    }

                    nPath = name
                }

                if (matched) {
                    // hide that "not part of a cargo project" notification
                    val fullPath = vfile.findFileByRelativePath(nPath)?.path
                    val key = "org.rust.hideNoCargoProjectNotifications$fullPath"
                    // hide no cargo project notification for opened files
                    PropertiesComponent.getInstance(project).setValue(key, true)
                }
            }
        }

        return Pair(nPath, vfile)
    }
}

fun resolveStringPath(path: String, workspace: CargoWorkspace, project: Project): Pair<RsNamedElement, CargoWorkspace.Package>? {
    val (pkgName, crateRelativePath) = splitAbsolutePath(path) ?: return null
    val pkg = workspace.findPackageByName(pkgName) ?: run {
        return if (ApplicationManager.getApplication().isUnitTestMode) {
            // Allows to set a fake path for some item in tests via
            // lang attribute, e.g. `#[lang = "std::iter::Iterator"]`
            RsLangItemIndex.findLangItem(project, path)?.let { it to workspace.packages.first() }
        } else {
            null
        }
    }

    val el = pkg.targets.asSequence()
        .mapNotNull { RsCodeFragmentFactory(project).createCrateRelativePath(crateRelativePath, it) }
        .filter {
            if (!project.isNewResolveEnabled) return@filter true
            val crateRoot = it.containingFile.context as RsFile
            val crateId = crateRoot.containingCrate?.id ?: return@filter false
            // ignore e.g. test/bench non-workspace crates
            project.defMapService.getOrUpdateIfNeeded(crateId) != null
        }
        .mapNotNull { it.reference?.resolve() }
        .filterIsInstance<RsNamedElement>()
        .firstOrNull() ?: return null
    return el to pkg
}
