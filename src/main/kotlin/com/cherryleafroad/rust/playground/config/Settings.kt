package com.cherryleafroad.rust.playground.config

import com.intellij.ide.util.PropertiesComponent

@Suppress("PropertyName")
object Settings {
    private val properties = PropertiesComponent.getInstance()
    private const val prefix: String = "rust_scratch"

    val TOOLCHAIN = ToolchainSetting("$prefix.rust_toolchain", properties)
    val EDITION = EditionSetting("$prefix.rust_edition", properties)
    val SCRATCH = StringSetting("$prefix.scratch_default", properties)

    fun getScratchOrDefault(): String {
        return SCRATCH.get(DEFAULT_TEXT)
    }

    val DEFAULT_TEXT =
        """
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
