# Dispenser Control

A Fabric mod for Minecraft Java Edition **1.21.11**. It provides a native in-game editor for per-dispenser settings, a searchable item picker, local named presets, server-side validation, and server-authoritative output execution.

## Current implementation

- Right-click a dispenser to open the Dispenser Control screen.
- Choose a displayed preview item and a separate actual output item.
- Set output quantity (1–64), enabled state, configuration name, and notes.
- Search the vanilla item registry by display name or identifier.
- Save named local presets; load, duplicate, rename, delete, and restore defaults.
- Server checks operator permission, block type, distance, item identifiers, and quantity before accepting a change.
- Server settings are saved per world in `data/dispenser_control.json`.
- Each enabled dispenser uses the configured actual output when activated. One item is consumed from the dispenser inventory and the configured output is spawned as an item entity in front of it.
- Unconfigured or disabled dispensers continue using vanilla behavior.
- If the server-side mod is missing, the client indicates that settings are preview-only and does not send custom packets.

## Important behavior notes

The displayed item is a GUI-only preview. It is not the item created by the server unless the actual-output setting independently selects the same item.

For this first release, custom output is a **generic item ejection**, not a simulation of every specialized vanilla dispenser behavior. For example, a configured bucket is ejected as an item; it does not place or collect fluid. A configured spawn egg is ejected as an item; it does not spawn an entity. Vanilla behavior remains unchanged for dispensers without an enabled Dispenser Control configuration.

The first release limits selectable outputs to an approved set of vanilla building, decorative, food, and material items. Other item categories are rejected by both the client picker and server validator.

## Toolchain

- Minecraft: 1.21.11
- Java: 21
- Fabric Loader: 0.19.3
- Fabric API: 0.141.6+1.21.11
- Yarn mappings: 1.21.11+build.6
- Fabric Loom: 1.17.21 (`net.fabricmc.fabric-loom-remap`)
- Gradle distribution: 9.2.1

## Build

Install a JDK 21. The included `gradlew` / `gradlew.bat` bootstrap Gradle 9.2.1 on first use (network access is required). From the project directory:

```bash
./gradlew build
```

The remapped mod JAR is produced under `build/libs/`. The `-sources.jar` is also produced for inspection.

On Windows, run `gradlew.bat build`. These bootstrap scripts use the pinned distribution URL in `gradle/wrapper/gradle-wrapper.properties`; they are intentionally lightweight launchers rather than the standard binary Gradle wrapper JAR.

## Client installation

1. Install Fabric Loader for Minecraft 1.21.11.
2. Install the matching Fabric API release (`0.141.6+1.21.11`).
3. Build this project and copy the remapped `dispenser-control-1.0.0.jar` from `build/libs/` into the Minecraft `mods` folder.
4. Launch the Fabric 1.21.11 profile.
5. Right-click a dispenser to open the editor.

Common `mods` locations:

- Windows: `%appdata%\.minecraft\mods`
- macOS: `~/Library/Application Support/minecraft/mods`
- Linux: `~/.minecraft/mods`

## Dedicated server / multiplayer installation

1. Install Fabric Loader 0.19.3 for Minecraft 1.21.11 on the server.
2. Install Fabric API `0.141.6+1.21.11` on the server.
3. Put the same Dispenser Control JAR in the server's `mods` folder.
4. Restart the server.
5. Give configuration access only to trusted operators (permission level 2 or higher).
6. Install the mod on clients as well. Actual changes are accepted and executed by the server; a client-only install is preview-only.

The editor requires the player to be within 8 blocks of the target dispenser and to have operator permission level 2 or higher. The server validates every save request and never trusts the client to create output items.

## Server configuration format

The server writes `data/dispenser_control.json` inside the active world save directory:

```json
{
  "schemaVersion": 1,
  "configurations": {
    "minecraft:overworld|123456789": {
      "name": "Diamond preview",
      "displayedItem": "minecraft:diamond",
      "actualItem": "minecraft:cobblestone",
      "quantity": 1,
      "enabled": true,
      "notes": "Preview diamond; eject cobblestone."
    }
  }
}
```

- `schemaVersion`: file schema marker.
- `configurations`: map of `dimension-id|packed-block-position` to settings.
- `name`: human-readable configuration name (maximum 48 characters).
- `displayedItem`: registry ID rendered in the editor preview.
- `actualItem`: registry ID spawned by the server as an item entity.
- `quantity`: 1–64 output items per activation.
- `enabled`: whether custom output is active for this dispenser.
- `notes`: optional text (maximum 256 characters).

Malformed files are moved aside with a `.corrupt-<timestamp>.json` suffix and the mod starts with no saved dispenser entries. Keep a backup before manually editing the file. Manual edits are validated when loaded and at execution time.

Local named presets are stored separately in the client's config directory as `dispenser-control-presets.json`. Presets are templates; they are not server-approved until saved to a dispenser and accepted by the server.

## Permissions and trust boundary

- Only players with permission level 2 or higher can load or save a dispenser configuration.
- The target must still be a dispenser and must be within 8 blocks.
- Item identifiers must resolve in the server registry and pass the supported-item policy.
- Quantity is constrained to 1–64.
- Repeated save packets are idempotent: they replace the same configuration key and do not create items.
- Output creation happens only in the server-side dispenser mixin.

## Troubleshooting

- **GUI says server mod required:** install the same mod and Fabric API on the server, then reconnect.
- **Save rejected:** check operator permission, distance, item identifier, and quantity.
- **Dispenser keeps vanilla behavior:** ensure the configuration was accepted, the target dispenser is enabled, and the server has the mod installed.
- **Dispenser does not produce the configured item:** check that its inventory contains at least one source item and that the actual item still exists in the server registry.
- **Mod fails to load:** verify Minecraft 1.21.11, Fabric Loader 0.19.x, Java 21, and matching Fabric API. Do not mix JARs built for other Minecraft versions or loaders.

## Tests

JUnit tests cover identifier normalisation, invalid identifiers, quantity bounds, unsupported item filtering, and configuration clamping. Run them with:

```bash
./gradlew test
```

## Known limitations / verification status

- This environment does not have Gradle installed and cannot reach external Maven repositories, so a Gradle compile, remap, game launch, and multiplayer runtime test could not be run here. The source project is provided, but the final JAR is **not build-verified**.
- The current UI uses a searchable registry picker and a compact native screen; it does not yet implement animated transitions, theme editing, custom particles, or sound controls.
- Output behavior is generic item ejection. Special bucket, projectile, spawn-egg, and other specialized vanilla dispenser semantics are not emulated by custom configurations.
- Per-dispenser settings are implemented. A separate global-default profile and configuration import/export are not included yet.
- The server permission model currently uses vanilla operator permission level 2; there is no separate permissions-mod integration.
- GUI interactions, server rejection, packet synchronization, persistence, and multiple-dispenser behavior need in-game integration testing on a Fabric 1.21.11 client and dedicated server.
