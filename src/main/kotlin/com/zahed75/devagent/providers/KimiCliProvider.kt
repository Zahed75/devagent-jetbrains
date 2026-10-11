package com.zahed75.devagent.providers

import com.zahed75.devagent.settings.DevAgentSettingsService
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class KimiCliProvider(private val settings: DevAgentSettingsService) : AIProvider {
    override val id: String = "kimi-cli"
    override val displayName: String = "Kimi Code (CLI)"
    override val providerType: ProviderType = ProviderType.KIMI_CLI
    override val availableModels: List<ModelDescriptor> = listOf(
        ModelDescriptor("kimi-code", "Kimi Code", supportsStreaming = false, capabilities = setOf("chat"))
    )

    override fun isAvailable(): Boolean {
        val executable = settings.state.kimiCliPath.ifBlank { "kimi" }
        return runCatching {
            val process = ProcessBuilder(executable, "--version").start()
            process.waitFor(8, TimeUnit.SECONDS)
        }.getOrDefault(false)
    }

    override fun testConnection(): ProviderCheckResult {
        val executable = settings.state.kimiCliPath.ifBlank { "kimi" }
        return runCatching {
            val process = ProcessBuilder(executable, "--help").start()
            val output = process.inputStream.bufferedReader().use(BufferedReader::readText)
            val exitCode = process.waitFor(8, TimeUnit.SECONDS).let { if (it) 0 else 1 }
            if (exitCode == 0) {
                ProviderCheckResult(true, "Kimi CLI is reachable: $executable\n${output.take(200)}")
            } else {
                ProviderCheckResult(false, "Kimi CLI process exited without a successful result.")
            }
        }.getOrElse {
            ProviderCheckResult(false, "Kimi CLI is not configured or not installed. Set the path in DevAgent settings.")
        }
    }

    override fun send(request: AgentRequest): AgentResponse {
        val isAvailable = isAvailable()
        return if (!isAvailable) {
            AgentResponse(
                text = "Kimi CLI is not available. Set the executable path in DevAgent settings and validate it with the connection test.",
                providerId = id,
                modelId = "kimi-code",
                success = false,
            )
        } else {
            AgentResponse(
                text = "Kimi CLI adapter detected. The provider is configured, but actual request execution requires a verified CLI contract. Use the provider configuration screen to validate the installed binary and its supported commands.",
                providerId = id,
                modelId = "kimi-code",
                success = true,
            )
        }
    }
}
