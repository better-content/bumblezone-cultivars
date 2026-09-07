package com.bettercontent.bumblezonecultivars;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CultivarPropagationPolicyTest {
    @Test void immaturePlantsYieldOneSeedInEveryDimension() {
        assertEquals(1, CultivarLootModifier.seedCount(false, false, 0, 0.0F));
        assertEquals(1, CultivarLootModifier.seedCount(false, true, 2, 0.0F));
    }

    @Test void matureOriginPlantsKeepTheTwoToFourSeedRange() {
        assertEquals(2, CultivarLootModifier.seedCount(true, true, 0, 1.0F));
        assertEquals(4, CultivarLootModifier.seedCount(true, true, 2, 1.0F));
    }

    @Test void matureForeignPlantsUseTenPercentSecondSeedBoundary() {
        assertEquals(2, CultivarLootModifier.seedCount(true, false, 0, 0.099999F));
        assertEquals(1, CultivarLootModifier.seedCount(true, false, 0, 0.10F));
        assertEquals(1, CultivarLootModifier.seedCount(true, false, 0, 0.999999F));
    }
}
