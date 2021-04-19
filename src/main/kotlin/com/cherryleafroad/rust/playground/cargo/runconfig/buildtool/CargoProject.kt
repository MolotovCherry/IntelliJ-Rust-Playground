package com.cherryleafroad.rust.playground.cargo.runconfig.buildtool

import com.intellij.openapi.project.Project
import java.nio.file.Path

data class CargoProject(
    val workingDirectory: Path,
    val project: Project
)
