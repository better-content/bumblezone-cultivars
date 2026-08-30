package com.bettercontent.bumblezonecultivars;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public final class CultivarLootModifier extends LootModifier {
    public static final Codec<CultivarLootModifier> CODEC = RecordCodecBuilder.create(instance -> codecStart(instance).apply(instance, CultivarLootModifier::new));
    public CultivarLootModifier(LootItemCondition[] conditions) { super(conditions); }

    @Override protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
        if (state == null) return loot;
        ResourceLocation plantId = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        CultivarDefinition cultivar = plantId == null ? null : CultivarCatalog.byPlant(plantId.toString());
        if (cultivar == null) return loot;
        var seed = ForgeRegistries.ITEMS.getValue(new ResourceLocation(cultivar.seedItem()));
        if (seed == null) return loot;
        loot.removeIf(stack -> stack.is(seed));
        boolean mature = isMature(state, cultivar.maturityRule());
        boolean inOrigin = cultivar.originDimensions().contains(context.getLevel().dimension().location().toString());
        int count = !mature ? 1 : inOrigin ? 2 + context.getRandom().nextInt(3) : 1 + (context.getRandom().nextFloat() < 0.01F ? 1 : 0);
        loot.add(new ItemStack(seed, count));
        return loot;
    }

    static boolean isMature(BlockState state, String rule) {
        if (rule.equals("always-mature") || rule.equals("fruit-block") || rule.equals("top-segment")) return true;
        for (var property : state.getProperties()) {
            if (property instanceof IntegerProperty integer && property.getName().equals("age")) {
                return state.getValue(integer) >= integer.getPossibleValues().stream().mapToInt(Integer::intValue).max().orElse(0);
            }
            if (property instanceof BooleanProperty booleanProperty && property.getName().equals("berries")) {
                return state.getValue(booleanProperty);
            }
        }
        return !rule.contains("immature");
    }

    @Override public Codec<? extends IGlobalLootModifier> codec() { return CODEC; }
}
