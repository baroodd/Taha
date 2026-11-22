package com.example.model

enum class LandShape(val arabicName: String, val description: String) {
    RECTANGLE("أرض مستطيلة", "طول × عرض منتظم"),
    SQUARE("أرض مربعة", "أضلاع متساوية"),
    TRAPEZOID("أرض غير منتظمة (4 أضلاع)", "حساب متوسط الأضلاع المتقابلة")
}

enum class BuildingType(
    val arabicName: String,
    val subtitle: String,
    val defaultCoverageRatio: Double,
    val maxRecommendedFloors: Int,
    val structuralFactor: Double,
    val finishFactor: Double,
    val mepFactor: Double
) {
    HOUSE("منزل عائلي", "سكن عائلي مريح واقتصادي", 0.70, 3, 1.00, 1.00, 1.00),
    VILLA("فيلا حديثة", "تصميم رحب بحديقة وارتدادات", 0.58, 3, 1.08, 1.25, 1.15),
    PALACE("قصر فاخر", "مساحات ملكية وتشطيبات عالية", 0.50, 3, 1.22, 1.65, 1.40),
    RESIDENTIAL_BUILDING("عمارة سكنية", "شقق متعددة واستثمار عقاري", 0.75, 12, 1.14, 1.05, 1.12),
    HOSPITAL("مستشفى / مركز طبي", "عيادات وغرف عمليات وتجهيز طبي", 0.65, 8, 1.28, 1.45, 1.75),
    SCHOOL("مدرسة / مبنى تعليمي", "فصول دراسية وساحات وممرات واسعة", 0.60, 5, 1.16, 1.05, 1.20),
    COMMERCIAL_TOWER("برج تجاري / إداري", "مكاتب ومحلات وواجهات زجاجية", 0.78, 15, 1.25, 1.35, 1.45),
    MOSQUE("مسجد / مرفق عام", "قاعة صلاة واسعة وبحور إنشائية كبيرة", 0.72, 2, 1.20, 1.30, 1.15)
}

enum class ConstructionType(
    val arabicName: String,
    val subtitle: String,
    val cementKgPerSqm: Double,
    val steelKgPerSqm: Double,
    val sandM3PerSqm: Double,
    val gravelM3PerSqm: Double,
    val blocksPerSqm: Double,
    val concreteM3PerSqm: Double,
    val laborCostMultiplier: Double
) {
    POPULAR(
        arabicName = "بناء شعبي / حجر وطوب",
        subtitle = "جدران حاملة وأسقف بسيطة منخفضة التكلفة",
        cementKgPerSqm = 115.0,
        steelKgPerSqm = 14.0,
        sandM3PerSqm = 0.28,
        gravelM3PerSqm = 0.22,
        blocksPerSqm = 56.0,
        concreteM3PerSqm = 0.18,
        laborCostMultiplier = 0.72
    ),
    REINFORCED_TRADITIONAL(
        arabicName = "بناء مسلح تقليدي (أعمدة وجسور)",
        subtitle = "هيكل خرساني متين بأعمدة وجسور ساقطة",
        cementKgPerSqm = 185.0,
        steelKgPerSqm = 46.0,
        sandM3PerSqm = 0.36,
        gravelM3PerSqm = 0.44,
        blocksPerSqm = 48.0,
        concreteM3PerSqm = 0.34,
        laborCostMultiplier = 1.00
    ),
    MODERN_FLAT_SLAB(
        arabicName = "مسلح حديث (فلات سلاب / هوردي)",
        subtitle = "سقف مستوٍ دون جسور ساقطة لمرونة التوزيع",
        cementKgPerSqm = 215.0,
        steelKgPerSqm = 58.0,
        sandM3PerSqm = 0.40,
        gravelM3PerSqm = 0.50,
        blocksPerSqm = 44.0,
        concreteM3PerSqm = 0.40,
        laborCostMultiplier = 1.15
    ),
    STEEL_STRUCTURE(
        arabicName = "هيكلي حديدي ومسبق الصب",
        subtitle = "سرعة تنفيذ عالية وبحور واسعة",
        cementKgPerSqm = 95.0,
        steelKgPerSqm = 72.0,
        sandM3PerSqm = 0.18,
        gravelM3PerSqm = 0.24,
        blocksPerSqm = 28.0,
        concreteM3PerSqm = 0.16,
        laborCostMultiplier = 1.18
    ),
    INSULATED_LUXURY_STONE(
        arabicName = "مسلح معزول وحجر معماري",
        subtitle = "عزل حراري وصوتي متكامل وواجهات حجرية",
        cementKgPerSqm = 225.0,
        steelKgPerSqm = 55.0,
        sandM3PerSqm = 0.44,
        gravelM3PerSqm = 0.52,
        blocksPerSqm = 52.0,
        concreteM3PerSqm = 0.42,
        laborCostMultiplier = 1.30
    )
}

enum class CurrencyOption(
    val code: String,
    val arabicName: String,
    val symbol: String,
    val rateFromUsd: Double
) {
    SAR("SAR", "ريال سعودي", "ر.س", 3.75),
    USD("USD", "دولار أمريكي", "$", 1.0),
    YER("YER", "ريال يمني", "ر.ي", 530.0),
    AED("AED", "درهم إماراتي", "د.إ", 3.67),
    EGP("EGP", "جنيه مصري", "ج.م", 49.0),
    JOD("JOD", "دينار أردني", "د.أ", 0.71)
}

enum class MaterialCategory(val arabicName: String) {
    STRUCTURAL("الأعمال الإنشائية والعظم"),
    FINISHES("التشطيبات والأرضيات والطلاء"),
    MEP("الكهرباء والسباكة والعزل"),
    LABOR("العمالة والمصنعية")
}

data class MaterialTypeOption(
    val id: String,
    val name: String,
    val priceMultiplier: Double
)

data class MaterialItem(
    val id: String,
    val name: String,
    val category: MaterialCategory,
    val unit: String,
    val estimatedQuantity: Double,
    val customQuantity: Double? = null,
    val unitPrice: Double,
    val selectedGradeId: String,
    val availableGrades: List<MaterialTypeOption>,
    val isIncluded: Boolean = true,
    val notes: String = ""
) {
    val selectedGrade: MaterialTypeOption
        get() = availableGrades.firstOrNull { it.id == selectedGradeId }
            ?: availableGrades.firstOrNull()
            ?: MaterialTypeOption("std", "قياسي", 1.0)

    val effectiveQuantity: Double
        get() = customQuantity ?: estimatedQuantity

    val totalCost: Double
        get() = if (isIncluded) effectiveQuantity * unitPrice else 0.0
}

data class RoomZone(
    val label: String,
    val x: Float,
    val y: Float,
    val w: Float,
    val h: Float,
    val colorIndex: Int
)

data class ArchitecturalDesignOption(
    val id: String,
    val title: String,
    val styleTag: String,
    val summary: String,
    val coverageRatio: Double,
    val footprintAreaSqm: Double,
    val totalBuiltAreaSqm: Double,
    val roomsPerFloor: Int,
    val bathroomsPerFloor: Int,
    val setbackFrontM: Double,
    val setbackSidesM: Double,
    val structuralNotes: String,
    val isAiSample: Boolean,
    val zones: List<RoomZone>
)

data class LandScanResult(
    val photoUri: String? = null,
    val estimatedAreaSqm: Double = 400.0,
    val lengthM: Double = 20.0,
    val widthM: Double = 20.0,
    val northSideM: Double = 20.0,
    val southSideM: Double = 20.0,
    val eastSideM: Double = 20.0,
    val westSideM: Double = 20.0,
    val shape: LandShape = LandShape.RECTANGLE,
    val terrainNote: String = "أرض مستوية مناسبة للتأسيس المباشر",
    val confidenceNote: String = "أدخل أبعاد الأرض أو التقط صورة لمعايرة المساحة",
    val isSampleAiOutput: Boolean = false
)

data class EngineeringProject(
    val id: Long = 0L,
    val projectName: String = "مشروع بناء جديد",
    val landScan: LandScanResult = LandScanResult(),
    val buildingType: BuildingType = BuildingType.VILLA,
    val constructionType: ConstructionType = ConstructionType.REINFORCED_TRADITIONAL,
    val floors: Int = 2,
    val selectedDesignId: String = "",
    val designs: List<ArchitecturalDesignOption> = emptyList(),
    val designsAreAiSample: Boolean = false,
    val materials: List<MaterialItem> = emptyList(),
    val currency: CurrencyOption = CurrencyOption.SAR,
    val updatedAt: Long = System.currentTimeMillis()
) {
    val selectedDesign: ArchitecturalDesignOption?
        get() = designs.firstOrNull { it.id == selectedDesignId } ?: designs.firstOrNull()

    val totalCost: Double
        get() = materials.sumOf { it.totalCost }

    val builtAreaSqm: Double
        get() = selectedDesign?.totalBuiltAreaSqm
            ?: (landScan.estimatedAreaSqm * buildingType.defaultCoverageRatio * floors)

    val costPerSqm: Double
        get() = if (builtAreaSqm > 0.0) totalCost / builtAreaSqm else 0.0

    val costPerFloor: Double
        get() = if (floors > 0) totalCost / floors else totalCost
}
