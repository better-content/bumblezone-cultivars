package com.bettercontent.bumblezonecultivars;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public final class CultivarChunkFinalizer {
    private static final ResourceLocation BUMBLEZONE = new ResourceLocation("the_bumblezone", "the_bumblezone");
    private static final ResourceLocation POLLEN = new ResourceLocation("the_bumblezone", "pile_of_pollen");
    private CultivarChunkFinalizer() {}

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getChunk() instanceof LevelChunk chunk)) return;
        // Fresh worlds only: inhabited chunks are never rewritten or backfilled.
        if (chunk.getInhabitedTime() != 0L) return;
        CultivarFinalizationData finalized = CultivarFinalizationData.get(level);
        long chunkKey = chunk.getPos().toLong();
        if (finalized.contains(chunkKey)) return;
        ResourceLocation dimension = level.dimension().location();
        removeForeignNaturalCultivars(chunk, dimension, level.getMinBuildHeight(), level.getMaxBuildHeight());
        if (dimension.equals(BUMBLEZONE)) {
            placeNurseries(chunk, level.random, level.getMinBuildHeight(), level.getMaxBuildHeight());
        }
        finalized.add(chunkKey);
    }

    private static void removeForeignNaturalCultivars(LevelChunk chunk, ResourceLocation dimension, int minBuildHeight, int maxBuildHeight) {
        int minX = chunk.getPos().getMinBlockX(), minZ = chunk.getPos().getMinBlockZ();
        for (int x = minX; x <= minX + 15; x++) for (int z = minZ; z <= minZ + 15; z++) for (int y = minBuildHeight; y < maxBuildHeight; y++) {
            BlockPos pos = new BlockPos(x, y, z);
            ResourceLocation id = ForgeRegistries.BLOCKS.getKey(chunk.getBlockState(pos).getBlock());
            CultivarDefinition cultivar = id == null ? null : CultivarCatalog.byPlant(id.toString());
            if (cultivar != null && !cultivar.originDimensions().contains(dimension.toString())) {
                chunk.setBlockState(pos, Blocks.AIR.defaultBlockState(), false);
            }
        }
    }

    private static void placeNurseries(LevelChunk chunk, RandomSource random, int minBuildHeight, int maxBuildHeight) {
        List<CultivarDefinition> choices = CultivarCatalog.ALL.stream().filter(c -> c.originDimensions().contains(BUMBLEZONE.toString()) && CultivarCatalog.resolves(c)).toList();
        if (choices.isEmpty()) return;
        int placed = 0, minX = chunk.getPos().getMinBlockX(), minZ = chunk.getPos().getMinBlockZ();
        for (int x = minX; x <= minX + 15 && placed < 24; x++) for (int z = minZ; z <= minZ + 15 && placed < 24; z++) for (int y = maxBuildHeight - 2; y >= minBuildHeight && placed < 24; y--) {
            BlockPos pollenPos = new BlockPos(x, y, z), hostPos = pollenPos.above();
            ResourceLocation below = ForgeRegistries.BLOCKS.getKey(chunk.getBlockState(pollenPos).getBlock());
            if (!POLLEN.equals(below) || !chunk.getBlockState(hostPos).isAir()) continue;
            CultivarDefinition chosen = choices.get(random.nextInt(choices.size()));
            chunk.setBlockState(hostPos, BumblezoneCultivars.LIVING_POLLEN_NURSERY.get().defaultBlockState(), false);
            if (chunk.getBlockEntity(hostPos) instanceof LivingPollenNurseryBlockEntity nursery) nursery.setSeedId(chosen.seedItem());
            placed++;
        }
    }
}
