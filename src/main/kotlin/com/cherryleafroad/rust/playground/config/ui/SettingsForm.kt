package com.cherryleafroad.rust.playground.config.ui

import com.cherryleafroad.rust.playground.config.Settings
import org.rust.cargo.toolchain.RustChannel
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import javax.swing.*

class SettingsForm {
    lateinit var scratchDefault: JTextArea
    lateinit var myPanel: JPanel
    lateinit var resetBtn: JButton
    lateinit var cargoPlayInstalled: JLabel
    lateinit var selectedToolchain: JComboBox<String>
    lateinit var scrollPane: JScrollPane

    var scrollPaneShowing = false

    init {
        resetBtn.addActionListener {
            scratchDefault.text = Settings.DEFAULT_TEXT
        }

        for (c in RustChannel.values()) {
            selectedToolchain.addItem(c.name)
        }

        scratchDefault.text = Settings.getScratchDefault()
        scrollPane.addComponentListener(object : ComponentListener {
            override fun componentResized(e: ComponentEvent?) {
                // wait until it's showing, otherwise scroll fails
                if (!scrollPaneShowing) {
                    scrollPane.verticalScrollBar.value = 0
                    scrollPaneShowing = true
                }
            }

            override fun componentMoved(e: ComponentEvent?) {
                //
            }

            override fun componentShown(e: ComponentEvent?) {
                //
            }

            override fun componentHidden(e: ComponentEvent?) {
                //
            }

        })
    }

    fun getPanel(): JComponent {
        return myPanel
    }

    fun getScratch(): String {
        return scratchDefault.text
    }
}
