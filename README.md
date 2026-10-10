# Sol Client

A Fabric client mod for **Minecraft 1.21.11**: a NanoVG ClickGUI, HUD editor, themes and modules.

> Based on **67Client**, modified and rebranded with the original author's permission.

![ClickGUI preview](docs/clickgui-preview.png)

*(This preview is a mockup drawn with the same sizes, colours and font as the code. It's not a real screenshot.)*

## The new UI

- **Top bar** with the Sol logo, **Modules / Configs** tabs, and a search box. Start typing anywhere to search.
- **Category windows** with solid accent title bars and compact 20px module rows. Enabled modules get an accent bar and highlight. The title bar shows how many modules are on. Click a title to collapse it, drag it to move it.
- **Settings window**: right-click a module to open its settings in a separate draggable window. It has a description, an enable switch, and a scrollable list of settings. Close it with Esc, the ✕, or by clicking outside.
- **Restyled controls**: square checkboxes, thin sliders with a square handle, `‹ mode ›` selectors (left-click goes forward, right-click goes back), and flat text fields and key boxes.
- **Themes**: Sol (default orange), Ember, Nebula, Ocean, Mint, Frost and Rainbow, plus custom accent colours.
- **HUD**: new watermark (sun logo + "Sol Client" + version) and flatter notifications.

## Building

```bash
./gradlew build          # output: build/libs/sol-client-1.0.0.jar
```

Requires JDK 21. The sources came from a decompiled jar, so they use **intermediary** names
(`class_310`, `method_1551`, …). `build.gradle` uses intermediary as its mappings so they compile as-is.
The first build may still turn up a few decompiler artifacts that need hand-fixing.

## Layout

```
src/main/java/dev/sol/          mod source (package dev.sol, entrypoint SolClient)
src/main/resources/             fabric.mod.json, solclient.mixins.json, assets/solclient/
```

## Known gaps

- **Sound files are missing.** The `.ogg` files referenced in `assets/solclient/sounds.json` weren't in the decompiled dump. Add them under `assets/solclient/sounds/ui/` and `assets/solclient/sounds/startup/`, or UI sounds will be silent.
- **AimAssist's logic is missing.** The original shipped it as an encrypted class (`dev.sol.secured.AimAssistLogic`). That class isn't in the dump, so the module loads but does nothing.
