package com.zahed75.devagent.context

import com.intellij.openapi.project.Project

class ProjectContextService(private val project: Project) {
    fun collectContext(exclusions: List<String> = emptyList(), maxFiles: Int = 200): List<ProjectFileContext> {
        val basePath = project.basePath ?: return emptyList()
        return ProjectContextScanner.scan(basePath, exclusions, maxFiles)
    }
}
