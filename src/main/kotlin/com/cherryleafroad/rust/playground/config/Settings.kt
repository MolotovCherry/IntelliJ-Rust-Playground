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
            /* Specify cargo-play features to use with //@ . Use space separators for single args.
             *   _Flags: check(c) clean expand infer(i) quiet(q) release test verbose(v)_
             *
             *     //@ expand release quiet i
             * 
             * Options can be specified on the same //@ line, but need comma separators between opts.
             * src adds additional files to compile with the main file (CWD is scratch directory, so
             * you can include other scratch files easily)
             *   _Options: cargo-option edition(e) mode(m) src_
             *
             *     //@ cargo-option --verbose --quiet, src scratch2.rs scratch3.rs, mode build
             *
             * You can also override the default playground toolchain by using
             *     //@ toolchain <toolchain>
             * Where <Toolchain> is one of (case-insensitive): DEFAULT, STABLE, BETA, NIGHTLY, DEV
             *
             * Full example (note: opts are always at the end)
             *     //@ expand release q, edition 2018, m run, src scratch2.rs scratch3.rs
             *
             * //@ must be the first line, and //$ can be first or second line. Long flags are
             * case-insensitive, where short flags are case sensitive
             *
             * Specify program args //$
             *     //$ --my-arg 5
             *
             * Specify external dependencies with //# . It uses the same TOML syntax as Cargo.toml
             *     //# serde_json = "*"
             *
             * For more information, check out cargo-play docs
             * Change the default Rust scratch message in Rust settings page
             */
            
            fn main() {
                println!("Hello, world!");
            }

        """.trimIndent()
}
