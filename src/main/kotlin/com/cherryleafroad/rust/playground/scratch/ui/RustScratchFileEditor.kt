package com.cherryleafroad.rust.playground.scratch.ui

import com.cherryleafroad.rust.playground.actions.CleanAction
import com.cherryleafroad.rust.playground.actions.ToolbarExecuteAction
import com.cherryleafroad.rust.playground.runconfig.toolchain.Edition
import com.cherryleafroad.rust.playground.runconfig.toolchain.RustChannel
import com.cherryleafroad.rust.playground.services.Settings
import com.cherryleafroad.rust.playground.settings.ScratchConfiguration
import com.cherryleafroad.rust.playground.utils.splitIgnoreEmpty
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Condition
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBCheckBox

class RustScratchFileEditor(
    project: Project,
    file: VirtualFile
) : ToolbarTextEditor(project, file, true) {
    private val updateToolbar = { refreshToolbar() }
    
    private val settings = Settings.getInstance().scratches[file.path]

    override fun addTopActions(toolbarGroup: DefaultActionGroup) {
        toolbarGroup.apply {
            add(ToolbarExecuteAction())
            addSeparator()
            add(CleanAction())
            addSeparator()
            add(CheckCheckBoxAction(settings))
            addSeparator()
            add(CleanCheckBoxAction(settings))
            addSeparator()
            add(ExpandCheckBoxAction(settings))
            addSeparator()
            add(InferCheckBoxAction(settings))
            addSeparator()
            add(QuietCheckBoxAction(settings))
            addSeparator()
            add(ReleaseCheckBoxAction(settings))
            addSeparator()
            add(TestCheckBoxAction(settings))
            addSeparator()
            add(VerboseCheckBoxAction(settings))
        }
    }

    override fun addBottomActions(toolbarGroup: DefaultActionGroup) {
        toolbarGroup.apply {
            add(ToolchainComboBoxAction(settings, updateToolbar))
            addSeparator()
            add(EditionComboBoxAction(settings, updateToolbar))
            addSeparator()
            add(SrcsTextField(settings))
            addSeparator()
            add(ArgsTextField(settings))
            addSeparator()
            add(ModeTextField(settings))
            addSeparator()
            add(CargoOptionsTextField(settings))
            add(CargoOptionNoDefaultCheckBoxAction(settings))
        }
    }
}

class ArgsTextField(
    private val settings: ScratchConfiguration
) : LabeledTextEditAction("Args", "Arguments to pass to program") {
    override val textfieldLength: Int =  100

    init {
        textfield.text = settings.args.joinToString(" ")
    }

    override fun textChanged(text: String) {
        settings.args = text.splitIgnoreEmpty(" ")
    }
}

class SrcsTextField(
    private val settings: ScratchConfiguration
) : LabeledTextEditAction("Srcs", "List of (spaced) additional Rust files to build. CWD is scratch dir") {
    override val textfieldLength: Int =  100

    init {
        textfield.text = settings.srcs.joinToString(" ")
    }

    override fun textChanged(text: String) {
        settings.srcs = text.splitIgnoreEmpty(" ")
    }
}

class CargoOptionsTextField(
    private val settings: ScratchConfiguration
) : LabeledTextEditAction("Cargo Options", "Customize flags passed to Cargo") {
    override val textfieldLength: Int =  100

    init {
        textfield.text = settings.cargoOptions.joinToString(" ")
    }

    override fun textChanged(text: String) {
        settings.cargoOptions = text.splitIgnoreEmpty(" ")
    }
}

class ModeTextField(
    private val settings: ScratchConfiguration
) : LabeledTextEditAction("Mode", "Specify subcommand to use when calling Cargo [default: run]") {
    override val textfieldLength: Int =  65

    init {
        textfield.text = settings.mode
    }

    override fun textChanged(text: String) {
        settings.mode = text
    }
}

class ExpandCheckBoxAction(
    private val settings: ScratchConfiguration
) : SmallBorderCheckboxAction("Expand", "Expand macros in your code") {
    override fun setPreselected(checkbox: JBCheckBox) {
        checkbox.isSelected = settings.expand
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        return settings.expand
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.expand = state
    }
}

class VerboseCheckBoxAction(
    private val settings: ScratchConfiguration
) : SmallBorderCheckboxAction("Verbose", "Set Cargo verbose level") {
    override fun setPreselected(checkbox: JBCheckBox) {
        checkbox.isSelected = settings.verbose
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        return settings.verbose
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.verbose = state
    }
}

class TestCheckBoxAction(
    private val settings: ScratchConfiguration
) : SmallBorderCheckboxAction("Test", "Run test code") {
    override fun setPreselected(checkbox: JBCheckBox) {
        checkbox.isSelected = settings.test
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        return settings.test
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.test = state
    }
}

class ReleaseCheckBoxAction(
    private val settings: ScratchConfiguration
) : SmallBorderCheckboxAction("Release", "Build program in release mode") {
    override fun setPreselected(checkbox: JBCheckBox) {
        checkbox.isSelected = settings.release
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        return settings.release
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.release = state
    }
}

class QuietCheckBoxAction(
    private val settings: ScratchConfiguration
) : SmallBorderCheckboxAction("Quiet", "Disable output from Cargo") {
    override fun setPreselected(checkbox: JBCheckBox) {
        checkbox.isSelected = settings.quiet
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        return settings.quiet
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.quiet = state
    }
}

class InferCheckBoxAction(
    private val settings: ScratchConfiguration
) : SmallBorderCheckboxAction("Infer", "[Experimental] Automatically infers crate dependencies") {
    override fun setPreselected(checkbox: JBCheckBox) {
        checkbox.isSelected = settings.infer
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        return settings.infer
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.infer = state
    }
}

class CleanCheckBoxAction(
    private val settings: ScratchConfiguration
) : SmallBorderCheckboxAction("Clean", "Rebuild the Cargo project without the cache from previous run") {
    override fun setPreselected(checkbox: JBCheckBox) {
        checkbox.isSelected = settings.clean
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        return settings.clean
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.clean = state
    }
}

class CheckCheckBoxAction(
    private val settings: ScratchConfiguration
) : SmallBorderCheckboxAction("Check", "Check for errors in your code") {
    override fun setPreselected(checkbox: JBCheckBox) {
        checkbox.isSelected = settings.check
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        return settings.check
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.check = state
    }
}

class CargoOptionNoDefaultCheckBoxAction(
    private val settings: ScratchConfiguration
) : SmallBorderCheckboxAction("No Defaults", "Remove default cargo options") {
    override fun setPreselected(checkbox: JBCheckBox) {
        checkbox.isSelected = settings.cargoOptionsNoDefault
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        return settings.cargoOptionsNoDefault
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        settings.cargoOptionsNoDefault = state
    }
}

class ToolchainComboBoxAction(
    private val settings: ScratchConfiguration,
    val updateToolbar: () -> Unit
) : ComboBoxAction<RustChannel>("Toolchain") {
    private val defaultSelection = settings.toolchain

    override val itemList = RustChannel.values().toList()

    override val preselectedItem: Condition<AnAction> = Condition { action ->
        (action as ComboBoxAction<*>.InnerAction).item == settings.toolchain
    }

    override var currentSelection = defaultSelection

    override fun performAction(e: AnActionEvent, item: RustChannel) {
        settings.toolchain = item
        currentSelection = item
        updateToolbar()
    }
}

class EditionComboBoxAction(
    private val settings: ScratchConfiguration,
    val updateToolbar: () -> Unit
) : ComboBoxAction<Edition>("Edition") {
    private val defaultSelection = settings.edition

    override val itemList = Edition.values().toList()

    override val preselectedItem: Condition<AnAction> = Condition { action ->
        (action as ComboBoxAction<*>.InnerAction).item == settings.edition
    }

    override var currentSelection = defaultSelection

    override fun performAction(e: AnActionEvent, item: Edition) {
        settings.edition = item
        currentSelection = item
        updateToolbar()
    }
}
