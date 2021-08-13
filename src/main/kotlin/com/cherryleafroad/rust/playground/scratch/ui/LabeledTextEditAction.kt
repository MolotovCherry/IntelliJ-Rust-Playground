package com.cherryleafroad.rust.playground.scratch.ui

import com.intellij.ide.HelpTooltip
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

abstract class LabeledTextEditAction(
    private val label: String,
    private val description: String? = null
) : AnAction(label, description, null), CustomComponentAction {
    open val textfieldLength: Int = 100
    val textfield = JBTextField()

    override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
        textfield.addKeyListener(object : KeyListener {
            override fun keyTyped(e: KeyEvent) {
                // nothing
            }

            override fun keyPressed(e: KeyEvent) {
                // nothing
            }

            override fun keyReleased(e: KeyEvent) {
                textChanged(textfield.text)
            }
        })

        val preferred = textfield.preferredSize
        val dimens = Dimension()
        dimens.height = preferred.height
        dimens.width = textfieldLength
        textfield.preferredSize = dimens

        val label = JLabel(label)
        label.labelFor = textfield
        val panel = JPanel(BorderLayout(JBUI.scale(3), 0))
        panel.add(BorderLayout.WEST, label)
        panel.add(BorderLayout.CENTER, textfield)
        panel.border = JBUI.Borders.empty(0, 6, 0, 3)

        if (description != null) {
            HelpTooltip().setDescription(description).installOn(panel)
            HelpTooltip().setDescription(description).installOn(textfield)
        }

        return panel
    }

    override fun actionPerformed(e: AnActionEvent) {
        // nothing
    }

    abstract fun textChanged(text: String)
}
