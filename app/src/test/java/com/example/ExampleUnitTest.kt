package com.example

import com.example.data.engine.EngineeringCalculator
import com.example.model.BuildingType
import com.example.model.ConstructionType
import com.example.model.CurrencyOption
import com.example.model.LandShape
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun landAreaCalculation_handlesRectangleAndTrapezoid() {
        val rectArea = EngineeringCalculator.calculateLandArea(
            shape = LandShape.RECTANGLE,
            lengthM = 25.0,
            widthM = 20.0,
            northM = 0.0,
            southM = 0.0,
            eastM = 0.0,
            westM = 0.0
        )
        assertEquals(500.0, rectArea, 0.01)

        val trapArea = EngineeringCalculator.calculateLandArea(
            shape = LandShape.TRAPEZOID,
            lengthM = 0.0,
            widthM = 0.0,
            northM = 20.0,
            southM = 24.0,
            eastM = 15.0,
            westM = 17.0
        )
        assertEquals(352.0, trapArea, 0.01)
    }

    @Test
    fun designAndMaterialCalculation_producesNonZeroCostsAndAllCoreMaterials() {
        val designs = EngineeringCalculator.generateDesigns(
            landAreaSqm = 400.0,
            buildingType = BuildingType.VILLA,
            constructionType = ConstructionType.REINFORCED_TRADITIONAL,
            floors = 2
        )
        assertEquals(4, designs.size)
        val first = designs.first()
        assertTrue(first.totalBuiltAreaSqm > 0.0)

        val materials = EngineeringCalculator.calculateMaterials(
            totalBuiltAreaSqm = first.totalBuiltAreaSqm,
            footprintAreaSqm = first.footprintAreaSqm,
            floors = 2,
            buildingType = BuildingType.VILLA,
            constructionType = ConstructionType.REINFORCED_TRADITIONAL,
            currency = CurrencyOption.SAR
        )
        val ids = materials.map { it.id }.toSet()
        assertTrue(ids.containsAll(listOf("cement", "steel", "sand", "gravel", "blocks", "tiles", "paint", "labor")))
        assertTrue(materials.sumOf { it.totalCost } > 0.0)
    }
}
