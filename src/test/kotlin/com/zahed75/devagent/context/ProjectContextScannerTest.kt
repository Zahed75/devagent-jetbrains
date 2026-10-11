package com.zahed75.devagent.context

import org.junit.jupiter.api.Test
import kotlin.io.path.createDirectory
import kotlin.io.path.writeText
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import java.nio.file.Files

class ProjectContextScannerTest {
    @Test
    fun `scan finds text files and ignores excluded directories`() {
        val root = Files.createTempDirectory("devagent-context")
        val src = root.resolve("src").createDirectory()
        src.resolve("main.kt").writeText("fun main() {}\n")
        src.resolve("build.gradle.kts").writeText("plugins {}")
        val buildDir = root.resolve("build").createDirectory()
        buildDir.resolve("ignored.txt").writeText("ignored content")

        val scan = ProjectContextScanner.scan(root.toString(), listOf("build"), maxFiles = 50)

        assertTrue(scan.any { it.path == "src/main.kt" })
        assertTrue(scan.any { it.path == "src/build.gradle.kts" })
        assertTrue(scan.none { it.path.startsWith("build/") })
        assertEquals(2, scan.size)
    }

    @Test
    fun `scan matches excluded path components without excluding similarly named files`() {
        val root = Files.createTempDirectory("devagent-exclusions")
        root.resolve("checkout").createDirectory().resolve("keep.kt").writeText("keep")
        root.resolve("build").createDirectory().resolve("skip.kt").writeText("skip")
        root.resolve("image.png").writeText("not source")

        val scan = ProjectContextScanner.scan(root.toString(), listOf("build"))

        assertEquals(listOf("checkout/keep.kt"), scan.map { it.path })
    }

    @Test
    fun `scan respects non-positive and maximum file limits`() {
        val root = Files.createTempDirectory("devagent-file-limit")
        root.resolve("first.kt").writeText("first")
        root.resolve("second.kt").writeText("second")

        assertTrue(ProjectContextScanner.scan(root.toString(), maxFiles = 0).isEmpty())
        assertTrue(ProjectContextScanner.scan(root.toString(), maxFiles = 1).size <= 1)
    }
}
