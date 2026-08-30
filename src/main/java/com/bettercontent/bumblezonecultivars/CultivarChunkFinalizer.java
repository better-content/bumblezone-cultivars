package com.bettercontent.bumblezonecultivars;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
        removeForeignNaturalCultivars(level, chunk, dimension);
        if (dimension.equals(BUMBLEZONE)) placeNurseries(level, chunk);
        finalized.add(chunkKey);
    }

    private static void removeForeignNaturalCultivars(ServerLevel level, LevelChunk chunk, ResourceLocation dimension) {
        int minX = chunk.getPos().getMinBlockX(), minZ = chunk.getPos().getMinBlockZ();
        for (int x = minX; x <= minX + 15; x++) for (int z = minZ; z <= minZ + 15; z++) for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++) {
            BlockPos pos = new BlockPos(x, y, z);
            ResourceLocation id = ForgeRegistries.BLOCKS.getKey(level.getBlockState(pos).getBlock());
            CultivarDefinition cultivar = id == null ? null : CultivarCatalog.byPlant(id.toString());
            if (cultivar != null && !cultivar.originDimensions().contains(dimension.toString())) level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        }
    }

    private static void placeNurseries(ServerLevel level, LevelChunk chunk) {
        List<CultivarDefinition> choices = CultivarCatalog.ALL.stream().filter(c -> c.originDimensions().contains(BUMBLEZONE.toString()) && CultivarCatalog.resolves(c)).toList();
        if (choices.isEmpty()) return;
        int placed = 0, minX = chunk.getPos().getMinBlockX(), minZ = chunk.getPos().getMinBlockZ();
        for (int x = minX; x <= minX + 15 && placed < 24; x++) for (int z = minZ; z <= minZ + 15 && placed < 24; z++) for (int y = level.getMaxBuildHeight() - 2; y >= level.getMinBuildHeight() && placed < 24; y--) {
            BlockPos pollenPos = new BlockPos(x, y, z), hostPos = pollenPos.above();
            ResourceLocation below = ForgeRegistries.BLOCKS.getKey(level.getBlockState(pollenPos).getBlock());
            if (!POLLEN.equals(below) || !level.getBlockState(hostPos).isAir()) continue;
            CultivarDefinition chosen = choices.get(level.random.nextInt(choices.size()));
            level.setBlock(hostPos, BumblezoneCultivars.LIVING_POLLEN_NURSERY.get().defaultBlockState(), 2);
            if (level.getBlockEntity(hostPos) instanceof LivingPollenNurseryBlockEntity nursery) nursery.setSeedId(chosen.seedItem());
            placed++;
        }
    }
}
