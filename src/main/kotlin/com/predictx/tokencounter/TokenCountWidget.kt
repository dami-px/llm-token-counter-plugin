package com.predictx.tokencounter

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.SelectionEvent
import com.intellij.openapi.editor.event.SelectionListener
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.CustomStatusBarWidget
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.ui.components.JBLabel
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.ui.JBUI
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import javax.swing.JComponent

class TokenCountWidget(private val project: Project) : CustomStatusBarWidget {

    companion object {
        const val ID = "com.predictx.tokencounter.widget"
        private const val DEBOUNCE_MS = 150L
        private const val LARGE_TEXT_THRESHOLD = 50_000
    }

    private val label = JBLabel("tokens: –").apply {
        border = JBUI.Borders.empty(0, 6)
        toolTipText = "LLM token estimate (o200k_base). Select text to count selection; otherwise counts the active file."
    }

    private var statusBar: StatusBar? = null
    private var pendingTask: Future<*>? = null
    private val selectionListener = object : SelectionListener {
        override fun selectionChanged(e: SelectionEvent) = scheduleRefresh()
    }
    private val attachedEditors = mutableSetOf<Editor>()

    override fun ID(): String = ID

    override fun getComponent(): JComponent = label

    override fun install(statusBar: StatusBar) {
        this.statusBar = statusBar

        project.messageBus.connect(this).subscribe(
            FileEditorManagerListener.FILE_EDITOR_MANAGER,
            object : FileEditorManagerListener {
                override fun selectionChanged(event: FileEditorManagerEvent) {
                    attachToCurrentEditor()
                    scheduleRefresh()
                }
            },
        )

        attachToCurrentEditor()
        scheduleRefresh()
    }

    override fun dispose() {
        pendingTask?.cancel(false)
        attachedEditors.forEach { it.selectionModel.removeSelectionListener(selectionListener) }
        attachedEditors.clear()
    }

    private fun attachToCurrentEditor() {
        val editor = currentEditor() ?: return
        if (attachedEditors.add(editor)) {
            editor.selectionModel.addSelectionListener(selectionListener)
            Disposer.register(this) {
                if (attachedEditors.remove(editor)) {
                    editor.selectionModel.removeSelectionListener(selectionListener)
                }
            }
        }
    }

    private fun currentEditor(): Editor? =
        FileEditorManager.getInstance(project).selectedTextEditor

    private fun scheduleRefresh() {
        pendingTask?.cancel(false)
        val editor = currentEditor()
        if (editor == null) {
            updateLabel("tokens: –")
            return
        }
        val selected = editor.selectionModel.selectedText
        val text = if (!selected.isNullOrEmpty()) selected else editor.document.text
        val isSelection = !selected.isNullOrEmpty()

        pendingTask = AppExecutorUtil.getAppScheduledExecutorService().schedule(
            {
                val count = Tokenizer.count(text)
                val prefix = if (isSelection) "sel" else "file"
                val warn = if (text.length > LARGE_TEXT_THRESHOLD) "~" else ""
                updateLabel("$prefix: $warn${formatCount(count)} tok")
            },
            DEBOUNCE_MS,
            TimeUnit.MILLISECONDS,
        )
    }

    private fun updateLabel(text: String) {
        ApplicationManager.getApplication().invokeLater {
            label.text = text
            statusBar?.updateWidget(ID)
        }
    }

    private fun formatCount(n: Int): String = when {
        n < 1_000 -> n.toString()
        n < 1_000_000 -> "%.1fk".format(n / 1_000.0)
        else -> "%.2fM".format(n / 1_000_000.0)
    }

    override fun getPresentation(): StatusBarWidget.WidgetPresentation? = null
}
