# IntelliJ Rust Playground

[![Marketplace](https://img.shields.io/jetbrains/plugin/v/16586?style=plastic&label=Marketplace&color=orange)](https://plugins.jetbrains.com/plugin/16586-rust-playground) ![Downloads](https://img.shields.io/jetbrains/plugin/d/16586?label=Downloads&style=plastic&color=blue)

[![Build](https://github.com/cherryleafroad/IntelliJ-Rust-Playground/actions/workflows/build.yml/badge.svg?event=push)](https://github.com/cherryleafroad/IntelliJ-Rust-Playground/actions/workflows/build.yml) [![IntelliJ Platform Plugin Compatibility](https://github.com/cherryleafroad/IntelliJ-Rust-Playground/actions/workflows/compatibility.yml/badge.svg?event=push)](https://github.com/cherryleafroad/IntelliJ-Rust-Playground/actions/workflows/compatibility.yml)

## Archival Notice:
Since I have to focus on more realistic things in life to stay afload, I do not have time to keep this updated with the ever changing IDE API (it's a LOT of work). So unfortunately I've had to discontinue development for this. The last IDE version this will work on is likely 2021 edition.

You are welcome to take all of this source code and use it/modify it freely. Just please credit me, and also, I would love to know about/see your project that utilizes it!

---

<!-- Plugin description -->
An IntelliJ plugin offering a powerful local Rust playground, straight from your IDE! Inspired by the [official Rust playground](https://play.rust-lang.org/), but more convenient to use!

### Features
- Complete integration with official IntelliJ Rust plugin
- Run playground-like scripts directly from your IDE without having to setup any projects
- Supports naming build-dependencies, different toolchains, and all `cargo-play` functions from the toolbar above your scratch file
- Ability to change the Rust default scratch file template to your own
- Open Cargo Play folder from right click menu
- Can be run in non-Cargo projects!
<!-- Plugin description end -->

![QQ截图20210813124830](https://user-images.githubusercontent.com/13651622/129411302-c91c205a-e3ef-4c09-a021-fff94c7b1733.png)

### Usage
- Make or open a cargo project
  - Open a Rust scratch file and go to `Run -> Run in Playground` to execute in Playground, or use the toolbar play button
  - `ctrl+alt+comma` can also be used as a shortcut

#### `//#` usage
This is part of cargo-play itself. This is used to add build-dependencies. It accepts any valid Cargo-TOML syntax. Please see cargo-play docs for more info.
Example `//# serde_json = "*"`

### Requirements
- [Official Rust Plugin](https://plugins.jetbrains.com/plugin/8182-rust)
- [Cargo cargo-play subcommand](https://github.com/fanzeyi/cargo-play) (it will prompt to install it automatically)
- [Cargo cargo-expand subcommand](https://github.com/dtolnay/cargo-expand) (for --expand function)
- IntelliJ 2021.3

### More information
Check out [cargo-play](https://github.com/fanzeyi/cargo-play) for complete non-plugin usage

### Contributions
Please contribute to the latest stable branch `major.minor.x` instead of master (e.g. if the latest stable release is `2.1.4`, check for a branch `2.1.x` and contribute to that one). Master is currently very experimental and will likely fail compiling, and may even be out of date in respect to the stable branch.
