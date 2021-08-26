package com.cherryleafroad.rust.playground.settings

import com.intellij.ide.scratch.ScratchFileService
import com.intellij.ide.scratch.ScratchRootType
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.xmlb.annotations.Transient

// global class for sharing global data
data class GlobalConfiguration(
    @Transient
    val runtime: Runtime = Runtime()
)

@Transient
data class Runtime(
    // points to the current virtualfile for the current scratch
    private var _scratchFile: VirtualFile? = null,
    // always points to the most recently run scratch (not counting manual run configurations)
    @Transient
    var currentScratch: ScratchConfiguration = ScratchConfiguration(),
    @Transient
    var scratchRoot: String = ScratchFileService.getInstance().getRootPath(ScratchRootType.getInstance()),
    // whether the current run
    @Transient
    var clean: Boolean = false
) {
    var scratchFile: VirtualFile
        get() = _scratchFile!!
        set(value) = run { _scratchFile = value }
}
