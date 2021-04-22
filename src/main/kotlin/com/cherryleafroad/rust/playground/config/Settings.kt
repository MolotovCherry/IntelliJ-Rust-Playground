package com.cherryleafroad.rust.playground.config

import com.intellij.ide.util.PropertiesComponent
import org.rust.cargo.toolchain.RustChannel

object Settings {
    private val properties = PropertiesComponent.getInstance()

    fun getScratchDefault(): String {
        return properties.getValue(SCRATCH_KEY, DEFAULT_TEXT)
    }

    fun getSelectedToolchain(): RustChannel {
        return RustChannel.fromIndex(properties.getInt(TOOLCHAIN, RustChannel.DEFAULT.index))
    }

    const val TOOLCHAIN: String = "rust_toolchain"
    const val SCRATCH_KEY: String = "scratch_default"

    val DEFAULT_TEXT =
        """
            /*
             * For more information, check out the cargo-play docs
             * You can change the default Rust scratch message in Rust settings page
             */
            
            fn main() {
                println!("Hello, world!");
            }

        """.trimIndent()
}