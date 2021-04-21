/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package com.cherryleafroad.rust.playground.scratch.ui

import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.CheckboxAction
import com.intellij.ui.components.JBCheckBox
import com.intellij.util.ui.JBUI
import javax.swing.JComponent

abstract class SmallBorderCheckboxAction(text: String, description: String? = null) : CheckboxAction(text, description, null) {
    lateinit var checkbox: JBCheckBox

    override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
        checkbox = super.createCustomComponent(presentation, place) as JBCheckBox
        checkbox.border = JBUI.Borders.emptyRight(4)
        setPreselected(checkbox)
        return checkbox
    }

    open fun setPreselected(checkbox: JBCheckBox) {}
}
