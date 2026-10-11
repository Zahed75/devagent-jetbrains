package com.zahed75.devagent.context

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.FileVisitResult
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

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
        if (maxFiles <= 0 || !Files.isDirectory(root)) {
            return emptyList()
        }

        val normalizedExclusions = (defaultExclusions + exclusions)
            .map { it.trim().replace('\\', '/').trim('/').lowercase() }
            .filter { it.isNotEmpty() }
            .toSet()

        val collected = mutableListOf<ProjectFileContext>()
        Files.walkFileTree(root, object : SimpleFileVisitor<Path>() {
            override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                if (collected.size >= maxFiles) return FileVisitResult.TERMINATE
                return if (dir != root && shouldExcludePath(root, dir, normalizedExclusions)) {
                    FileVisitResult.SKIP_SUBTREE
                } else {
                    FileVisitResult.CONTINUE
                }
            }

            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                if (collected.size >= maxFiles) return FileVisitResult.TERMINATE
                if (!attrs.isRegularFile || shouldExcludePath(root, file, normalizedExclusions) || !isTextLike(file)) {
                    return FileVisitResult.CONTINUE
                }
                val size = attrs.size()
                if (size > MAX_FILE_SIZE_BYTES) {
                    return FileVisitResult.CONTINUE
                }
                val lines = runCatching { Files.readAllLines(file).size }.getOrNull()
                    ?: return FileVisitResult.CONTINUE
                val relative = root.relativize(file).toString().replace('\\', '/')
                collected.add(
                    ProjectFileContext(
                        path = relative,
                        kind = kindFromName(file.fileName.toString()),
                        sizeBytes = size,
                        lines = lines,
                        depth = relative.split('/').size - 1,
                    )
                )
                return FileVisitResult.CONTINUE
            }

            override fun visitFileFailed(file: Path, exc: java.io.IOException): FileVisitResult =
                FileVisitResult.CONTINUE
        })

        return collected.sortedBy { it.path }
    }

    private fun shouldExcludePath(root: Path, path: Path, exclusions: Set<String>): Boolean {
        val relative = root.relativize(path).normalize().toString().replace('\\', '/').lowercase()
        return exclusions.any { exclusion ->
            relative == exclusion ||
                relative.startsWith("$exclusion/") ||
                relative.split('/').any { it == exclusion }
        }
    }

    private fun isTextLike(path: Path): Boolean {
        val fileName = path.fileName.toString()
        val name = fileName.lowercase()
        val knownTextExtensions = setOf(
            ".kt", ".kts", ".java", ".py", ".js", ".ts", ".tsx", ".jsx", ".go", ".rs", ".php", ".dart",
            ".sql", ".xml", ".json", ".yaml", ".yml", ".md", ".txt", ".gradle", ".properties",
            ".html", ".css", ".scss", ".ini", ".toml",
        )
        val extension = fileName.lastIndexOf('.').takeIf { it >= 0 }?.let { fileName.substring(it).lowercase() }.orEmpty()
        return extension in knownTextExtensions || name in setOf("dockerfile", "makefile", "readme", "license")
    }

    private fun kindFromName(name: String): String {
        val extension = name.substringAfterLast('.', "")
        return if (extension.isNotBlank()) extension.uppercase() else "FILE"
    }

    private const val MAX_FILE_SIZE_BYTES = 1_000_000L
}
