package com.bettercontent.bumblezonecultivars;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.registries.ForgeRegistries;

public final class CultivarSeedItem extends Item {
    private final ResourceLocation plant;
    public CultivarSeedItem(String plant) { super(new Properties()); this.plant = new ResourceLocation(plant); }

    @Override public InteractionResult useOn(UseOnContext context) {
        Block block = ForgeRegistries.BLOCKS.getValue(plant);
        if (block == null) return InteractionResult.FAIL;
        var pos = context.getClickedPos().relative(context.getClickedFace());
        BlockState state = block.defaultBlockState();
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            for (var direction : net.minecraft.core.Direction.Plane.HORIZONTAL) {
                BlockState candidate = state.setValue(BlockStateProperties.HORIZONTAL_FACING, direction);
                if (candidate.canSurvive(context.getLevel(), pos)) {
                    state = candidate;
                    break;
                }
            }
        }
        if (!context.getLevel().getBlockState(pos).canBeReplaced() || !state.canSurvive(context.getLevel(), pos)) return InteractionResult.FAIL;
        if (!context.getLevel().isClientSide()) {
            context.getLevel().setBlock(pos, state, 3);
            if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) context.getItemInHand().shrink(1);
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
    }
}
