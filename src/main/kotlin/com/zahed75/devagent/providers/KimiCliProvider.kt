package com.zahed75.devagent.providers

import com.zahed75.devagent.settings.DevAgentSettingsService
import java.io.File

class KimiCliProvider(private val settings: DevAgentSettingsService) : AIProvider {
    override val id: String = "kimi-cli"
    override val displayName: String = "Kimi Code (CLI)"
    override val providerType: ProviderType = ProviderType.KIMI_CLI
    override val availableModels: List<ModelDescriptor> = emptyList()

    override fun isAvailable(): Boolean = findExecutable() != null

    override fun testConnection(): ProviderCheckResult {
        val executable = findExecutable()
        return if (executable == null) {
            ProviderCheckResult(false, "Kimi CLI was not found. Set its executable path in DevAgent settings.")
        } else {
            ProviderCheckResult(
                false,
                "Found $executable, but this build has no verified Kimi request protocol. No process was started.",
            )
        }
    }

    override fun send(request: AgentRequest): AgentResponse {
        return AgentResponse(
            text = "Kimi requests are not implemented. No prompt or project content was sent.",
            providerId = id,
            modelId = settings.state.model,
            success = false,
        )
    }

    private fun findExecutable(): File? {
        val configuredPath = settings.state.kimiCliPath.trim()
        val command = configuredPath.ifBlank { "kimi" }
        val hasPath = command.contains(File.separator) || (File.separatorChar == '\\' && command.contains('/'))
        val candidates = if (hasPath) {
            listOf(File(command))
        } else {
            val pathEntries = System.getenv("PATH").orEmpty().split(File.pathSeparator)
            val commands = if (File.separatorChar == '\\' && !command.endsWith(".exe", ignoreCase = true)) {
                listOf(command, "$command.exe")
            } else {
                listOf(command)
            }
            pathEntries.flatMap { directory -> commands.map { File(directory, it) } }
        }
        return candidates.firstOrNull { it.isFile && (File.separatorChar == '\\' || it.canExecute()) }
    }
}
