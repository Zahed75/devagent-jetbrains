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
        val buildDir = root.resolve("build").createDirectory()
        buildDir.resolve("ignored.txt").writeText("ignored content")

        val scan = ProjectContextScanner.scan(root.toString(), listOf("build"), maxFiles = 50)

        assertTrue(scan.any { it.path == "src/main.kt" })
        assertTrue(scan.none { it.path.contains("build") })
        assertEquals(1, scan.size)
    }
}
