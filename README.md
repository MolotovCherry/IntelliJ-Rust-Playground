# IntelliJ Rust Playground

[![Marketplace](https://img.shields.io/jetbrains/plugin/v/16586?style=plastic&label=Marketplace&color=orange)](https://plugins.jetbrains.com/plugin/16586-rust-playground) ![Downloads](https://img.shields.io/jetbrains/plugin/d/16586?label=Downloads&style=plastic&color=blue)

[![Build](https://github.com/cherryleafroad/IntelliJ-Rust-Playground/actions/workflows/build.yml/badge.svg?event=push)](https://github.com/cherryleafroad/IntelliJ-Rust-Playground/actions/workflows/build.yml) [![IntelliJ Platform Plugin Compatibility](https://github.com/cherryleafroad/IntelliJ-Rust-Playground/actions/workflows/compatibility.yml/badge.svg?event=push)](https://github.com/cherryleafroad/IntelliJ-Rust-Playground/actions/workflows/compatibility.yml)

An IntelliJ plugin allowing you to use a similar playground experience just like [https://play.rust-lang.org/](https://play.rust-lang.org/), except straight from your IDE!

### Features
- Complete integration with official IntelliJ Rust plugin
- Run playground-like scripts directly from your IDE without having to setup any projects
- Supports naming build-dependencies, different toolchains, and all `cargo-play` flags straight from the top of your file (specified in comments)
- Ability to change the Rust default scratch file template to your own

## Beta
_**Want to try the new beta? Follow [instructions here](https://www.jetbrains.com/help/idea/managing-plugins.html#repos) to add the beta channel repo**_  
`https://plugins.jetbrains.com/plugins/list?channel=beta&pluginId=16586`

We need testers to report beta bugs so we can release it into stable as soon as possible. Please test it out!

- New editor toolbar system
- Comments are no longer required! All options can be controlled straight from the toolbar~
- Open Cargo Play folder from right click menu
- Edition selector in settings
- Properly checks for cargo-expand installation (also indicator in settings)
- Can be run in non-Cargo projects!

![QQ截图20210813124830](https://user-images.githubusercontent.com/13651622/129411302-c91c205a-e3ef-4c09-a021-fff94c7b1733.png)

## Stable

### Usage
- Make or open a cargo project
  - Open a Rust scratch file and go to `Run -> Run in Playground` to execute in Playground
  - `ctrl+comma` can also be used as a shortcut
  - You can also access the `Run in Playground` menu from the `Tools -> Rust` section, or right click menu on the file `Rust -> Run in Playground`
  

`//@` specifies `cargo-play` arguments. It must be the first line in the file.

`//$` specifies your programs arguments. It can be the first or second line in the file.

Case doesn't matter in options or option arguments, except for short flags which are case-sensitive

### `//@` flags
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

### `//@` options
These must be separated by commas,  
Example `//@ edition 2015, mode build`  
Options **MUST** come last after flags

| Short | Long         | Parameters                          |
| ----- | ------------ | --------------------                |
|       | cargo-option | Any cargo options                   |
| e     | edition      | 2015, 2018                          |
| m     | mode         | Any cargo subcommand                |
|       | toolchain    | DEFAULT, STABLE, BETA, NIGHTLY, DEV |
|       | src          | Any .rs files                       |


Examples  
`//@ cargo-option --verbose --color=auto, e 2015, m build, src scratch_2.rs scratch_3.rs`

### `//$` usage
Example `//$ --my prog args`

Of course, you can combine both flags and options together. Just space separate flags, then use a comma between options  
`//@ quiet release, edition 2015, mode build`

### `//#` usage
This is part of cargo-play itself. This is used to add build-dependencies. It accepts any valid Cargo-TOML syntax. This MUST be after the `//@` and `//$` configuration lines.  
Example `//# serde_json = "*"`

### Requirements
- [Official Rust Plugin](https://plugins.jetbrains.com/plugin/8182-rust)
- [Cargo cargo-play subcommand](https://github.com/fanzeyi/cargo-play) (it will prompt to install it automatically)
- IntelliJ 2021.1 or newer

### More information
Check out [cargo-play](https://github.com/fanzeyi/cargo-play) for complete non-plugin usage
