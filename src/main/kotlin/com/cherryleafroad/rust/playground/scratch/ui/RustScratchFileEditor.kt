package com.cherryleafroad.rust.playground.scratch.ui

import com.cherryleafroad.rust.playground.actions.CleanAction
import com.cherryleafroad.rust.playground.actions.ToolbarExecuteAction
import com.cherryleafroad.rust.playground.config.*
import com.cherryleafroad.rust.playground.runconfig.toolchain.Edition
import com.cherryleafroad.rust.playground.runconfig.toolchain.RustChannel
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
    
    private val settings = ScratchSettings(file)

    override fun addTopActions(toolbarGroup: DefaultActionGroup) {
        toolbarGroup.apply {
            add(ToolbarExecuteAction())
            addSeparator()
            add(CleanAction())
            addSeparator()
            add(CheckCheckBoxAction(settings.CHECK))
            addSeparator()
            add(CleanCheckBoxAction(settings.CLEAN))
            addSeparator()
            add(ExpandCheckBoxAction(settings.EXPAND))
            addSeparator()
            add(InferCheckBoxAction(settings.INFER))
            addSeparator()
            add(QuietCheckBoxAction(settings.QUIET))
            addSeparator()
            add(ReleaseCheckBoxAction(settings.RELEASE))
            addSeparator()
            add(TestCheckBoxAction(settings.TEST))
            addSeparator()
            add(VerboseCheckBoxAction(settings.VERBOSE))
        }
    }

    override fun addBottomActions(toolbarGroup: DefaultActionGroup) {
        toolbarGroup.apply {
            add(ToolchainComboBoxAction(settings.TOOLCHAIN, updateToolbar))
            addSeparator()
            add(EditionComboBoxAction(settings.EDITION, updateToolbar))
            addSeparator()
            add(SrcTextField(settings.SRC))
            addSeparator()
            add(ArgsTextField(settings.ARGS))
            addSeparator()
            add(ModeTextField(settings.MODE))
            addSeparator()
            add(CargoOptionTextField(settings.CARGO_OPTIONS))
            add(CargoOptionNoDefaultCheckBoxAction(settings.CARGO_OPTIONS_NO_DEFAULTS))
        }
    }
}

class ArgsTextField(
    private val ARGS: StringSetting
) : LabeledTextEditAction("Args", "Arguments to pass to program") {
    override val textfieldLength: Int =  100

    init {
        textfield.text = ARGS.get()
    }

    override fun textChanged(text: String) {
        ARGS.set(text)
    }
}

class SrcTextField(
    private val SRC: StringSetting
) : LabeledTextEditAction("Src", "List of (spaced) additional Rust files to build. CWD is scratch dir") {
    override val textfieldLength: Int =  100

    init {
        textfield.text = SRC.get()
    }

    override fun textChanged(text: String) {
        SRC.set(text)
    }
}

class CargoOptionTextField(
    private val CARGO_OPTION: StringSetting
) : LabeledTextEditAction("Cargo Options", "Customize flags passed to Cargo") {
    override val textfieldLength: Int =  100

    init {
        textfield.text = CARGO_OPTION.get()
    }

    override fun textChanged(text: String) {
        CARGO_OPTION.set(text)
    }
}

class ModeTextField(
    private val MODE: StringSetting
) : LabeledTextEditAction("Mode", "Specify subcommand to use when calling Cargo [default: run]") {
    override val textfieldLength: Int =  65

    init {
        textfield.text = MODE.get()
    }

    override fun textChanged(text: String) {
        MODE.set(text)
    }
}

class ExpandCheckBoxAction(
    private val EXPAND: BooleanSetting
) : SmallBorderCheckboxAction("Expand", "Expand macros in your code") {
    override fun setPreselected(checkbox: JBCheckBox) {
        checkbox.isSelected = EXPAND.get()
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        return EXPAND.get()
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        EXPAND.set(state)
    }
}

class VerboseCheckBoxAction(
    private val VERBOSE: BooleanSetting
) : SmallBorderCheckboxAction("Verbose", "Set Cargo verbose level") {
    override fun setPreselected(checkbox: JBCheckBox) {
        checkbox.isSelected = VERBOSE.get()
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        return VERBOSE.get()
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        VERBOSE.set(state)
    }
}

class TestCheckBoxAction(
    private val TEST: BooleanSetting
) : SmallBorderCheckboxAction("Test", "Run test code") {
    override fun setPreselected(checkbox: JBCheckBox) {
        checkbox.isSelected = TEST.get()
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        return TEST.get()
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        TEST.set(state)
    }
}

class ReleaseCheckBoxAction(
    private val RELEASE: BooleanSetting
) : SmallBorderCheckboxAction("Release", "Build program in release mode") {
    override fun setPreselected(checkbox: JBCheckBox) {
        checkbox.isSelected = RELEASE.get()
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        return RELEASE.get()
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        RELEASE.set(state)
    }
}

class QuietCheckBoxAction(
    private val QUIET: BooleanSetting
) : SmallBorderCheckboxAction("Quiet", "Disable output from Cargo") {
    override fun setPreselected(checkbox: JBCheckBox) {
        checkbox.isSelected = QUIET.get()
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        return QUIET.get()
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        QUIET.set(state)
    }
}

class InferCheckBoxAction(
    private val INFER: BooleanSetting
) : SmallBorderCheckboxAction("Infer", "[Experimental] Automatically infers crate dependencies") {
    override fun setPreselected(checkbox: JBCheckBox) {
        checkbox.isSelected = INFER.get()
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        return INFER.get()
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        INFER.set(state)
    }
}

class CleanCheckBoxAction(
    private val CLEAN: BooleanSetting
) : SmallBorderCheckboxAction("Clean", "Rebuild the Cargo project without the cache from previous run") {
    override fun setPreselected(checkbox: JBCheckBox) {
        checkbox.isSelected = CLEAN.get()
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        return CLEAN.get()
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        CLEAN.set(state)
    }
}

class CheckCheckBoxAction(
    private val CHECK: BooleanSetting
) : SmallBorderCheckboxAction("Check", "Check for errors in your code") {
    override fun setPreselected(checkbox: JBCheckBox) {
        checkbox.isSelected = CHECK.get()
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        return CHECK.get()
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        CHECK.set(state)
    }
}

class CargoOptionNoDefaultCheckBoxAction(
    private val CARGO_OPTION_NO_DEFAULT: BooleanSetting
) : SmallBorderCheckboxAction("No Defaults", "Remove default cargo options") {
    override fun setPreselected(checkbox: JBCheckBox) {
        checkbox.isSelected = CARGO_OPTION_NO_DEFAULT.get()
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        return CARGO_OPTION_NO_DEFAULT.get()
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        CARGO_OPTION_NO_DEFAULT.set(state)
    }
}

class ToolchainComboBoxAction(
    private val TOOLCHAIN: ToolchainSetting,
    val updateToolbar: () -> Unit
) : ComboBoxAction("Toolchain") {
    private val defaultSelection = Settings.TOOLCHAIN.get()

    override val itemList = RustChannel.values().map { it.name }

    init {
        TOOLCHAIN.set(defaultSelection.index)
    }

    override val preselectedItem: Condition<AnAction> = Condition { action ->
        (action as InnerAction).index == TOOLCHAIN.get(defaultSelection.index)
    }

    override var currentSelection: String = defaultSelection.name

    override fun performAction(e: AnActionEvent, index: Int) {
        TOOLCHAIN.set(index)
        currentSelection = RustChannel.values()[index].name
        updateToolbar()
    }
}

class EditionComboBoxAction(
    private val EDITION: EditionSetting,
    val updateToolbar: () -> Unit
) : ComboBoxAction("Edition") {
    private val defaultSelection = Settings.EDITION.get()

    override val itemList = Edition.values().map { it.myName }

    init {
        EDITION.set(defaultSelection.index)
    }

    override val preselectedItem: Condition<AnAction> = Condition { action ->
        (action as InnerAction).index == EDITION.get(defaultSelection.index)
    }

    override var currentSelection: String = defaultSelection.myName

    override fun performAction(e: AnActionEvent, index: Int) {
        EDITION.set(index)
        currentSelection = Edition.values()[index].myName
        updateToolbar()
    }
}
