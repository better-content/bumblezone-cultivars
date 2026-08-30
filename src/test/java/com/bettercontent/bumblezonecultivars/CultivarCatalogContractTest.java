package com.bettercontent.bumblezonecultivars;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class CultivarCatalogContractTest {
    @Test void everyCultivarHasThePublicContractAndDistinctPropagule() throws Exception {
        try (var stream = getClass().getClassLoader().getResourceAsStream("defaults/cultivars.json")) {
            assertNotNull(stream);
            JsonArray entries = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonArray();
            assertTrue(entries.size() >= 40, "catalog must cover the live pack's principal food and magical flora");
            var ids = new HashSet<String>();
            for (var element : entries) {
                var cultivar = element.getAsJsonObject();
                String id = cultivar.get("id").getAsString();
                assertTrue(ids.add(id), "duplicate cultivar " + id);
                for (String field : new String[]{"seedItem", "plantBlocks", "produce", "growthForm", "maturityRule", "placementAdapter", "propagationAnchor", "originDimensions"}) {
                    assertTrue(cultivar.has(field), id + " lacks " + field);
                }
                String seed = cultivar.get("seedItem").getAsString();
                for (var produce : cultivar.getAsJsonArray("produce")) {
                    assertNotEquals(produce.getAsString(), seed, id + " plants edible produce directly");
                }
                assertFalse(cultivar.getAsJsonArray("plantBlocks").isEmpty());
                assertFalse(cultivar.getAsJsonArray("originDimensions").isEmpty());
            }
        }
    }
}
