package com.cherryleafroad.rust.playground.services

import com.cherryleafroad.rust.playground.utils.CargoPlayPath
import com.intellij.openapi.project.Project

class CargoPlayProjectService(project: Project) {
    lateinit var cargoPlayPath: CargoPlayPath

    fun setCargoPlayPath(srcs: List<String>, cwd: String) {
        cargoPlayPath = CargoPlayPath(srcs, cwd)
    }
}