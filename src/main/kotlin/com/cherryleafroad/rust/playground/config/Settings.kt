package com.cherryleafroad.rust.playground.config

import com.cherryleafroad.rust.playground.parser.Edition
import com.cherryleafroad.rust.playground.parser.RustChannel
import com.intellij.ide.util.PropertiesComponent

object Settings {
    private val properties = PropertiesComponent.getInstance()

    fun getScratchDefault(): String {
        return properties.getValue(SCRATCH_KEY, DEFAULT_TEXT)
    }

    fun getSelectedToolchain(): RustChannel {
        return RustChannel.fromIndex(properties.getInt(TOOLCHAIN_KEY, RustChannel.DEFAULT.index))
    }

    fun getSelectedEdition(): Edition {
        return Edition.fromIndex(properties.getInt(EDITION_KEY, Edition.DEFAULT.index))
    }

    const val TOOLCHAIN_KEY: String = "rust_toolchain"
    const val SCRATCH_KEY: String = "scratch_default"
    const val EDITION_KEY: String = "rust_edition"

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