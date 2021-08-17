package com.cherryleafroad.rust.playground.runconfig

object CargoConstants {

    const val MANIFEST_FILE = "Cargo.toml"
    const val XARGO_MANIFEST_FILE = "Xargo.toml"
    const val LOCK_FILE = "Cargo.lock"

    const val RUST_BACKTRACE_ENV_VAR = "RUST_BACKTRACE"
    const val CARGO = "cargo"
    const val XARGO = "xargo"

    object ProjectLayout {
        val sources = listOf("src", "examples")
        val tests = listOf("tests", "benches")
        const val target = "target"
    }
}
