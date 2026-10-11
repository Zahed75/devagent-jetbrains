# DevAgent — Your AI Coding Workspace

DevAgent is a JetBrains IDE plugin that gives you a project-aware AI coding workspace inside IntelliJ IDEA, PyCharm, WebStorm, and other compatible IDEs. It is designed to support Kimi Code as the primary provider while keeping a clean abstraction for Claude, OpenAI-compatible providers, and future integrations.

This repository contains a conservative foundation build of the DevAgent JetBrains plugin. It includes a project-aware tool window, local project-file scanning, persistent application settings, secure API-key storage, and provider interfaces. Provider request execution and conversation persistence are not implemented; the UI reports this rather than returning fabricated responses.

## Highlights

- JetBrains IDE tool window named `DevAgent`
- Native Swing UI using IntelliJ Platform components
- Modular provider abstraction (`AIProvider`, `ProviderType`, `ModelDescriptor`)
- Kimi CLI path discovery without invoking unverified commands
- OpenAI-compatible and Claude provider scaffolding that explicitly does not send requests
- Local project context scanning with configurable exclusions and bounded text-file filtering
- Persistent application settings with JetBrains `PersistentStateComponent`
- API credentials stored through the JetBrains PasswordSafe service

## Minimum supported IDE

The project targets IntelliJ Platform 2024.1 (`since-build` 241) through 2025.1 (`until-build` 251.*), using JDK 17 and Kotlin 1.9.24.

## Project structure

```text
.
├── build.gradle.kts
├── gradle.properties
├── settings.gradle.kts
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── com/zahed75/devagent/
│   │   │       ├── context/
│   │   │       ├── providers/
│   │   │       ├── settings/
│   │   │       ├── DevAgentToolWindow.kt
│   │   │       └── DevAgentToolWindowFactory.kt
│   │   └── resources/
│   │       ├── META-INF/plugin.xml
│   │       └── messages/DevAgentBundle.properties
│   └── test/
│       └── kotlin/com/zahed75/devagent/context/
├── LICENSE
├── README.md
└── .gitignore
```

## Getting started

### 1. Install prerequisites

- JDK 17+
- Gradle 9.0+ (the IntelliJ Platform Gradle Plugin 2.19.0)
- IntelliJ IDEA / PyCharm / WebStorm with the IntelliJ Platform SDK (for local plugin testing)

### 2. Build the plugin

```bash
gradle test
gradle verifyPlugin
gradle buildPlugin
```

The distributable ZIP is written to `build/distributions/`. Gradle downloads the target IntelliJ Platform SDK on the first build, so access to JetBrains' SDK download service is required.

### 3. Install the plugin locally

In a compatible IDE, open **Settings/Preferences | Plugins**, choose **Install Plugin from Disk**, and select the ZIP in `build/distributions/`. Restart the IDE and open **View | Tool Windows | DevAgent**.

### 4. Run in IDE sandbox

From IntelliJ IDEA:

1. Open the project.
2. Use the Gradle task `runIde`.
3. When the sandbox IDE opens, open the `DevAgent` tool window from the right-side tool window bar.

## Configuration and current limitations

Open **Settings/Preferences | Tools | DevAgent** to select a provider, set its model identifier and endpoint, configure the Kimi executable path, and set project scan exclusions. API keys are masked in the settings UI and stored with JetBrains PasswordSafe rather than in `devagent-settings.xml`.

The tool window can scan a project locally and display text-file metadata. File contents are inspected locally only to count lines; they are not included in requests or uploaded. Chat drafts are temporary; no conversation history is persisted. The send action deliberately returns a clear “not implemented” result for every provider.

### Kimi Code

The plugin can locate a configured Kimi executable, but does not invoke it. The installed CLI and its supported non-interactive request protocol could not be verified in this environment. Do not assume a Kimi CLI subscription provides API access. A real adapter needs a verified CLI contract and local validation before request execution can be added.

### Claude and OpenAI-compatible providers

These are architecture stubs only. The plugin makes no HTTP requests, does not test credentials remotely, and does not claim that a configured endpoint is connected. Claude requires an API key and compatible endpoint implementation; OpenAI-compatible services require a base URL, model identifier, and provider-specific testing.

## Security and privacy

- No prompt or project content is sent to a provider by this foundation build.
- API keys are stored with JetBrains PasswordSafe and are not serialized into ordinary plugin settings.
- Project scanning is local, skips common generated/cache directories and symlinked files, and only lists known text-file types up to 1 MB each.
- A real provider adapter must add explicit disclosure, bounded requests, and secret/exclusion handling before sending context externally.

## Not yet implemented

- Real Kimi CLI, Claude API, or OpenAI-compatible request execution and streaming
- Persistent conversations, search, rename, export, or project memory
- Reading selected file contents, editor selection, attachments, and citations
- Agent tools, patch previews, code application, and approved command execution
- Provider connection tests

## Roadmap

- Verify Kimi CLI request/session protocol and implement a cancellable adapter
- Add real provider transports, connection diagnostics, and streaming where supported
- Add explicit file-content context, attachments, and privacy controls
- Add persistent conversations and a permission-gated agent/edit loop

## License

This project is released under the MIT license. See the `LICENSE` file for details.
