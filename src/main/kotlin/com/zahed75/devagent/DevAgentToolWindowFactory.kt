package com.zahed75.devagent

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.zahed75.devagent.context.ProjectFileContext
import com.zahed75.devagent.context.ProjectContextService
import com.zahed75.devagent.providers.AgentRequest
import com.zahed75.devagent.providers.AgentResponse
import com.zahed75.devagent.providers.ProviderRegistry
import com.zahed75.devagent.settings.DevAgentSettingsService
import java.awt.BorderLayout
import java.awt.CardLayout
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.DefaultListModel
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

private class DevAgentToolWindow(private val project: Project) {
    private val settings = ApplicationManager.getApplication().getService(DevAgentSettingsService::class.java)
    private val contextFiles = DefaultListModel<String>()
    private val conversation = JBTextArea()
    private val prompt = JBTextArea(5, 40)
    private val chatStatus = JBLabel("Provider requests are not implemented in this build.")

    fun createPanel(): JComponent {
        val root = JPanel(BorderLayout(8, 0))
        root.border = BorderFactory.createEmptyBorder(8, 8, 8, 8)

        val sidebar = JBList<String>(listOf("Chat", "Project context", "Settings"))
        sidebar.selectionMode = ListSelectionModel.SINGLE_SELECTION
        sidebar.selectedIndex = 0
        sidebar.border = BorderFactory.createEmptyBorder(4, 4, 4, 4)

        val cards = JPanel(CardLayout())
        cards.add(createChatPanel(), "Chat")
        cards.add(createProjectPanel(), "Project context")
        cards.add(createSettingsPanel(), "Settings")
        sidebar.addListSelectionListener { event ->
            if (!event.valueIsAdjusting) {
                (cards.layout as CardLayout).show(cards, sidebar.selectedValue)
            }
        }

        root.add(JBScrollPane(sidebar), BorderLayout.WEST)
        root.add(cards, BorderLayout.CENTER)
        return root
    }

    private fun createChatPanel(): JPanel {
        conversation.isEditable = false
        conversation.lineWrap = true
        conversation.wrapStyleWord = true
        conversation.text = "Welcome to DevAgent.\n" +
            "This chat is temporary. Provider request execution is not implemented; prompts stay local."

        prompt.lineWrap = true
        prompt.wrapStyleWord = true

        val newChat = JButton("New chat")
        newChat.addActionListener { conversation.text = "" }
        val send = JButton("Send")
        send.addActionListener { sendPrompt(send) }

        val buttons = JPanel()
        buttons.layout = BoxLayout(buttons, BoxLayout.X_AXIS)
        buttons.add(newChat)
        buttons.add(send)

        val composer = JPanel(BorderLayout(0, 4))
        composer.add(JBScrollPane(prompt), BorderLayout.CENTER)
        composer.add(buttons, BorderLayout.SOUTH)

        val footer = JPanel(BorderLayout(0, 4))
        footer.add(composer, BorderLayout.CENTER)
        footer.add(chatStatus, BorderLayout.SOUTH)
        val panel = JPanel(BorderLayout(0, 6))
        panel.add(JBLabel("Conversation"), BorderLayout.NORTH)
        panel.add(JBScrollPane(conversation), BorderLayout.CENTER)
        panel.add(footer, BorderLayout.SOUTH)
        return panel
    }

    private fun sendPrompt(sendButton: JButton) {
        val text = prompt.text.trim()
        if (text.isEmpty()) return

        val provider = ProviderRegistry(settings).providers()
            .firstOrNull { it.id == settings.state().provider }
            ?: return
        sendButton.isEnabled = false
        prompt.isEnabled = false
        chatStatus.text = "Running ${provider.displayName} adapter..."

        object : Task.Backgroundable(project, "DevAgent", true) {
            private var response: AgentResponse? = null
            private var failure: Throwable? = null

            override fun run(indicator: ProgressIndicator) {
                try {
                    response = provider.send(AgentRequest(text))
                } catch (error: Exception) {
                    failure = error
                }
            }

            override fun onSuccess() {
                sendButton.isEnabled = true
                prompt.isEnabled = true
                conversation.append("\n\nYou: $text")
                val result = response
                if (result?.success == true) {
                    conversation.append("\n\nDevAgent: ${result.text}")
                    chatStatus.text = "Completed with ${provider.displayName}."
                } else {
                    val detail = failure?.message ?: result?.text ?: "The request did not complete."
                    conversation.append("\n\nDevAgent: $detail")
                    chatStatus.text = "No provider response was received."
                }
                prompt.text = ""
            }

            override fun onThrowable(error: Throwable) {
                sendButton.isEnabled = true
                prompt.isEnabled = true
                chatStatus.text = "Provider request failed: ${error.message ?: error.javaClass.simpleName}"
            }

            override fun onCancel() {
                sendButton.isEnabled = true
                prompt.isEnabled = true
                chatStatus.text = "Request cancelled. No provider response was received."
            }
        }.queue()
    }

    private fun createProjectPanel(): JPanel {
        val fileList = JBList<String>(contextFiles)
        val status = JBLabel(project.basePath ?: "No project is open.")
        val refresh = JButton("Scan project files")
        refresh.addActionListener { scanProject(status, refresh) }

        val header = JPanel(BorderLayout(6, 0))
        header.add(status, BorderLayout.CENTER)
        header.add(refresh, BorderLayout.EAST)

        val panel = JPanel(BorderLayout(0, 6))
        panel.add(header, BorderLayout.NORTH)
        panel.add(JBScrollPane(fileList), BorderLayout.CENTER)
        panel.add(
            JBLabel("Only local text-file metadata is listed. No project content is sent to a provider."),
            BorderLayout.SOUTH,
        )
        return panel
    }

    private fun scanProject(status: JBLabel, refresh: JButton) {
        val exclusions = settings.state().projectExclusions.split(',').map(String::trim).filter(String::isNotEmpty)
        refresh.isEnabled = false
        status.text = "Scanning ${project.basePath ?: "project"}..."

        object : Task.Backgroundable(project, "Scan DevAgent project context", true) {
            private var result: List<ProjectFileContext> = emptyList()
            private var failure: Throwable? = null

            override fun run(indicator: ProgressIndicator) {
                try {
                    result = project.getService(ProjectContextService::class.java)
                        .collectContext(exclusions, maxFiles = 200)
                } catch (error: Exception) {
                    failure = error
                }
            }

            override fun onSuccess() {
                refresh.isEnabled = true
                val error = failure
                if (error != null) {
                    status.text = "Scan failed: ${error.message ?: error.javaClass.simpleName}"
                    return
                }
                contextFiles.clear()
                result.forEach { file ->
                    contextFiles.addElement("${file.path} — ${file.lines} lines, ${file.sizeBytes} bytes")
                }
                status.text = "Found ${result.size} text files in ${project.basePath ?: "project"}."
            }

            override fun onThrowable(error: Throwable) {
                refresh.isEnabled = true
                status.text = "Scan failed: ${error.message ?: error.javaClass.simpleName}"
            }

            override fun onCancel() {
                refresh.isEnabled = true
                status.text = "Scan cancelled."
            }
        }.queue()
    }

    private fun createSettingsPanel(): JPanel {
        val panel = JPanel(BorderLayout())
        panel.add(JBLabel("DevAgent settings"), BorderLayout.NORTH)
        panel.add(
            JBLabel(
                "<html>Provider: ${settings.state().provider}<br>" +
                    "Model: ${settings.state().model}<br><br>" +
                    "Configure providers and project exclusions in Settings | Tools | DevAgent.<br>" +
                    "Provider execution is not implemented in this build.</html>",
            ),
            BorderLayout.CENTER,
        )
        return panel
    }
}
