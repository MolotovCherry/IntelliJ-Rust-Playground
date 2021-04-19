/*
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 */

package com.cherryleafroad.rust.playground.cargo.runconfig.buildtool

import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.roots.ProjectModelBuildableElement
import com.intellij.openapi.roots.ProjectModelExternalSource
import org.rust.cargo.runconfig.buildtool.CargoBuildManager.isBuildToolWindowEnabled
import org.rust.cargo.runconfig.command.CargoCommandConfiguration

@Suppress("UnstableApiUsage")
open class CargoBuildConfiguration(
    val configuration: CargoCommandConfiguration,
    val environment: ExecutionEnvironment
) : ProjectModelBuildableElement {
    open val enabled: Boolean get() = configuration.project.isBuildToolWindowEnabled

    override fun getExternalSource(): ProjectModelExternalSource? = null
}
