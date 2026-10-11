package com.zahed75.devagent

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity

class DevAgentStartup : StartupActivity.DumbAware {
    override fun runActivity(project: Project) {
        // Startup initialization hook for later milestones.
    }
}
