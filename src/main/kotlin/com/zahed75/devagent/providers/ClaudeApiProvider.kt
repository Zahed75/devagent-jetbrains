package com.zahed75.devagent.providers

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.ide.passwordSafe.PasswordSafe
import com.zahed75.devagent.settings.DevAgentSettingsService

class ClaudeApiProvider(private val settings: DevAgentSettingsService) : AIProvider {
    override val id: String = "claude-api"
    override val displayName: String = "Claude API"
    override val providerType: ProviderType = ProviderType.CLAUDE_API
    override val availableModels: List<ModelDescriptor> = emptyList()

    override fun isAvailable(): Boolean =
        !PasswordSafe.instance.getPassword(CredentialAttributes("com.zahed75.devagent.apiKey")).isNullOrBlank()

    override fun testConnection(): ProviderCheckResult {
        return if (isAvailable()) {
            ProviderCheckResult(false, "Claude API transport is not implemented. No network request was made.")
        } else {
            ProviderCheckResult(false, "Configure a Claude API key in DevAgent settings.")
        }
    }

    override fun send(request: AgentRequest): AgentResponse {
        return AgentResponse(
            text = "Claude API requests are not implemented. No prompt or project content was sent.",
            providerId = id,
            modelId = settings.state.model,
            success = false,
        )
    }
}
