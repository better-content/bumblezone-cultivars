package com.bettercontent.bumblezonecultivars;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class CultivarCatalog {
    public static final List<CultivarDefinition> ALL = load();
    private CultivarCatalog() {}

    private static List<CultivarDefinition> load() {
        var stream = CultivarCatalog.class.getResourceAsStream("/defaults/cultivars.json");
        if (stream == null) throw new IllegalStateException("Missing fixed cultivar catalog");
        return new Gson().fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), new TypeToken<List<CultivarDefinition>>() {}.getType());
    }

    public static CultivarDefinition bySeed(String id) { return ALL.stream().filter(c -> c.seedItem().equals(id)).findFirst().orElse(null); }
    public static CultivarDefinition byPlant(String id) { return ALL.stream().filter(c -> c.plantBlocks().contains(id)).findFirst().orElse(null); }
    public static boolean resolves(CultivarDefinition c) {
        return ForgeRegistries.ITEMS.containsKey(new ResourceLocation(c.seedItem())) && c.plantBlocks().stream().allMatch(id -> ForgeRegistries.BLOCKS.containsKey(new ResourceLocation(id)));
    }
}
