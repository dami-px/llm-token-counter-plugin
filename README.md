# Token Counter — IntelliJ plugin

Status-bar widget that shows an LLM token estimate for the current selection (or the active file when nothing is selected). Uses [JTokkit](https://github.com/knuddelsgmbh/jtokkit) with the `o200k_base` encoding (GPT-4o / o-series).

## Install from disk

1. Build the plugin:
   ```bash
   ./gradlew buildPlugin
   ```
2. In your IDE, go to **Settings** (Cmd+, / Ctrl+Alt+S) → **Plugins** → click the **gear icon** (top-right) → **Install Plugin from Disk...**
3. Select `build/distributions/token-counter-plugin-0.1.0.zip`
4. Restart the IDE when prompted.

The widget appears in the bottom status bar.

## Development

```bash
./gradlew runIde
```

First run downloads IntelliJ Community 2024.3 (~1 GB) and launches a sandboxed IDE with the plugin installed. Useful for iterating without touching your main IDE.

## Display

- No selection → `file: 12.3k tok`
- Selection active → `sel: 487 tok`
- `~` prefix when counting >50k chars (debounced, but worth flagging)

## Switching encoding

Edit `Tokenizer.kt` — swap `EncodingType.O200K_BASE` for `CL100K_BASE` (GPT-4 / 3.5) or `R50K_BASE` (GPT-3). No Python required.

## Marketplace publishing

1. Get an upload token from [plugins.jetbrains.com](https://plugins.jetbrains.com) (Profile → My Tokens).
2. Add `intellijPublishToken=your_token_here` to `gradle.properties` (don't commit this).
3. Run:
   ```bash
   ./gradlew publishPlugin
   ```

To include screenshots on the marketplace listing, add them inside a `<change-notes>` or use the JetBrains plugin portal UI after the first upload — the portal lets you upload screenshots directly when editing the plugin page.

## Compatibility

- IntelliJ-based IDEs build 243+ (2024.3 and newer)
- IntelliJ IDEA, WebStorm, PyCharm, GoLand, Rider, etc.

## Files

- `TokenCountWidgetFactory.kt` — registers widget with the platform
- `TokenCountWidget.kt` — status-bar UI + selection/editor listeners + debounce
- `Tokenizer.kt` — JTokkit wrapper
- `plugin.xml` — extension point registration
- `pluginIcon.svg` / `pluginIcon_dark.svg` — marketplace and IDE icons
