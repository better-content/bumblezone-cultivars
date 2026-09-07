package com.bettercontent.bumblezonecultivars;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SeedVisualContractTest {
    private static final Set<String> DEDICATED_SEEDS = Set.of(
        "minecraft_carrot_seeds", "minecraft_potato_seeds", "minecraft_sweet_berry_seeds",
        "minecraft_glow_berry_seeds", "minecraft_brown_mushroom_spores", "minecraft_red_mushroom_spores",
        "minecraft_kelp_spores", "minecraft_cocoa_cutting", "aether_berry_bush_seeds",
        "farmersrespite_coffee_seeds", "farmersdelight_onion_seeds", "farmersdelight_rice_seeds",
        "ubesdelight_garlic_seeds", "ubesdelight_ginger_seeds", "ubesdelight_ube_seeds",
        "natures_spirit_shiitake_spores", "minecraft_nether_wart_spores", "minecraft_sugar_cane_cutting",
        "minecraft_cactus_cutting", "minecraft_bamboo_shoots", "ars_nouveau_sourceberry_seeds",
        "hexerei_belladonna_seeds", "hexerei_mandrake_seeds", "hexerei_mugwort_seeds",
        "hexerei_yellow_dock_seeds", "natures_spirit_green_bearberry_seeds",
        "natures_spirit_purple_bearberry_seeds", "natures_spirit_red_bearberry_seeds",
        "twilightforest_torchberry_seeds", "goety_firethorn_seeds");

    @Test void tintManifestCoversEveryDedicatedSeedWithDistinctReadableColors() throws Exception {
        JsonObject tints = objectResource("assets/bumblezone_cultivars/seed_tints.json");
        assertEquals(DEDICATED_SEEDS, tints.keySet());
        Set<Integer> colors = new HashSet<>();
        tints.entrySet().forEach(entry -> {
            String id = entry.getKey();
            var value = entry.getValue();
            String hex = value.getAsString();
            assertTrue(hex.matches("#[0-9A-F]{6}"), id + " has invalid tint " + hex);
            assertEquals(Integer.parseInt(hex.substring(1), 16), SeedVisuals.color(id));
            assertTrue(colors.add(SeedVisuals.color(id)), id + " duplicates another tint");
        });
        assertEquals(0xFFFFFF, SeedVisuals.color("missing_seed"));
    }

    @Test void everySeedModelUsesTheNeutralTintableTexture() throws Exception {
        for (String id : DEDICATED_SEEDS) {
            JsonObject model = objectResource("assets/bumblezone_cultivars/models/item/" + id + ".json");
            assertEquals("bumblezone_cultivars:item/cultivar_seed",
                model.getAsJsonObject("textures").get("layer0").getAsString(), id);
        }
        try (var stream = getClass().getClassLoader().getResourceAsStream(
            "assets/bumblezone_cultivars/textures/item/cultivar_seed.png")) {
            assertNotNull(stream);
            var image = ImageIO.read(stream);
            assertEquals(16, image.getWidth());
            assertEquals(16, image.getHeight());
            boolean hasTransparent = false;
            boolean hasVisible = false;
            for (int y = 0; y < image.getHeight(); y++) for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                int alpha = argb >>> 24;
                hasTransparent |= alpha == 0;
                hasVisible |= alpha > 0;
                if (alpha > 0) {
                    int red = argb >> 16 & 0xFF;
                    int green = argb >> 8 & 0xFF;
                    int blue = argb & 0xFF;
                    assertEquals(red, green, "neutral texture must tint without hue bias");
                    assertEquals(green, blue, "neutral texture must tint without hue bias");
                }
            }
            assertTrue(hasTransparent);
            assertTrue(hasVisible);
        }
    }

    private JsonObject objectResource(String path) throws Exception {
        try (var stream = getClass().getClassLoader().getResourceAsStream(path)) {
            assertNotNull(stream, path);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }
}
