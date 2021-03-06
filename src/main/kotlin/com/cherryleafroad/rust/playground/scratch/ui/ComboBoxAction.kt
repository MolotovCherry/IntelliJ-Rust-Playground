/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package com.cherryleafroad.rust.playground.scratch.ui

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.util.Condition
import com.intellij.openapi.vcs.changes.committed.LabeledComboBoxAction
import com.intellij.util.ui.UIUtil
import javax.swing.JComponent

abstract class ComboBoxAction<E : Enum<E>>(
    label: String
) : LabeledComboBoxAction(label), DumbAware {
    abstract val itemList: List<E>
    abstract val preselectedItem: Condition<AnAction>
    abstract var currentSelection: E

    override fun getPreselectCondition(): Condition<AnAction> = preselectedItem

    override fun createPopupActionGroup(button: JComponent): DefaultActionGroup {
        val actionGroup = DefaultActionGroup()

        for (item in itemList) {
            actionGroup.add(InnerAction(item.name, item))
        }

        return actionGroup
    }

    abstract fun performAction(e: AnActionEvent, item: E)

    override fun update(e: AnActionEvent) {
        super.update(e)
        e.presentation.text = currentSelection.name
    }

    inner class InnerAction(label: String, val item: E) : DumbAwareAction(label) {
        override fun actionPerformed(e: AnActionEvent) {
            performAction(e, item)
        }
    }

    /**
     * By default this action uses big font for label, so we have to decrease it
     * to make it look the same as in [com.intellij.openapi.actionSystem.ex.CheckboxAction].
     */
    override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
        val customComponent = super.createCustomComponent(presentation, place)
        customComponent.components.forEach { it.font = UIUtil.getFont(UIUtil.FontSize.SMALL, it.font) }
        return customComponent
    }
}