package com.example.data.ai

import android.graphics.Bitmap
import com.example.data.engine.EngineeringCalculator
import com.example.model.ArchitecturalDesignOption
import com.example.model.BuildingType
import com.example.model.ConstructionType
import com.example.model.LandScanResult
import com.example.model.LandShape
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

interface AiRepository {
    suspend fun analyzeLandPhoto(
        bitmap: Bitmap?,
        photoUri: String?,
        referenceLengthM: Double,
        referenceWidthM: Double,
        shape: LandShape
    ): LandScanResult

    suspend fun generateArchitecturalDesigns(
        areaSqm: Double,
        buildingType: BuildingType,
        constructionType: ConstructionType,
        floors: Int
    ): Pair<List<ArchitecturalDesignOption>, Boolean>
}

/**
 * Placeholder implementation used when Firebase AI Logic is not enabled.
 * Provides clearly labeled sample engineering analysis derived from image aspect ratio
 * and user reference dimensions without direct Gemini REST calls or embedded keys.
 */
class PlaceholderAiRepository : AiRepository {

    override suspend fun analyzeLandPhoto(
        bitmap: Bitmap?,
        photoUri: String?,
        referenceLengthM: Double,
        referenceWidthM: Double,
        shape: LandShape
    ): LandScanResult {
        delay(550L)
        val safeLength = referenceLengthM.coerceAtLeast(5.0)
        val safeWidth = if (bitmap != null && bitmap.height > 0 && referenceWidthM <= 0.0) {
            val aspect = bitmap.width.toDouble() / bitmap.height.toDouble()
            ((safeLength / aspect) * 10.0).roundToInt() / 10.0
        } else {
            referenceWidthM.coerceAtLeast(5.0)
        }

        val area = EngineeringCalculator.calculateLandArea(
            shape = shape,
            lengthM = safeLength,
            widthM = safeWidth,
            northM = safeLength,
            southM = safeLength,
            eastM = safeWidth,
            westM = safeWidth
        )

        val terrainNote = if (bitmap != null) {
            "نتيجة معايرة هندسية تجريبية بناءً على نسبة أبعاد الصورة (${bitmap.width}×${bitmap.height}) والضلع المرجعي."
        } else {
            "نتيجة حساب هندسي تجريبي للأبعاد المدخلة — التقط أو اختر صورة للأرض للمعايرة البصرية."
        }

        return LandScanResult(
            photoUri = photoUri,
            estimatedAreaSqm = area,
            lengthM = safeLength,
            widthM = safeWidth,
            northSideM = safeLength,
            southSideM = safeLength,
            eastSideM = safeWidth,
            westSideM = safeWidth,
            shape = shape,
            terrainNote = terrainNote,
            confidenceNote = "تحليل تجريبي (يتطلب تفعيل Firebase AI Logic للتحليل الجغرافي المباشر بالذكاء الاصطناعي)",
            isSampleAiOutput = true
        )
    }

    override suspend fun generateArchitecturalDesigns(
        areaSqm: Double,
        buildingType: BuildingType,
        constructionType: ConstructionType,
        floors: Int
    ): Pair<List<ArchitecturalDesignOption>, Boolean> {
        delay(450L)
        val designs = EngineeringCalculator.generateDesigns(
            landAreaSqm = areaSqm,
            buildingType = buildingType,
            constructionType = constructionType,
            floors = floors,
            isAiSample = true
        )
        return designs to true
    }
}
