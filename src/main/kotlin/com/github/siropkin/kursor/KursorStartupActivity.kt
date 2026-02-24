package com.github.siropkin.kursor

import com.intellij.ide.IdeEventQueue
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import java.awt.event.KeyEvent


class KursorStartupActivity: ProjectActivity {
    private val kursors = mutableMapOf<Editor, Kursor>()

    override suspend fun execute(project: Project) {
        // add kursor to all existing editors
        val editors: Array<Editor> = EditorFactory.getInstance().allEditors
        for (editor in editors) {
            if (kursors[editor] == null) {
                kursors[editor] = Kursor(editor)
            }
        }

        // add editor factory listener to track editor creation and deletion
        EditorFactory.getInstance().addEditorFactoryListener(object : EditorFactoryListener {
            override fun editorCreated(event: EditorFactoryEvent) {
                val editor: Editor = event.editor
                if (kursors[editor] == null) {
                    kursors[editor] = Kursor(editor)
                }
            }

            override fun editorReleased(event: EditorFactoryEvent) {
                val editor: Editor = event.editor
                if (kursors[editor] != null) {
                    kursors.remove(editor)
                }
            }
        }, project)

        // add key listener
        IdeEventQueue.getInstance().addPostprocessor({ event ->
            if (event is KeyEvent) {
                kursors.forEach { (_, kursor) -> kursor.repaint() }
            }
            false
        }, project)
    }
}
