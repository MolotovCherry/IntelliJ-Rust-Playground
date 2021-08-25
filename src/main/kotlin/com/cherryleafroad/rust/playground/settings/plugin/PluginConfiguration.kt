package com.cherryleafroad.rust.playground.settings.plugin

import com.cherryleafroad.rust.playground.runconfig.toolchain.Edition
import com.cherryleafroad.rust.playground.runconfig.toolchain.RustChannel
import com.intellij.util.xmlb.annotations.Tag


data class PluginConfiguration(
    @Tag("toolchain")
    var toolchain: RustChannel = RustChannel.DEFAULT,
    @Tag("edition")
    var edition: Edition = Edition.DEFAULT,
    @Tag("scratchText")
    var scratchText: String = DEFAULT_SCRATCH,
    @Tag("kargoPlay")
    var kargoPlay: Boolean = true
) {
    companion object {
        val DEFAULT_SCRATCH = """
            /*
             * Specify external dependencies with //# . It uses the same TOML syntax as Cargo.toml
             *     //# serde_json = "*"
             *
             * For more information, check out the cargo-play docs -
             * You can change the default Rust scratch message in Rust settings page
             */
            
            fn main() {
                println!("Hello, world!");
            }
        """.trimIndent()
    }
}
