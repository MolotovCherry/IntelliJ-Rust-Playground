package com.cherryleafroad.rust.playground.settings.plugin.ui

import com.cherryleafroad.rust.playground.kargoplay.KargoPlay
import com.cherryleafroad.rust.playground.runconfig.toolchain.Edition
import com.cherryleafroad.rust.playground.runconfig.toolchain.RustChannel
import com.cherryleafroad.rust.playground.services.Settings
import com.cherryleafroad.rust.playground.settings.plugin.PluginConfiguration
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import javax.swing.*

class SettingsForm {
    private val settings = Settings.getInstance().plugin

    private lateinit var _scratchTextArea: JTextArea
    var scratchTextArea: String
        get() = _scratchTextArea.text
        set(value) = run { _scratchTextArea.text = value }

    private lateinit var myPanel: JPanel
    private lateinit var resetBtn: JButton

    lateinit var cargoPlayInstalled: JLabel
    lateinit var cargoExpandInstalled: JLabel
    lateinit var selectedToolchain: JComboBox<RustChannel>
    lateinit var selectedEdition: JComboBox<Edition>
    lateinit var scrollPane: JScrollPane
    lateinit var kargoPlay: JCheckBox

    var scrollPaneShowing = false

    init {
        resetBtn.addActionListener {
            scratchTextArea = PluginConfiguration.DEFAULT_SCRATCH
        }

        for (c in RustChannel.values()) {
            selectedToolchain.addItem(c)
        }

        for (c in Edition.values()) {
            selectedEdition.addItem(c)
        }

        kargoPlay.isSelected = settings.kargoPlay
        kargoPlay.addActionListener {
            val cb = it.source as JCheckBox
            settings.kargoPlay = cb.isSelected
            // make sure this is truly reset to 0
            KargoPlay.lastExitCode = 0
        }

        scratchTextArea = settings.scratchText
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
}
