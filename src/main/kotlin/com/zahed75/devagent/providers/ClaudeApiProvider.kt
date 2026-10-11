package com.zahed75.devagent.providers

import com.zahed75.devagent.settings.DevAgentSettingsService

class ClaudeApiProvider(private val settings: DevAgentSettingsService) : AIProvider {
    override val id: String = "claude-api"
    override val displayName: String = "Claude API"
    override val providerType: ProviderType = ProviderType.CLAUDE_API
    override val availableModels: List<ModelDescriptor> = listOf(
        ModelDescriptor("claude-sonnet-4", "Claude Sonnet 4", supportsStreaming = true, capabilities = setOf("chat", "streaming"))
    )

    override fun isAvailable(): Boolean = settings.state.apiKey.isNotBlank()

    override fun testConnection(): ProviderCheckResult {
        return if (isAvailable()) {
            ProviderCheckResult(true, "Claude API is configured and ready for a later backend validation pass.")
        } else {
            ProviderCheckResult(false, "Add a valid API key before enabling the Claude integration.")
        }
    }

    override fun send(request: AgentRequest): AgentResponse {
        return AgentResponse(
            text = "Claude integration is ready for configuration and future API testing. The plugin architecture supports it without hard-coding vendor-specific assumptions.",
            providerId = id,
            modelId = settings.state.model,
            success = isAvailable(),
        )
    }
}
