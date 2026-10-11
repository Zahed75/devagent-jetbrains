package com.zahed75.devagent.context

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.math.min

private val defaultExclusions = setOf(
    ".git",
    ".idea",
    ".gradle",
    ".venv",
    ".mypy_cache",
    ".pytest_cache",
    "build",
    "dist",
    "out",
    "target",
    "node_modules",
    "__pycache__",
    "venv",
    "venv.bak",
)

data class ProjectFileContext(
    val path: String,
    val kind: String,
    val sizeBytes: Long,
    val lines: Int,
    val depth: Int,
)

object ProjectContextScanner {
    fun scan(rootPath: String, exclusions: List<String> = emptyList(), maxFiles: Int = 200): List<ProjectFileContext> {
        val root = Paths.get(rootPath)
        if (!root.toFile().exists()) {
            return emptyList()
        }

        val normalizedExclusions = (defaultExclusions + exclusions.map { it.trim() }.filter { it.isNotEmpty() })
            .map { it.trim().lowercase() }
            .toSet()

        val collected = mutableListOf<ProjectFileContext>()
        Files.walk(root).use { stream ->
            stream.filter { it.isRegularFile() }
                .filter { !shouldExcludePath(it, normalizedExclusions) }
                .filter { isTextLike(it) }
                .forEach { file ->
                    if (collected.size >= maxFiles) {
                        return@forEach
                    }
                    val relative = root.relativize(file).toString().replace('\\', '/')
                    val lines = try {
                        Files.readAllLines(file).size
                    } catch (_: Exception) {
                        0
                    }
                    collected.add(
                        ProjectFileContext(
                            path = relative,
                            kind = kindFromName(file.fileName.toString()),
                            sizeBytes = Files.size(file),
                            lines = lines,
                            depth = relative.split('/').size - 1,
                        )
                    )
                }
        }

        return collected.sortedBy { it.path }
    }

    private fun shouldExcludePath(path: Path, exclusions: Set<String>): Boolean {
        val normalized = path.toString().replace('\\', '/').lowercase()
        return exclusions.any { token ->
            normalized.contains("/$token/") ||
                normalized.endsWith("/$token") ||
                normalized.contains("\\$token\\") ||
                normalized.endsWith("\\$token") ||
                normalized.contains("/$token") ||
                normalized.contains("\\$token")
        }
    }

    private fun isTextLike(path: Path): Boolean {
        val name = path.fileName.toString().lowercase()
        val knownTextExtensions = setOf(
            ".kt", ".java", ".py", ".js", ".ts", ".tsx", ".jsx", ".go", ".rs", ".php", ".dart",
            ".sql", ".xml", ".json", ".yaml", ".yml", ".md", ".txt", ".gradle", ".properties",
            ".html", ".css", ".scss", ".ini", ".toml"
        )
        val extension = path.extension.lowercase()
        return extension in knownTextExtensions || name in setOf("Dockerfile", "Makefile", "README", "LICENSE")
    }

    private fun kindFromName(name: String): String {
        val extension = name.substringAfterLast('.', "")
        return if (extension.isNotBlank()) extension.uppercase() else "FILE"
    }
}
