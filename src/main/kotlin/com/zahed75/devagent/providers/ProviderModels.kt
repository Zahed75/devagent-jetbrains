package com.zahed75.devagent.providers

import com.zahed75.devagent.settings.DevAgentSettingsService

enum class ProviderType {
    KIMI_CLI,
    CLAUDE_API,
    OPENAI_COMPATIBLE,
    UNKNOWN,
}

data class ModelDescriptor(
    val id: String,
    val displayName: String,
    val supportsStreaming: Boolean = false,
    val capabilities: Set<String> = emptySet(),
)

data class AgentRequest(
    val prompt: String,
    val contextFiles: List<String> = emptyList(),
    val selectedCode: String? = null,
    val conversationId: String? = null,
)

data class ProviderCheckResult(
    val ok: Boolean,
    val message: String,
)

data class AgentResponse(
    val text: String,
    val providerId: String,
    val modelId: String,
    val success: Boolean = true,
    val usage: Map<String, Long> = emptyMap(),
)

interface AIProvider {
    val id: String
    val displayName: String
    val providerType: ProviderType
    val availableModels: List<ModelDescriptor>

    fun isAvailable(): Boolean
    fun testConnection(): ProviderCheckResult
    fun send(request: AgentRequest): AgentResponse
}

class ProviderRegistry(private val settings: DevAgentSettingsService) {
    fun providers(): List<AIProvider> = listOf(
        KimiCliProvider(settings),
        OpenAICompatibleProvider(settings),
        ClaudeApiProvider(settings),
    )
}
