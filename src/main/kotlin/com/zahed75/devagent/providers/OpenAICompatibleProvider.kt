package com.zahed75.devagent.providers

import com.zahed75.devagent.settings.DevAgentSettingsService

class OpenAICompatibleProvider(private val settings: DevAgentSettingsService) : AIProvider {
    override val id: String = "openai-compatible"
    override val displayName: String = "OpenAI-compatible"
    override val providerType: ProviderType = ProviderType.OPENAI_COMPATIBLE
    override val availableModels: List<ModelDescriptor> = listOf(
        ModelDescriptor("gpt-4o-mini", "GPT-4o mini", supportsStreaming = true, capabilities = setOf("chat", "streaming"))
    )

    override fun isAvailable(): Boolean = settings.state.baseUrl.isNotBlank() && settings.state.apiKey.isNotBlank()

    override fun testConnection(): ProviderCheckResult {
        return if (isAvailable()) {
            ProviderCheckResult(true, "OpenAI-compatible provider is configured and ready for a connection check.")
        } else {
            ProviderCheckResult(false, "Set a base URL and API key before enabling the OpenAI-compatible provider.")
        }
    }

    override fun send(request: AgentRequest): AgentResponse {
        return AgentResponse(
            text = "The OpenAI-compatible provider is configured for later API integration. The final transport and streaming contract should be validated against the selected backend before production use.",
            providerId = id,
            modelId = settings.state.model,
            success = isAvailable(),
        )
    }
}
