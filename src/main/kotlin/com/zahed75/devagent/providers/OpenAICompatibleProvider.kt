package com.zahed75.devagent.providers

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.ide.passwordSafe.PasswordSafe
import com.zahed75.devagent.settings.DevAgentSettingsService

class OpenAICompatibleProvider(private val settings: DevAgentSettingsService) : AIProvider {
    override val id: String = "openai-compatible"
    override val displayName: String = "OpenAI-compatible"
    override val providerType: ProviderType = ProviderType.OPENAI_COMPATIBLE
    override val availableModels: List<ModelDescriptor> = emptyList()

    override fun isAvailable(): Boolean =
        settings.state.baseUrl.isNotBlank() &&
            !PasswordSafe.instance.getPassword(CredentialAttributes("com.zahed75.devagent.apiKey")).isNullOrBlank()

    override fun testConnection(): ProviderCheckResult {
        return if (isAvailable()) {
            ProviderCheckResult(false, "OpenAI-compatible HTTP transport is not implemented. No network request was made.")
        } else {
            ProviderCheckResult(false, "Set a base URL and API key in DevAgent settings.")
        }
    }

    override fun send(request: AgentRequest): AgentResponse {
        return AgentResponse(
            text = "OpenAI-compatible requests are not implemented. No prompt or project content was sent.",
            providerId = id,
            modelId = settings.state.model,
            success = false,
        )
    }
}
