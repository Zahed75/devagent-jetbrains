package com.zahed75.devagent.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import java.awt.BorderLayout
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel

class DevAgentSettingsState {
    var provider: String = "kimi-cli"
    var model: String = "kimi-code"
    var baseUrl: String = ""
    var apiKey: String = ""
    var kimiCliPath: String = ""
    var projectExclusions: String = ".git,build,dist,target,node_modules,.idea,.gradle,.venv,__pycache__"
    var contextLimit: Int = 12000
    var conversationRetentionDays: Int = 30
    var toolPermissions: String = "read,search,edit,tests"
    var autoApproveHighRiskCommands: Boolean = false
}

@State(name = "DevAgentSettings", storages = [Storage("devagent-settings.xml")])
@Service(Service.Level.APP)
class DevAgentSettingsService : PersistentStateComponent<DevAgentSettingsState> {
    private var state = DevAgentSettingsState()

    override fun getState(): DevAgentSettingsState = state

    override fun loadState(state: DevAgentSettingsState) {
        this.state = state
    }

    fun state(): DevAgentSettingsState = state
}

class DevAgentConfigurable : SearchableConfigurable {
    private val settings = ApplicationManager.getApplication().getService(DevAgentSettingsService::class.java)
    private val providerCombo = ComboBox(arrayOf("Kimi CLI", "OpenAI-compatible", "Claude API"))
    private val modelField = JBTextField(settings.state().model)
    private val cliPathField = JBTextField(settings.state().kimiCliPath)
    private val baseUrlField = JBTextField(settings.state().baseUrl)
    private val apiKeyField = JBTextField(settings.state().apiKey)
    private val exclusionField = JBTextField(settings.state().projectExclusions)

    override fun getId(): String = "DevAgent.Settings"

    override fun getDisplayName(): String = "DevAgent"

    override fun createComponent(): JComponent {
        val root = JPanel(BorderLayout())
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

        panel.add(createLabeledRow("Provider:", providerCombo))
        panel.add(createLabeledRow("Model:", modelField))
        panel.add(createLabeledRow("Kimi CLI path:", cliPathField))
        panel.add(createLabeledRow("Base URL:", baseUrlField))
        panel.add(createLabeledRow("API key:", apiKeyField))
        panel.add(createLabeledRow("Project exclusions:", exclusionField))

        root.add(panel, BorderLayout.NORTH)
        return root
    }

    override fun isModified(): Boolean = true

    override fun apply() {
        settings.state().provider = when (providerCombo.selectedItem as String) {
            "Kimi CLI" -> "kimi-cli"
            "OpenAI-compatible" -> "openai-compatible"
            else -> "claude-api"
        }
        settings.state().model = modelField.text
        settings.state().kimiCliPath = cliPathField.text
        settings.state().baseUrl = baseUrlField.text
        settings.state().apiKey = apiKeyField.text
        settings.state().projectExclusions = exclusionField.text
    }

    override fun reset() {
        providerCombo.selectedItem = when (settings.state().provider) {
            "kimi-cli" -> "Kimi CLI"
            "openai-compatible" -> "OpenAI-compatible"
            else -> "Claude API"
        }
        modelField.text = settings.state().model
        cliPathField.text = settings.state().kimiCliPath
        baseUrlField.text = settings.state().baseUrl
        apiKeyField.text = settings.state().apiKey
        exclusionField.text = settings.state().projectExclusions
    }

    private fun createLabeledRow(labelText: String, field: JComponent): JPanel {
        val row = JPanel(BorderLayout())
        row.add(JBLabel(labelText), BorderLayout.WEST)
        row.add(field, BorderLayout.CENTER)
        return row
    }
}
