package com.cherryleafroad.rust.playground.scratch.ui

import com.intellij.codeHighlighting.BackgroundEditorHighlighter
import com.intellij.icons.AllIcons
import com.intellij.ide.structureView.StructureViewBuilder
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.pom.Navigatable
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JPanel


/*
 * Just a normal text editor, but with an editor toolbar at the top
 * for different types of actions
 */
abstract class ToolbarTextEditor(
    val project: Project,
    private val virtualFile: VirtualFile,
    private val doubleToolbar: Boolean = false
) : UserDataHolderBase(), TextEditor, FileEditor {

    private val myEditor = run {
        val textEditorProvider = TextEditorProvider.getInstance()
        textEditorProvider.createEditor(project, virtualFile) as TextEditor
    }

    private val myName = "ToolbarTextEditor"

    private val myListenersGenerator: MyListenersMultimap = MyListenersMultimap()

    private lateinit var myComponent: JComponent
    private lateinit var myToolbarWrapper: EditorToolbar
    private lateinit var myTopActionToolbar: ActionToolbar
    private var myBottomActionToolbar: ActionToolbar? = null

    override fun getBackgroundHighlighter(): BackgroundEditorHighlighter? {
        return this.myEditor.backgroundHighlighter
    }

    override fun getCurrentLocation(): FileEditorLocation? {
        return this.myEditor.currentLocation
    }

    override fun getStructureViewBuilder(): StructureViewBuilder? {
        return this.myEditor.structureViewBuilder
    }

    override fun canNavigateTo(navigatable: Navigatable): Boolean {
        return this.myEditor.canNavigateTo(navigatable)
    }

    override fun navigateTo(navigatable: Navigatable) {
        this.myEditor.navigateTo(navigatable)
    }

    override fun getEditor(): Editor {
        return this.myEditor.editor
    }

    override fun dispose() {
        Disposer.dispose(this.myEditor)
    }

    override fun selectNotify() {
        this.myEditor.selectNotify()
    }

    override fun deselectNotify() {
        this.myEditor.deselectNotify()
    }

    override fun getComponent(): JComponent {
        if (!this::myComponent.isInitialized) {
            myToolbarWrapper = createToolbarWrapper()
            Disposer.register(this, myToolbarWrapper)

            myComponent = JBUI.Panels.simplePanel().addToTop(myToolbarWrapper).addToCenter(this.myEditor.component)
        }
        return myComponent
    }

    private fun createToolbarWrapper(): EditorToolbar {
        val topToolbarGroup = DefaultActionGroup()
        addTopActions(topToolbarGroup)
        myTopActionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.EDITOR_TOOLBAR, topToolbarGroup, true)
        myTopActionToolbar.setTargetComponent(myTopActionToolbar.component)
        myTopActionToolbar.setReservePlaceAutoPopupIcon(false)

        val bottomToolbarGroup = DefaultActionGroup()
        if (doubleToolbar) {
            addBottomActions(bottomToolbarGroup)
            myBottomActionToolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.EDITOR_TOOLBAR, bottomToolbarGroup, true)
            myBottomActionToolbar!!.setTargetComponent(myBottomActionToolbar!!.component)
            myBottomActionToolbar!!.setReservePlaceAutoPopupIcon(false)
        }

        return EditorToolbar(project, myTopActionToolbar, myBottomActionToolbar)
    }

    open fun addTopActions(toolbarGroup: DefaultActionGroup) {
        TODO("Implement actions")
    }
    open fun addBottomActions(toolbarGroup: DefaultActionGroup) {
        TODO("Implement actions")
    }

    fun refreshToolbar() {
        myTopActionToolbar.updateActionsImmediately()
        if (doubleToolbar) {
            myBottomActionToolbar!!.updateActionsImmediately()
        }
    }

    override fun setState(state: FileEditorState) {
        this.myEditor.setState(state)
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return this.myEditor.preferredFocusedComponent
    }

    override fun getName(): String {
        return myName
    }

    override fun getState(level: FileEditorStateLevel): FileEditorState {
        return this.myEditor.getState(level)
    }

    override fun addPropertyChangeListener(listener: PropertyChangeListener) {
        this.myEditor.addPropertyChangeListener(listener)
        val delegate = myListenersGenerator.addListenerAndGetDelegate(listener)
        this.myEditor.addPropertyChangeListener(delegate)
    }

    override fun removePropertyChangeListener(listener: PropertyChangeListener) {
        this.myEditor.removePropertyChangeListener(listener)
        val delegate = myListenersGenerator.removeListenerAndGetDelegate(listener)
        if (delegate != null) {
            this.myEditor.removePropertyChangeListener(delegate)
        }
    }

    override fun isModified(): Boolean {
        return this.myEditor.isModified
    }

    override fun isValid(): Boolean {
        return this.myEditor.isValid
    }

    private inner class DoublingEventListenerDelegate(private val myDelegate: PropertyChangeListener) :
        PropertyChangeListener {
        override fun propertyChange(evt: PropertyChangeEvent) {
            myDelegate.propertyChange(
                PropertyChangeEvent(this@ToolbarTextEditor, evt.propertyName, evt.oldValue, evt.newValue)
            )
        }
    }

    private inner class MyListenersMultimap {
        private val myMap: MutableMap<PropertyChangeListener, Pair<Int, DoublingEventListenerDelegate>> = HashMap()
        fun addListenerAndGetDelegate(listener: PropertyChangeListener): DoublingEventListenerDelegate {
            if (!myMap.containsKey(listener)) {
                myMap[listener] =
                    Pair.create(
                        1,
                        DoublingEventListenerDelegate(listener)
                    )
            } else {
                val oldPair = myMap[listener]!!
                myMap[listener] =
                    Pair.create(
                        oldPair.getFirst() + 1,
                        oldPair.getSecond()
                    )
            }
            return myMap[listener]!!.getSecond()
        }

        fun removeListenerAndGetDelegate(listener: PropertyChangeListener): DoublingEventListenerDelegate? {
            val oldPair = myMap[listener] ?: return null
            if (oldPair.getFirst() == 1) {
                myMap.remove(listener)
            } else {
                myMap[listener] =
                    Pair.create(
                        oldPair.getFirst() - 1,
                        oldPair.getSecond()
                    )
            }
            return oldPair.getSecond()
        }
    }

    override fun getFile(): VirtualFile {
        return virtualFile
    }
}

private class EditorToolbar(
    val project: Project,
    myTopToolbar: ActionToolbar,
    val myBottomToolbar: ActionToolbar?
) : JPanel(GridBagLayout()), Disposable {
    init {
        val gbc = GridBagConstraints(
            0, 0, 1, 1, 1.0, 1.0,
            GridBagConstraints.WEST, GridBagConstraints.BOTH, JBUI.emptyInsets(), 0, 0
        )

        add(myTopToolbar.component, gbc)

        if (myBottomToolbar != null) {
            val gbc2 = GridBagConstraints(
                1, 0, 1, 1, 0.0, 1.0,
                GridBagConstraints.PAGE_END, GridBagConstraints.BOTH, JBUI.emptyInsets(), 0, 0
            )

            val hideGroup = DefaultActionGroup()
            hideGroup.add(ToggleBottomToolbarAction())
            val hideButton = ActionManager.getInstance().createActionToolbar(ActionPlaces.EDITOR_TOOLBAR, hideGroup, true)
            hideButton.setTargetComponent(hideButton.component)
            hideButton.setReservePlaceAutoPopupIcon(false)

            add(hideButton.component, gbc2)

            val bottomConstants = GridBagConstraints(
                0, 1, 2, 1, 1.0, 1.0,
                GridBagConstraints.WEST, GridBagConstraints.BOTH, JBUI.emptyInsets(), 0, 0
            )

            add(myBottomToolbar.component, bottomConstants)
        }

        border = BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtil.CONTRAST_BORDER_COLOR)

        myTopToolbar.updateActionsImmediately()
    }

    override fun dispose() {}

    inner class ToggleBottomToolbarAction: DumbAwareAction("Show/Hide Bottom Toolbar", null, AllIcons.General.HideToolWindow) {
        val properties: PropertiesComponent = PropertiesComponent.getInstance(project)
        val bottomBarKey = "ToolbarTextEditor/bottomBarShown"
        var shown = properties.getBoolean(bottomBarKey, true)

        init {
            if (!shown) {
                myBottomToolbar!!.component.isVisible = false
            }
        }

        override fun actionPerformed(e: AnActionEvent) {
            shown = !shown

            if (!shown) {
                myBottomToolbar!!.component.isVisible = false
                properties.setValue(bottomBarKey, false, true)
            } else {
                myBottomToolbar!!.component.isVisible = true
                properties.setValue(bottomBarKey, true, true)
            }
        }
    }
}
