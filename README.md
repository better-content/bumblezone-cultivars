# Bumblezone Cultivars

Better Content-owned Forge mod that makes Bumblezone the acquisition origin for cultivatable food flora while preserving portable Overworld farming.

Requires Java 17, Minecraft 1.20.1, and Forge 47.4.13. Build from this repository with its own checked-in Gradle wrapper:

```sh
./gradlew --no-daemon verifyFull stageRuntimeJar
```

The reobfuscated runtime artifact is `build/libs/bumblezone-cultivars-0.1.0.jar`. Local verification does not authorize deployment or pack tests. Generated build, cache, and runtime data stay untracked.

Licensed under GPL-3.0-or-later; see [LICENSE](LICENSE). Contribution and validation requirements are in [AGENTS.md](AGENTS.md).

Imported-cultivar discovery evidence records Bumblezone seed origin on the harvested seed stack, then saves the planter and planting identity with the Overworld crop position. Only successfully spawned produce from that mature crop emits `CultivarHarvestEvent`; loot previews, disabled drops, untagged seeds, and repeated delivery cannot award it. This currently covers the native block-drop harvest path, including automatic block harvesting; right-click-only harvest adapters do not fabricate an equivalent outcome.

`verifyFull` includes a Forge GameTest using the pinned Bumblezone 7.13.4 dependency and vanilla wheat. It checks actual produce spawning, loot-preview rejection, disabled drops, author persistence, and duplicate rejection. Bumblezone's inline SRG mixin targets need an official-name development adaptation: `scripts/remap-test-mixin-strings.py` writes only `build/development-dependencies/bumblezone-mapped.jar`. The upstream dependency and deployable Cultivars JAR remain separate and unchanged by that development remap.
