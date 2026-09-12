package com.bettercontent.bumblezonecultivars;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CultivarPropagationPolicyTest {
    @Test void immaturePlantsYieldOneSeedInEveryDimension() {
        assertEquals(1, CultivarLootModifier.seedCount(false, false, false, 0, 0.0F));
        assertEquals(1, CultivarLootModifier.seedCount(false, true, false, 2, 0.0F));
    }

    @Test void matureOriginPlantsKeepTheTwoToFourSeedRange() {
        assertEquals(2, CultivarLootModifier.seedCount(true, true, false, 0, 1.0F));
        assertEquals(4, CultivarLootModifier.seedCount(true, true, false, 2, 1.0F));
        assertEquals(2, CultivarLootModifier.seedCount(true, true, true, 0, 1.0F));
    }

    @Test void matureForeignPlantsUseTenPercentSecondSeedBoundary() {
        assertEquals(2, CultivarLootModifier.seedCount(true, false, false, 0, 0.099999F));
        assertEquals(1, CultivarLootModifier.seedCount(true, false, false, 0, 0.10F));
        assertEquals(1, CultivarLootModifier.seedCount(true, false, false, 0, 0.999999F));
    }

    @Test void persistentHarvestPlantsNeverCreateSeedsOutsideTheirOrigin() {
        assertEquals(0, CultivarLootModifier.seedCount(false, false, true, 0, 0.0F));
        assertEquals(0, CultivarLootModifier.seedCount(true, false, true, 0, 0.0F));
    }
}
