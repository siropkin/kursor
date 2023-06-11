package com.github.siropkin.kursor

import com.intellij.ide.FrameStateListener
import com.intellij.ide.IdeEventQueue
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.project.Project
import java.awt.event.KeyEvent

class KursorStartupActivity: StartupActivity {
    private var kursor: Kursor = Kursor()

    override fun runActivity(project: Project) {
        kursor.saveDefaultCaretVisualAttributes()
        kursor.updateCaretVisualAttributes()

        // add frame state listener
        val connection = ApplicationManager.getApplication().messageBus.connect()
        connection.subscribe(FrameStateListener.TOPIC, object : FrameStateListener {
            override fun onFrameActivated() {
                kursor.updateCaretVisualAttributes()
            }
        })

        // add key listener
        // TODO: Fix Win + Space issue: if Win released after Space, then Space is not registered
        IdeEventQueue.getInstance().addDispatcher({ event ->
            if (event is KeyEvent) {
                kursor.updateCaretVisualAttributes()
            }
            false
        }, project)
    }
}
