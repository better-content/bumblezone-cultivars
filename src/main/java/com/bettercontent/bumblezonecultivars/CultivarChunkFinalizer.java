package com.bettercontent.bumblezonecultivars;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
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
        // This is worldgen finalization, never a scan or backfill of an ordinary loaded chunk.
        if (!event.isNewChunk()) return;
        CultivarFinalizationData finalized = CultivarFinalizationData.get(level);
        long chunkKey = chunk.getPos().toLong();
        if (finalized.contains(chunkKey)) return;
        ResourceLocation dimension = level.dimension().location();
        removeForeignNaturalCultivars(chunk, dimension);
        if (dimension.equals(BUMBLEZONE)) {
            placeNurseries(chunk, level.random, level.getMinBuildHeight(), level.getMaxBuildHeight());
        }
        finalized.add(chunkKey);
    }

    private static void removeForeignNaturalCultivars(LevelChunk chunk, ResourceLocation dimension) {
        int minX = chunk.getPos().getMinBlockX(), minZ = chunk.getPos().getMinBlockZ();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        LevelChunkSection[] sections = chunk.getSections();
        for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
            LevelChunkSection section = sections[sectionIndex];
            if (section == null || section.hasOnlyAir()) continue;
            int baseY = SectionPos.sectionToBlockCoord(chunk.getSectionYFromSectionIndex(sectionIndex));
            for (int localY = 0; localY < 16; localY++) for (int localZ = 0; localZ < 16; localZ++) for (int localX = 0; localX < 16; localX++) {
                var state = section.getBlockState(localX, localY, localZ);
                var block = state.getBlock();
                if (block == Blocks.AIR) continue;
                CultivarDefinition cultivar = CultivarCatalog.byPlant(block);
                if (cultivar != null && !cultivar.originDimensions().contains(dimension.toString())) {
                    cursor.set(minX + localX, baseY + localY, minZ + localZ);
                    // Aquatic plants occupy a water block. Preserve that fluid when
                    // removing a foreign natural cultivar, otherwise worldgen leaves
                    // kelp-shaped columns of air in the ocean.
                    var fluidState = state.getFluidState();
                    // This runs from ChunkEvent.Load while the chunk is still being
                    // promoted. Mutating through LevelChunk would fire block-place and
                    // fluid-interaction callbacks that synchronously request this chunk
                    // again. Update the section storage directly for this worldgen-only
                    // cleanup; no neighbor/update callbacks are needed here.
                    section.setBlockState(localX, localY, localZ, fluidState.isEmpty()
                        ? Blocks.AIR.defaultBlockState()
                        : fluidState.createLegacyBlock(), false);
                }
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
