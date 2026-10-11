package com.zahed75.devagent.context

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class ProjectContextService(private val project: Project) {
    fun collectContext(exclusions: List<String> = emptyList(), maxFiles: Int = 200): List<ProjectFileContext> {
        val basePath = project.basePath ?: return emptyList()
        return ProjectContextScanner.scan(basePath, exclusions, maxFiles)
    }
}
