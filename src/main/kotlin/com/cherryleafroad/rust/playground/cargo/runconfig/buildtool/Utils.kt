package com.cherryleafroad.rust.playground.cargo.runconfig.buildtool

import org.rust.cargo.toolchain.CargoCommandLine
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.util.Key

typealias CargoPatch = (CargoCommandLine) -> CargoCommandLine

var ExecutionEnvironment.cargoPatches: List<CargoPatch>
    get() = putUserDataIfAbsent(CARGO_PATCHES, emptyList())
    set(value) = putUserData(CARGO_PATCHES, value)

private val CARGO_PATCHES: Key<List<CargoPatch>> = Key.create("CARGO.PATCHES")
