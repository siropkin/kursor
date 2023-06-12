package com.github.siropkin.kursor

import com.intellij.ide.FrameStateListener
import com.intellij.ide.IdeEventQueue
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import java.awt.event.KeyEvent

class KursorStartupActivity: StartupActivity {
    private val kursors = mutableMapOf<Editor, Kursor>()

    override fun runActivity(project: Project) {
        // add kursor to all existing editors
        val editors: Array<Editor> = EditorFactory.getInstance().allEditors
        for (editor in editors) {
            val kursor = Kursor(editor)
            kursors[editor] = kursor
        }

        // add editor factory listener to track editor creation and deletion
        EditorFactory.getInstance().addEditorFactoryListener(object : EditorFactoryListener {
            override fun editorCreated(event: EditorFactoryEvent) {
                val editor: Editor? = event.editor
                if (editor != null) {
                    val kursor = Kursor(editor)
                    kursors[editor] = kursor
                }

            }

            override fun editorReleased(event: EditorFactoryEvent) {
                val editor: Editor? = event.editor
                if (editor != null) {
                    kursors.remove(editor)
                }
            }
        }, project)

        // TODO: Add listener for cases when user changes language using OS UI
        // add frame state listener
        val connection = ApplicationManager.getApplication().messageBus.connect()
        connection.subscribe(FrameStateListener.TOPIC, object : FrameStateListener {
            override fun onFrameActivated() {
                kursors.forEach { (_, kursor) -> kursor.repaint() }
            }
        })

        // TODO: Fix Win + Space issue: if Win released after Space, then Space is not registered
        // add key listener
        IdeEventQueue.getInstance().addDispatcher({ event ->
            if (event is KeyEvent) {
                // repaint all kursors
                kursors.forEach { (_, kursor) -> kursor.repaint() }
            }
            false
        }, project)
    }
}
