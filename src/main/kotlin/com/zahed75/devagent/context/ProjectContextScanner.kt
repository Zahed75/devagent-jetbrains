package com.zahed75.devagent.context

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.extension
import kotlin.io.path.isRegularFile

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

private val textExtensions = setOf(
    "kt", "java", "py", "js", "ts", "tsx", "jsx", "go", "rs", "php", "dart",
    "sql", "xml", "json", "yaml", "yml", "md", "txt", "gradle", "kts", "properties",
    "html", "css", "scss", "ini", "toml",
)

private val textFileNames = setOf("dockerfile", "makefile", "readme", "license")

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
        if (!Files.isDirectory(root)) {
            return emptyList()
        }

        val excluded = (defaultExclusions + exclusions.map { it.trim().lowercase() }.filter { it.isNotEmpty() })

        val collected = mutableListOf<ProjectFileContext>()
        Files.walk(root).use { stream ->
            val iterator = stream.iterator()
            while (iterator.hasNext() && collected.size < maxFiles) {
                val file = iterator.next()
                if (!file.isRegularFile()) continue
                val relativePath = root.relativize(file)
                if (isExcluded(relativePath, excluded)) continue
                if (!isTextLike(file)) continue

                val relative = relativePath.toString().replace('\\', '/')
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

    /** Matches excluded names against each segment of the path relative to the project root. */
    private fun isExcluded(relative: Path, excluded: Set<String>): Boolean =
        relative.any { segment -> segment.toString().lowercase() in excluded }

    private fun isTextLike(path: Path): Boolean {
        val name = path.fileName.toString().lowercase()
        return path.extension.lowercase() in textExtensions || name in textFileNames
    }

    private fun kindFromName(name: String): String {
        val extension = name.substringAfterLast('.', "")
        return if (extension.isNotBlank()) extension.uppercase() else "FILE"
    }
}
