# IntelliJ Rust Playground

An IntelliJ plugin allowing you to use a similar playground experience just like [https://play.rust-lang.org/](https://play.rust-lang.org/), except straight from your IDE!

### Features
- Complete integration with official IntelliJ Rust plugin
- Run playground-like scripts directly from your IDE without having to setup any projects
- Supports naming build-dependencies, different toolchains, and all `cargo-play` flags straight from the top of your file (specified in comments)
- Ability to change the Rust default scratch file template to your own

### Usage
- Make or open a cargo project
  - Open a Rust scratch file and go to `Run -> Run in Playground` to execute in Playground
  - `ctrl+comma` can also be used as a shortcut
  - You can also access the `Run in Playground` menu from the `Tools -> Rust` section, or right click menu on the file `Rust -> Run in Playground`
  

`//@` specifies `cargo-play` arguments. It must be the first line in the file.

`//$` specifies your programs arguments. It can be the first or second line in the file.

Case doesn't matter in options or option arguments, except for short flags which are case-sensitive

###`//@` flags
These are separated by spaces,  
Example `//@ i q release`  
Flags **MUST** precede options

| Short | Long    |
| ------| --------|
|       | check   |
| c     | clean   |
|       | expand  |
| i     | infer   |
| q     | quiet   |
|       | release |
|       | test    |
| v     | verbose |

Examples  
`//@ c i q release`

###`//@` options
These must be separated by commas,  
Example `//@ edition 2015, mode build`  
Options **MUST** come last after flags

| Short | Long         | Parameters                          | What it does             |
| ----- | ------------ | --------------------                | ------------------------ |
|       | cargo-option | Any cargo options                   | Sets cargo options       |
| e     | edition      | 2015, 2018                          | Changes rust edition     |
| m     | mode         | Any cargo subcommand                | Changes cargo subcommand |
|       | toolchain    | DEFAULT, STABLE, BETA, NIGHTLY, DEV | Changes playground toolchain for this file (you can also specify default toolchain for all files in playground settings |
|       | src          | Any .rs file                        | Compiles additional .rs files with your open scratch. CWD is the scratch dir, so you can reference multiple scratches easily |


Examples  
`//@ cargo-option --verbose --color=auto, e 2015, m build, src scratch_2.rs scratch_3.rs`

###`//$` usage
Example `//$ --my prog args`

Of course, you can combine both flags and options together. Just space separate flags, then use a comma between options  
`//@ quiet release, edition 2015, mode build`

###`//#` usage
This is part of cargo-play itself. This is used to add build-dependencies. It accepts any valid Cargo-TOML syntax. This MUST be after the `//@` and `//$` configuration lines.  
Example `//# serde_json = "*"`

### Requirements
- [Official Rust Plugin](https://plugins.jetbrains.com/plugin/8182-rust)
- [Cargo cargo-play subcommand](https://github.com/fanzeyi/cargo-play) (it will prompt to install it automatically)
- IntelliJ 2021.1 or newer

### More information
Check out [cargo-play](https://github.com/fanzeyi/cargo-play) for complete non-plugin usage
