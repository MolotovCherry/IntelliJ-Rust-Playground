package com.cherryleafroad.rust.playground.runconfig.runtime

data class RuntimeConfiguration(
    var options: List<String> = listOf(),
    var sources: List<String> = listOf(),
    var args: List<String> = listOf()
)
