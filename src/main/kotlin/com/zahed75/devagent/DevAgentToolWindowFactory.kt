package com.zahed75.devagent

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import java.awt.BorderLayout
import java.awt.CardLayout
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.ListSelectionModel

class DevAgentToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = DevAgentToolWindow(project).createPanel()
        val content = toolWindow.contentManager.factory.createContent(panel, "DevAgent", false)
        toolWindow.contentManager.addContent(content)
    }
}

class DevAgentToolWindow(private val project: Project) {
    fun createPanel(): JComponent {
        val root = JPanel(BorderLayout())
        root.border = BorderFactory.createEmptyBorder(8, 8, 8, 8)

        val sidebarModel = com.intellij.ui.components.JBList<String>(listOf("Inbox", "Project", "Context", "Settings"))
        sidebarModel.selectionMode = ListSelectionModel.SINGLE_SELECTION
        sidebarModel.selectedIndex = 0

        val sidebarPanel = JPanel(BorderLayout())
        sidebarPanel.add(JBScrollPane(sidebarModel), BorderLayout.CENTER)
        sidebarPanel.border = BorderFactory.createEmptyBorder(0, 0, 0, 8)

        val centerCard = JPanel(CardLayout())
        centerCard.add(buildInboxPanel(), "Inbox")
        centerCard.add(buildProjectPanel(), "Project")
        centerCard.add(buildContextPanel(), "Context")
        centerCard.add(buildSettingsPanel(), "Settings")

        root.add(sidebarPanel, BorderLayout.WEST)
        root.add(centerCard, BorderLayout.CENTER)
        return root
    }

    private fun buildInboxPanel(): JPanel {
        val panel = JPanel(BorderLayout())
        val header = JBLabel("DevAgent Workspace")
        val conversationArea = JBTextArea()
        conversationArea.isEditable = false
        conversationArea.text = "Welcome to DevAgent.\n\nThis foundation build includes: \n- JetBrains tool window\n- provider abstractions\n- Kimi CLI support hooks\n- project context scanning\n- persistent settings"

        val input = JBTextArea()
        input.lineWrap = true
        input.wrapStyleWord = true
        input.rows = 6

        val actions = JPanel()
        actions.layout = BoxLayout(actions, BoxLayout.X_AXIS)
        actions.add(JButton("Send"))
        actions.add(JButton("Stop"))
        actions.add(JButton("Retry"))

        val body = JPanel(BorderLayout())
        body.add(JBScrollPane(conversationArea), BorderLayout.CENTER)
        body.add(input, BorderLayout.SOUTH)

        panel.add(header, BorderLayout.NORTH)
        panel.add(body, BorderLayout.CENTER)
        panel.add(actions, BorderLayout.SOUTH)
        return panel
    }

    private fun buildProjectPanel(): JPanel {
        val panel = JPanel(BorderLayout())
        val label = JBLabel("Project context")
        val text = JBTextArea()
        text.isEditable = false
        val basePath = project.basePath ?: "<no project open>"
        text.text = "Active project: $basePath\n\nContext scanning is ready for later milestones and supports exclusions, file grouping, and local documentation attachments."
        panel.add(label, BorderLayout.NORTH)
        panel.add(JBScrollPane(text), BorderLayout.CENTER)
        return panel
    }

    private fun buildContextPanel(): JPanel {
        val panel = JPanel(BorderLayout())
        val label = JBLabel("Included files")
        val text = JBTextArea()
        text.isEditable = false
        text.text = "No files selected yet.\n\nThis panel is the seam for later context selection, file attachments, and citations."
        panel.add(label, BorderLayout.NORTH)
        panel.add(JBScrollPane(text), BorderLayout.CENTER)
        return panel
    }

    private fun buildSettingsPanel(): JPanel {
        val panel = JPanel(BorderLayout())
        val label = JBLabel("Provider settings")
        val text = JBTextArea()
        text.isEditable = false
        text.text = "Provider: Kimi CLI\nModel: kimi-code\nSecurity: credential storage pending final integration\nConnection tests: available via provider adapters"
        panel.add(label, BorderLayout.NORTH)
        panel.add(JBScrollPane(text), BorderLayout.CENTER)
        return panel
    }
}
