# DevAgent — Your AI Coding Workspace

DevAgent is a JetBrains IDE plugin that gives you a project-aware AI coding workspace inside IntelliJ IDEA, PyCharm, WebStorm, and other compatible IDEs. It is designed to support Kimi Code as the primary provider while keeping a clean abstraction for Claude, OpenAI-compatible providers, and future integrations.

This repository starts with the foundation milestone: a working JetBrains plugin skeleton, a tool window, provider abstractions, and configuration surfaces. The project is intentionally structured so later milestones can add richer project context, persistent conversation storage, tool execution, patch approval, and secure credential storage.

## Highlights

- JetBrains IDE tool window named `DevAgent`
- Native Swing UI using IntelliJ Platform components
- Modular provider abstraction (`AIProvider`, `ProviderType`, `ModelDescriptor`)
- Kimi Code CLI readiness and configuration support
- OpenAI-compatible provider shell for future API integrations
- Claude-ready provider abstraction
- Local project context scanning with exclusion support
- Persistent application settings with JetBrains `PersistentStateComponent`
- Security-first design with clear limitations and no hard-coded credentials

## Minimum supported IDE

The plugin declares `since-build` 241 (2024.1) and `until-build` 263.* (up to 2026.3), configured in `patchPluginXml` in `build.gradle.kts`. Update `untilBuild` when newer IDE builds are verified. It is designed to load into IntelliJ IDEA, PyCharm, or WebStorm with the compatible platform version installed.

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
- Gradle 8.10.x via the bundled wrapper (`./gradlew`); do not use Gradle 9+, which is incompatible with the `org.jetbrains.intellij` 1.x plugin
- IntelliJ IDEA / PyCharm / WebStorm with the IntelliJ Platform SDK (for local plugin testing)

### 2. Build the plugin

```bash
./gradlew buildPlugin
```

### 3. Run in IDE sandbox

From IntelliJ IDEA:

1. Open the project.
2. Use the Gradle task `runIde`.
3. When the sandbox IDE opens, open the `DevAgent` tool window from the right-side tool window bar.

## Kimi Code configuration

This repository contains a provider abstraction for Kimi Code, but the actual CLI integration must be validated against the user's installed Kimi executable and published documentation. The implementation is intentionally conservative and only uses a documented, low-risk detection strategy:

- check if a `kimi` executable is installed and accessible
- allow explicit configuration of the CLI path
- validate the binary via `--version` / `--help` if supported
- treat unsupported runtime interfaces as a configuration blocker rather than a fake success

If your local Kimi CLI exposes a different command model, you should configure the path and override the provider behavior in the `DevAgent` settings.

## Security notes

- No API keys are stored in the repository.
- Credentials should be stored via JetBrains secure storage or via the OS keychain in the final implementation.
- The project intentionally avoids secret leakage and warns against unsafe command execution.

## Known limitations in this foundation build

This is the initial milestone and intentionally focuses on the plugin skeleton and architecture rather than full provider execution.

- Real Kimi Code request/stream execution is not assumed without a verified interface.
- Conversation persistence is not yet a full multi-project database layer.
- Code-editing workflows and project memory are planned for later milestones.
- The plugin has a working foundation but still requires provider-specific validation on the user machine.

## Roadmap

- Milestone 1: Foundation (current)
- Milestone 2: Provider integration and Kimi CLI adapter validation
- Milestone 3: Project context and attachments
- Milestone 4: Persistent conversation history
- Milestone 5: Agentic coding workflows, diff review, and patch approval
- Milestone 6: Security and final UX polish
- Milestone 7: plugin packaging and release verification

## License

This project is released under the MIT license. See the `LICENSE` file for details.
