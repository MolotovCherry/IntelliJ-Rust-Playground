@file:Suppress("ComponentNotRegistered")

package com.cherryleafroad.rust.playground.scratch.ui

import com.cherryleafroad.rust.playground.actions.CleanAction
import com.cherryleafroad.rust.playground.actions.ToolbarExecuteAction
import com.cherryleafroad.rust.playground.config.Settings
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Condition
import com.intellij.openapi.vfs.VirtualFile
import org.rust.cargo.toolchain.RustChannel

class RustScratchFileEditor(
    val project: Project,
    file: VirtualFile
) : ToolbarTextEditor(project, file, true) {
    private val updateToolbar = { refreshToolbar() }

    override fun addTopActions(toolbarGroup: DefaultActionGroup) {
        toolbarGroup.apply {
            add(ToolbarExecuteAction())
            addSeparator()
            add(CleanAction())
            addSeparator()
            add(CheckCheckBoxAction(file))
            addSeparator()
            add(CleanCheckBoxAction(file))
            add(ExpandCheckBoxAction(file))
            addSeparator()
            add(InferCheckBoxAction(file))
            addSeparator()
            add(QuietCheckBoxAction(file))
            addSeparator()
            add(ReleaseCheckBoxAction(file))
            addSeparator()
            add(TestCheckBoxAction(file))
            addSeparator()
            add(VerboseCheckBoxAction(file))
        }
    }

    override fun addBottomActions(toolbarGroup: DefaultActionGroup) {
        toolbarGroup.apply {
            add(ToolchainComboBoxAction(file, updateToolbar))
            addSeparator()
            add(EditionComboBoxAction(file, updateToolbar))
        }
    }
}

class ExpandCheckBoxAction(
    val file: VirtualFile
) : SmallBorderCheckboxAction("Expand", "Expand macros in your code") {
    private val properties: PropertiesComponent = PropertiesComponent.getInstance()

    init {
        // reset to default
        properties.setValue("expand/${file.path}", false)
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        return properties.getBoolean("expand/${file.path}")
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        properties.setValue("expand/${file.path}", state)
    }
}

class VerboseCheckBoxAction(
    val file: VirtualFile
) : SmallBorderCheckboxAction("Verbose", "Set Cargo verbose level") {
    private val properties: PropertiesComponent = PropertiesComponent.getInstance()

    init {
        // reset to default
        properties.setValue("verbose/${file.path}", false)
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        return properties.getBoolean("verbose/${file.path}")
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        properties.setValue("verbose/${file.path}", state)
    }
}

class TestCheckBoxAction(
    val file: VirtualFile
) : SmallBorderCheckboxAction("Test", "Run test code") {
    private val properties: PropertiesComponent = PropertiesComponent.getInstance()

    init {
        // reset to default
        properties.setValue("test/${file.path}", false)
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        return properties.getBoolean("test/${file.path}")
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        properties.setValue("test/${file.path}", state)
    }
}

class ReleaseCheckBoxAction(
    val file: VirtualFile
) : SmallBorderCheckboxAction("Release", "Build program in release mode") {
    private val properties: PropertiesComponent = PropertiesComponent.getInstance()

    init {
        // reset to default
        properties.setValue("release/${file.path}", false)
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        return properties.getBoolean("release/${file.path}")
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        properties.setValue("release/${file.path}", state)
    }
}

class QuietCheckBoxAction(
    val file: VirtualFile
) : SmallBorderCheckboxAction("Quiet", "Disable output from Cargo") {
    private val properties: PropertiesComponent = PropertiesComponent.getInstance()

    init {
        // reset to default
        properties.setValue("quiet/${file.path}", false)
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        return properties.getBoolean("quiet/${file.path}")
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        properties.setValue("quiet/${file.path}", state)
    }
}

class InferCheckBoxAction(
    val file: VirtualFile
) : SmallBorderCheckboxAction("Infer", "[Experimental] Automatically infers crate dependencies") {
    private val properties: PropertiesComponent = PropertiesComponent.getInstance()

    init {
        // reset to default
        properties.setValue("infer/${file.path}", false)
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        return properties.getBoolean("infer/${file.path}")
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        properties.setValue("infer/${file.path}", state)
    }
}

class CleanCheckBoxAction(
    val file: VirtualFile
) : SmallBorderCheckboxAction("Clean", "Rebuild the Cargo project without the cache from previous run") {
    private val properties: PropertiesComponent = PropertiesComponent.getInstance()

    init {
        // reset to default
        properties.setValue("clean/${file.path}", false)
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        return properties.getBoolean("clean/${file.path}")
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        properties.setValue("clean/${file.path}", state)
    }
}

class CheckCheckBoxAction(
    val file: VirtualFile
) : SmallBorderCheckboxAction("Check", "Check for errors in your code") {
    private val properties: PropertiesComponent = PropertiesComponent.getInstance()

    init {
        // reset to default
        properties.setValue("check/${file.path}", false)
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        return properties.getBoolean("check/${file.path}")
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        properties.setValue("check/${file.path}", state)
    }
}

class ToolchainComboBoxAction(
    val file: VirtualFile,
    val updateToolbar: () -> Unit
) : ComboBoxAction("Toolchain") {
    private val properties: PropertiesComponent = PropertiesComponent.getInstance()
    private val defaultSelection = Settings.getSelectedToolchain()

    init {
        // reset to default
        properties.setValue("toolchain/${file.path}", defaultSelection.index, RustChannel.DEFAULT.index)
    }

    override val itemList = RustChannel.values().map { it.name }

    override val preselectedItem: Condition<AnAction> = Condition { action ->
        (action as InnerAction).index == defaultSelection.index
    }

    override var currentSelection: String = defaultSelection.name

    override fun performAction(e: AnActionEvent, index: Int) {
        val selected = RustChannel.values()[index]
        properties.setValue("toolchain/${file.path}", selected.index, RustChannel.DEFAULT.index)
        currentSelection = RustChannel.values()[index].name
        updateToolbar()
    }
}

class EditionComboBoxAction(
    val file: VirtualFile,
    val updateToolbar: () -> Unit
) : ComboBoxAction("Edition") {
    private val properties: PropertiesComponent = PropertiesComponent.getInstance()

    init {
        // reset to default
        properties.setValue("edition/${file.path}", "DEFAULT")
    }

    override val itemList = listOf("DEFAULT", "2015", "2018")

    override val preselectedItem: Condition<AnAction> = Condition { action ->
        (action as InnerAction).index == 0
    }

    override var currentSelection: String = "DEFAULT"

    override fun performAction(e: AnActionEvent, index: Int) {
        properties.setValue("edition/${file.path}", itemList[index])
        currentSelection = itemList[index]
        updateToolbar()
    }
}
