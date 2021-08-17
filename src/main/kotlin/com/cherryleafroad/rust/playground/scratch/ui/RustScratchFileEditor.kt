@file:Suppress("ComponentNotRegistered")

package com.cherryleafroad.rust.playground.scratch.ui

import com.cherryleafroad.rust.playground.actions.CleanAction
import com.cherryleafroad.rust.playground.actions.ToolbarExecuteAction
import com.cherryleafroad.rust.playground.config.Settings
import com.cherryleafroad.rust.playground.parser.Edition
import com.cherryleafroad.rust.playground.parser.RustChannel
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
    private val ARGS: Setting
) : LabeledTextEditAction("Args", "Arguments to pass to program") {
    override val textfieldLength: Int =  100

    init {
        val saved = ARGS.getValue()
        textfield.text = saved
    }

    override fun textChanged(text: String) {
        ARGS.setValue(text)
    }
}

class SrcTextField(
    private val SRC: Setting
) : LabeledTextEditAction("Src", "List of (spaced) additional Rust files to build. CWD is scratch dir") {
    override val textfieldLength: Int =  100

    init {
        val saved = SRC.getValue()
        textfield.text = saved
    }

    override fun textChanged(text: String) {
        SRC.setValue(text)
    }
}

class CargoOptionTextField(
    private val CARGO_OPTION: Setting
) : LabeledTextEditAction("Cargo Options", "Customize flags passed to Cargo") {
    override val textfieldLength: Int =  100

    init {
        val saved = CARGO_OPTION.getValue()
        textfield.text = saved
    }

    override fun textChanged(text: String) {
        CARGO_OPTION.setValue(text)
    }
}

class ModeTextField(
    private val MODE: Setting
) : LabeledTextEditAction("Mode", "Specify subcommand to use when calling Cargo [default: run]") {
    override val textfieldLength: Int =  65

    init {
        val saved = MODE.getValue()
        textfield.text = saved
    }

    override fun textChanged(text: String) {
        MODE.setValue(text)
    }
}

class ExpandCheckBoxAction(
    private val EXPAND: Setting
) : SmallBorderCheckboxAction("Expand", "Expand macros in your code") {
    override fun setPreselected(checkbox: JBCheckBox) {
        val saved = EXPAND.getBoolean()
        checkbox.isSelected = saved
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        return EXPAND.getBoolean()
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        EXPAND.setValue(state)
    }
}

class VerboseCheckBoxAction(
    private val VERBOSE: Setting
) : SmallBorderCheckboxAction("Verbose", "Set Cargo verbose level") {
    override fun setPreselected(checkbox: JBCheckBox) {
        val saved = VERBOSE.getBoolean()
        checkbox.isSelected = saved
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        return VERBOSE.getBoolean()
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        VERBOSE.setValue(state)
    }
}

class TestCheckBoxAction(
    private val TEST: Setting
) : SmallBorderCheckboxAction("Test", "Run test code") {
    override fun setPreselected(checkbox: JBCheckBox) {
        val saved = TEST.getBoolean()
        checkbox.isSelected = saved
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        return TEST.getBoolean()
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        TEST.setValue(state)
    }
}

class ReleaseCheckBoxAction(
    private val RELEASE: Setting
) : SmallBorderCheckboxAction("Release", "Build program in release mode") {
    override fun setPreselected(checkbox: JBCheckBox) {
        val saved = RELEASE.getBoolean()
        checkbox.isSelected = saved
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        return RELEASE.getBoolean()
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        RELEASE.setValue(state)
    }
}

class QuietCheckBoxAction(
    private val QUIET: Setting
) : SmallBorderCheckboxAction("Quiet", "Disable output from Cargo") {
    override fun setPreselected(checkbox: JBCheckBox) {
        val saved = QUIET.getBoolean()
        checkbox.isSelected = saved
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        return QUIET.getBoolean()
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        QUIET.setValue(state)
    }
}

class InferCheckBoxAction(
    private val INFER: Setting
) : SmallBorderCheckboxAction("Infer", "[Experimental] Automatically infers crate dependencies") {
    override fun setPreselected(checkbox: JBCheckBox) {
        val saved = INFER.getBoolean()
        checkbox.isSelected = saved
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        return INFER.getBoolean()
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        INFER.setValue(state)
    }
}

class CleanCheckBoxAction(
    private val CLEAN: Setting
) : SmallBorderCheckboxAction("Clean", "Rebuild the Cargo project without the cache from previous run") {
    override fun setPreselected(checkbox: JBCheckBox) {
        val saved = CLEAN.getBoolean()
        checkbox.isSelected = saved
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        return CLEAN.getBoolean()
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        CLEAN.setValue(state)
    }
}

class CheckCheckBoxAction(
    private val CHECK: Setting
) : SmallBorderCheckboxAction("Check", "Check for errors in your code") {
    override fun setPreselected(checkbox: JBCheckBox) {
        val saved = CHECK.getBoolean()
        checkbox.isSelected = saved
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        return CHECK.getBoolean()
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        CHECK.setValue(state)
    }
}

class CargoOptionNoDefaultCheckBoxAction(
    private val CARGO_OPTION_NO_DEFAULT: Setting
) : SmallBorderCheckboxAction("No Defaults", "Remove default cargo options") {
    override fun setPreselected(checkbox: JBCheckBox) {
        val saved = CARGO_OPTION_NO_DEFAULT.getBoolean()
        checkbox.isSelected = saved
    }

    override fun isSelected(e: AnActionEvent): Boolean {
        return CARGO_OPTION_NO_DEFAULT.getBoolean()
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        CARGO_OPTION_NO_DEFAULT.setValue(state)
    }
}

class ToolchainComboBoxAction(
    private val TOOLCHAIN: Setting,
    val updateToolbar: () -> Unit
) : ComboBoxAction("Toolchain") {
    private val defaultSelection = Settings.getSelectedToolchain()

    override val itemList = RustChannel.values().map { it.name }

    init {
        TOOLCHAIN.setValue(defaultSelection.index)
    }

    override val preselectedItem: Condition<AnAction> = Condition { action ->
        (action as InnerAction).index == TOOLCHAIN.getInt(defaultSelection.index)
    }

    override var currentSelection: String = defaultSelection.name

    override fun performAction(e: AnActionEvent, index: Int) {
        TOOLCHAIN.setValue(index)
        currentSelection = RustChannel.values()[index].name
        updateToolbar()
    }
}

class EditionComboBoxAction(
    private val EDITION: Setting,
    val updateToolbar: () -> Unit
) : ComboBoxAction("Edition") {
    private val defaultSelection = Settings.getSelectedEdition()

    override val itemList = Edition.values().map { it.myName }

    init {
        EDITION.setValue(defaultSelection.index)
    }

    override val preselectedItem: Condition<AnAction> = Condition { action ->
        (action as InnerAction).index == EDITION.getInt(defaultSelection.index)
    }

    override var currentSelection: String = defaultSelection.myName

    override fun performAction(e: AnActionEvent, index: Int) {
        EDITION.setValue(index)
        currentSelection = Edition.values()[index].myName
        updateToolbar()
    }
}
