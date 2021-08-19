package com.cherryleafroad.rust.playground.services

import com.cherryleafroad.rust.playground.utils.CargoPlayPath
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import org.rust.cargo.project.settings.toolchain
import org.rust.cargo.toolchain.tools.rustc
import java.nio.file.Paths
import java.util.concurrent.Callable

class CargoPlayProjectService(project: Project) {
    lateinit var cargoPlayPath: CargoPlayPath
    var sysroot: String? = null

    init {
        sysroot = ApplicationManager.getApplication().executeOnPooledThread(Callable<String?> {
            project.toolchain?.rustc()?.getSysroot(Paths.get("").toAbsolutePath())
        }).get()
    }

    fun setCargoPlayPath(srcs: List<String>, cwd: String) {
        cargoPlayPath = CargoPlayPath(srcs, cwd)
    }
}