package com.predictx.tokencounter

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory

class TokenCountWidgetFactory : StatusBarWidgetFactory {
    override fun getId(): String = TokenCountWidget.ID
    override fun getDisplayName(): String = "Token Count"
    override fun isAvailable(project: Project): Boolean = true
    override fun createWidget(project: Project): StatusBarWidget = TokenCountWidget(project)
    override fun isConfigurable(): Boolean = true
}
