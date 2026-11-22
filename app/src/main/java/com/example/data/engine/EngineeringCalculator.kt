package com.example.data.engine

import com.example.model.ArchitecturalDesignOption
import com.example.model.BuildingType
import com.example.model.ConstructionType
import com.example.model.CurrencyOption
import com.example.model.LandShape
import com.example.model.MaterialCategory
import com.example.model.MaterialItem
import com.example.model.MaterialTypeOption
import com.example.model.RoomZone
import kotlin.math.roundToInt
import kotlin.math.sqrt

object EngineeringCalculator {

    fun calculateLandArea(
        shape: LandShape,
        lengthM: Double,
        widthM: Double,
        northM: Double,
        southM: Double,
        eastM: Double,
        westM: Double
    ): Double {
        return when (shape) {
            LandShape.RECTANGLE -> (lengthM.coerceAtLeast(1.0) * widthM.coerceAtLeast(1.0))
            LandShape.SQUARE -> {
                val side = lengthM.coerceAtLeast(1.0)
                side * side
            }
            LandShape.TRAPEZOID -> {
                val avgWidth = (northM.coerceAtLeast(1.0) + southM.coerceAtLeast(1.0)) / 2.0
                val avgHeight = (eastM.coerceAtLeast(1.0) + westM.coerceAtLeast(1.0)) / 2.0
                avgWidth * avgHeight
            }
        }.roundToTwoDecimals()
    }

    fun calculatePerimeter(
        shape: LandShape,
        lengthM: Double,
        widthM: Double,
        northM: Double,
        southM: Double,
        eastM: Double,
        westM: Double
    ): Double {
        return when (shape) {
            LandShape.RECTANGLE -> 2.0 * (lengthM + widthM)
            LandShape.SQUARE -> 4.0 * lengthM
            LandShape.TRAPEZOID -> northM + southM + eastM + westM
        }.roundToTwoDecimals()
    }

    fun generateDesigns(
        landAreaSqm: Double,
        buildingType: BuildingType,
        constructionType: ConstructionType,
        floors: Int,
        isAiSample: Boolean = true
    ): List<ArchitecturalDesignOption> {
        val safeArea = landAreaSqm.coerceAtLeast(60.0)
        val safeFloors = floors.coerceIn(1, 20)
        val baseCoverage = buildingType.defaultCoverageRatio

        val specs = listOf(
            DesignSpec(
                id = "classic_balanced",
                title = "التصميم الكلاسيكي المتوازن",
                styleTag = "عملي • توزيع متناظر",
                coverageDelta = 0.0,
                setbackFront = 3.0,
                setbackSides = 2.0,
                summary = "توزيع هندسي متوازن يضمن الإضاءة والتهوية الطبيعية لكافة الفراغات مع استغلال مثالي لمحاور الأعمدة.",
                structuralNotes = "شبكة أعمدة منتظمة (${constructionType.arabicName}) بمسافات بحور 4.2م إلى 5.0م لتقليل الهدر في الحديد.",
                zones = zonesFor(buildingType, variant = 0)
            ),
            DesignSpec(
                id = "modern_open",
                title = "التصميم المودرن المفتوح",
                styleTag = "عصري • واجهات رحبة",
                coverageDelta = -0.05,
                setbackFront = 4.0,
                setbackSides = 2.2,
                summary = "فراغات معيشية مفتوحة مع بهو استقبال مضاعف الارتفاع وواجهات زجاجية تسمح بدخول الإضاءة الطبيعية.",
                structuralNotes = "يتناسب مع نظام ${constructionType.arabicName} مع تدعيم الكوابيل الأمامية للواجهات الحديثة.",
                zones = zonesFor(buildingType, variant = 1)
            ),
            DesignSpec(
                id = "smart_compact",
                title = "التصميم الاقتصادي الذكي",
                styleTag = "موفر للتكلفة • كفاءة عالية",
                coverageDelta = 0.05,
                setbackFront = 2.5,
                setbackSides = 1.5,
                summary = "تقليل الممرات الضائعة وتركيز نواة الخدمات (المطابخ والحمامات والدرج) لتخفيض تكلفة السباكة والهيكل بنسبة 12%.",
                structuralNotes = "توحيد مقاسات القواعد والميدات في ${constructionType.arabicName} لتسريع التنفيذ وخفض كلفة النجارة.",
                zones = zonesFor(buildingType, variant = 2)
            ),
            DesignSpec(
                id = "courtyard_luxury",
                title = "تصميم الفناء والخصوصية الفاخر",
                styleTag = "فناء داخلي • خصوصية تامة",
                coverageDelta = -0.08,
                setbackFront = 4.5,
                setbackSides = 2.5,
                summary = "مخطط يتمحور حول فناء داخلي أو حديقة وسطية مع فصل واضح بين أجنحة الضيوف والمناطق الخاصة.",
                structuralNotes = "توزيع أحمال متدرج لنظام ${constructionType.arabicName} مع عزل حراري مزدوج للواجهات الخارجية.",
                zones = zonesFor(buildingType, variant = 3)
            )
        )

        return specs.map { spec ->
            val cov = (baseCoverage + spec.coverageDelta).coerceIn(0.40, 0.85)
            val footprint = (safeArea * cov).roundToTwoDecimals()
            val totalBuilt = (footprint * safeFloors).roundToTwoDecimals()
            val roomsPerFloor = when (buildingType) {
                BuildingType.HOUSE -> (footprint / 32.0).roundToInt().coerceIn(3, 8)
                BuildingType.VILLA -> (footprint / 38.0).roundToInt().coerceIn(4, 10)
                BuildingType.PALACE -> (footprint / 45.0).roundToInt().coerceIn(6, 16)
                BuildingType.RESIDENTIAL_BUILDING -> (footprint / 28.0).roundToInt().coerceIn(6, 14)
                BuildingType.HOSPITAL -> (footprint / 26.0).roundToInt().coerceIn(6, 18)
                BuildingType.SCHOOL -> (footprint / 42.0).roundToInt().coerceIn(4, 12)
                BuildingType.COMMERCIAL_TOWER -> (footprint / 35.0).roundToInt().coerceIn(4, 14)
                BuildingType.MOSQUE -> 3
            }
            val bathsPerFloor = when (buildingType) {
                BuildingType.MOSQUE -> (footprint / 90.0).roundToInt().coerceIn(2, 8)
                BuildingType.HOSPITAL -> (roomsPerFloor * 0.8).roundToInt().coerceIn(3, 12)
                else -> (roomsPerFloor * 0.5).roundToInt().coerceIn(2, 8)
            }
            ArchitecturalDesignOption(
                id = spec.id,
                title = spec.title,
                styleTag = spec.styleTag,
                summary = spec.summary,
                coverageRatio = cov,
                footprintAreaSqm = footprint,
                totalBuiltAreaSqm = totalBuilt,
                roomsPerFloor = roomsPerFloor,
                bathroomsPerFloor = bathsPerFloor,
                setbackFrontM = spec.setbackFront,
                setbackSidesM = spec.setbackSides,
                structuralNotes = spec.structuralNotes,
                isAiSample = isAiSample,
                zones = spec.zones
            )
        }
    }

    fun calculateMaterials(
        totalBuiltAreaSqm: Double,
        footprintAreaSqm: Double,
        floors: Int,
        buildingType: BuildingType,
        constructionType: ConstructionType,
        currency: CurrencyOption,
        customDefaultPricesUsd: Map<String, Double> = emptyMap(),
        existingItems: List<MaterialItem> = emptyList()
    ): List<MaterialItem> {
        val area = totalBuiltAreaSqm.coerceAtLeast(40.0)
        val sFactor = buildingType.structuralFactor * (1.0 + (floors - 1).coerceAtLeast(0) * 0.025)
        val fFactor = buildingType.finishFactor
        val mFactor = buildingType.mepFactor

        val cementTons = ((area * constructionType.cementKgPerSqm * sFactor) / 1000.0).roundToOneDecimal()
        val steelTons = ((area * constructionType.steelKgPerSqm * sFactor) / 1000.0).roundToOneDecimal()
        val sandM3 = (area * constructionType.sandM3PerSqm * sFactor).roundToOneDecimal()
        val gravelM3 = (area * constructionType.gravelM3PerSqm * sFactor).roundToOneDecimal()
        val blocksCount = (area * constructionType.blocksPerSqm).roundToInt().toDouble()
        val concreteM3 = (area * constructionType.concreteM3PerSqm * sFactor).roundToOneDecimal()
        val tilesSqm = (area * 1.08 * fFactor.coerceAtMost(1.25)).roundToOneDecimal()
        val paintBuckets = ((area * 2.7) / 14.0).roundToOneDecimal()
        val timberM3 = (footprintAreaSqm * 0.045 * floors.coerceAtLeast(1)).roundToOneDecimal()
        val mepPoints = ((area / 6.5) * mFactor).roundToInt().toDouble()
        val doorsWindowsUnits = ((area / 22.0) * fFactor.coerceAtMost(1.3)).roundToInt().toDouble()
        val laborSqm = area.roundToOneDecimal()

        fun unitPrice(id: String, baseUsd: Double): Double {
            val usd = customDefaultPricesUsd[id] ?: baseUsd
            return (usd * currency.rateFromUsd).roundToTwoDecimals()
        }

        val templates = listOf(
            MaterialTemplate(
                id = "cement",
                name = "الأسمنت",
                category = MaterialCategory.STRUCTURAL,
                unit = "طن",
                qty = cementTons,
                baseUsd = 95.0,
                notes = "ما يعادل ${(cementTons * 20).roundToInt()} كيس (50 كجم)",
                grades = listOf(
                    MaterialTypeOption("opc", "بورتلاندي عادي", 1.0),
                    MaterialTypeOption("src", "مقاوم للأملاح والكبريتات", 1.12),
                    MaterialTypeOption("white", "أسمنت أبيض للديكور", 1.45)
                )
            ),
            MaterialTemplate(
                id = "steel",
                name = "حديد التسليح",
                category = MaterialCategory.STRUCTURAL,
                unit = "طن",
                qty = steelTons,
                baseUsd = 720.0,
                notes = "يشمل القواعد والأعمدة والأسقف (أقطار 10–18 ملم)",
                grades = listOf(
                    MaterialTypeOption("std_60", "تسليح قياسي Grade 60", 1.0),
                    MaterialTypeOption("high_tensile", "عالي المقاومة مقاوم للزلازل", 1.10),
                    MaterialTypeOption("mesh", "حديد تسليح + شبك لحام", 1.06)
                )
            ),
            MaterialTemplate(
                id = "sand",
                name = "الرمل (البطحاء)",
                category = MaterialCategory.STRUCTURAL,
                unit = "م³",
                qty = sandM3,
                baseUsd = 18.0,
                notes = "للخلطات الخرسانية وأعمال اللياسة والدفان",
                grades = listOf(
                    MaterialTypeOption("washed", "رمل مغسول للخرسانة", 1.0),
                    MaterialTypeOption("fine_plaster", "رمل ناعم للياسة", 1.15)
                )
            ),
            MaterialTemplate(
                id = "gravel",
                name = "الحصى / الكري (السن)",
                category = MaterialCategory.STRUCTURAL,
                unit = "م³",
                qty = gravelM3,
                baseUsd = 22.0,
                notes = "حصى متدرج للخرسانة المسلحة والفرشات",
                grades = listOf(
                    MaterialTypeOption("crushed", "كري كسارات متدرج 3/4", 1.0),
                    MaterialTypeOption("basalt", "حصى بازلت عالي الصلابة", 1.22)
                )
            ),
            MaterialTemplate(
                id = "blocks",
                name = "الطوب / البلوك",
                category = MaterialCategory.STRUCTURAL,
                unit = "حبة",
                qty = blocksCount,
                baseUsd = 0.65,
                notes = "للجدران الخارجية والقواطع الداخلية",
                grades = listOf(
                    MaterialTypeOption("hollow", "بلوك أسمنتي مفرغ 20 سم", 1.0),
                    MaterialTypeOption("insulated", "بلوك معزول حرارياً", 1.45),
                    MaterialTypeOption("red_brick", "طوب أحمر فخاري", 1.25)
                )
            ),
            MaterialTemplate(
                id = "concrete_mix",
                name = "الخرسانة والمياه والصب",
                category = MaterialCategory.STRUCTURAL,
                unit = "م³",
                qty = concreteM3,
                baseUsd = 58.0,
                notes = "تجهيز وصب الخرسانة الجاهزة أو الموقع",
                grades = listOf(
                    MaterialTypeOption("c30", "خرسانة إجهاد C30", 1.0),
                    MaterialTypeOption("c35", "خرسانة عالية الإجهاد C35", 1.15),
                    MaterialTypeOption("c40", "خرسانة مقاومة C40 للأبراج", 1.28)
                )
            ),
            MaterialTemplate(
                id = "timber",
                name = "الأخشاب والشدات",
                category = MaterialCategory.STRUCTURAL,
                unit = "م³",
                qty = timberM3,
                baseUsd = 310.0,
                notes = "أخشاب القوالب والدعائم (نسبة استهلاك دورية)",
                grades = listOf(
                    MaterialTypeOption("plywood", "بليود ومعاوض خشبية", 1.0),
                    MaterialTypeOption("steel_form", "شدات معدنية مستأجرة", 1.20)
                )
            ),
            MaterialTemplate(
                id = "tiles",
                name = "البلاط والأرضيات",
                category = MaterialCategory.FINISHES,
                unit = "م²",
                qty = tilesSqm,
                baseUsd = 16.0,
                notes = "شامل أرضيات الغرف والحمامات ونسبة هدر 8%",
                grades = listOf(
                    MaterialTypeOption("ceramic", "سيراميك محلي / قياسي", 1.0),
                    MaterialTypeOption("porcelain", "بورسلين نخب أول", 1.55),
                    MaterialTypeOption("marble", "رخام وجرانيت طبيعي", 2.80)
                )
            ),
            MaterialTemplate(
                id = "paint",
                name = "الطلاء والمعجون",
                category = MaterialCategory.FINISHES,
                unit = "دلو 18ل",
                qty = paintBuckets,
                baseUsd = 42.0,
                notes = "وجه أساس + طبقتان معجون + وجهان دهان نهائي",
                grades = listOf(
                    MaterialTypeOption("plastic", "دهان بلاستيك مائي داخلي", 1.0),
                    MaterialTypeOption("washable", "دهان قابل للغسل مقاوم للرطوبة", 1.32),
                    MaterialTypeOption("textured", "رشة خارجية / جرافيتو", 1.50)
                )
            ),
            MaterialTemplate(
                id = "doors_windows",
                name = "الأبواب والنوافذ",
                category = MaterialCategory.FINISHES,
                unit = "وحدة",
                qty = doorsWindowsUnits,
                baseUsd = 195.0,
                notes = "أبواب خشبية/حديدية ونوافذ ألمنيوم دبل جلاس",
                grades = listOf(
                    MaterialTypeOption("std_alu", "ألمنيوم قياسي وأبواب قشرة", 1.0),
                    MaterialTypeOption("thermal_upvc", "نوافذ UPVC عازلة وأبواب صلب", 1.45),
                    MaterialTypeOption("luxury_wood", "خشب صلب فاخر وزجاج سيكوريت", 1.95)
                )
            ),
            MaterialTemplate(
                id = "mep",
                name = "الكهرباء والسباكة والعزل",
                category = MaterialCategory.MEP,
                unit = "نقطة",
                qty = mepPoints,
                baseUsd = 38.0,
                notes = "تمديدات كهربائية، مواسير تغذية وصرف، وعزل مائي",
                grades = listOf(
                    MaterialTypeOption("std_mep", "تمديدات معتمدة قياسية", 1.0),
                    MaterialTypeOption("smart_mep", "تمديدات ذكية وعزل حراري ومائي", 1.48)
                )
            ),
            MaterialTemplate(
                id = "labor",
                name = "أجور العمالة والمصنعية",
                category = MaterialCategory.LABOR,
                unit = "م²",
                qty = laborSqm,
                baseUsd = 68.0 * constructionType.laborCostMultiplier,
                notes = "مقاولات العظم والتشطيب حسب نظام البناء",
                grades = listOf(
                    MaterialTypeOption("labor_std", "مقاولة عظم وتشطيب قياسي", 1.0),
                    MaterialTypeOption("labor_turnkey", "تسليم مفتاح بإشراف هندسي", 1.28)
                )
            )
        )

        val existingMap = existingItems.associateBy { it.id }
        return templates.map { t ->
            val prev = existingMap[t.id]
            val selectedGradeId = prev?.selectedGradeId
                ?.takeIf { gId -> t.grades.any { it.id == gId } }
                ?: t.grades.first().id
            val gradeMultiplier = t.grades.first { it.id == selectedGradeId }.priceMultiplier
            val defaultPrice = (unitPrice(t.id, t.baseUsd) * gradeMultiplier).roundToTwoDecimals()
            MaterialItem(
                id = t.id,
                name = t.name,
                category = t.category,
                unit = t.unit,
                estimatedQuantity = t.qty,
                customQuantity = prev?.customQuantity,
                unitPrice = prev?.unitPrice ?: defaultPrice,
                selectedGradeId = selectedGradeId,
                availableGrades = t.grades,
                isIncluded = prev?.isIncluded ?: true,
                notes = t.notes
            )
        }
    }

    fun formatNumber(value: Double): String {
        val rounded = (value * 100.0).roundToInt() / 100.0
        return if (rounded % 1.0 == 0.0) {
            String.format(java.util.Locale.US, "%,.0f", rounded)
        } else {
            String.format(java.util.Locale.US, "%,.2f", rounded)
        }
    }

    private fun zonesFor(type: BuildingType, variant: Int): List<RoomZone> {
        return when (type) {
            BuildingType.HOSPITAL -> when (variant) {
                0 -> listOf(
                    RoomZone("الاستقبال والطوارئ", 0.04f, 0.04f, 0.56f, 0.28f, 0),
                    RoomZone("العيادات", 0.64f, 0.04f, 0.32f, 0.44f, 1),
                    RoomZone("ممر رئيسي", 0.04f, 0.35f, 0.56f, 0.13f, 3),
                    RoomZone("غرف الرعاية", 0.04f, 0.52f, 0.44f, 0.44f, 2),
                    RoomZone("التعقيم والمختبر", 0.52f, 0.52f, 0.44f, 0.44f, 1)
                )
                else -> listOf(
                    RoomZone("ردهة مركزية", 0.30f, 0.04f, 0.40f, 0.92f, 3),
                    RoomZone("جناح عيادات", 0.04f, 0.04f, 0.23f, 0.60f, 0),
                    RoomZone("عمليات وأشعة", 0.73f, 0.04f, 0.23f, 0.58f, 1),
                    RoomZone("صيدلية", 0.04f, 0.68f, 0.23f, 0.28f, 2),
                    RoomZone("مصاعد وخدمة", 0.73f, 0.66f, 0.23f, 0.30f, 2)
                )
            }
            BuildingType.SCHOOL -> listOf(
                RoomZone("فصل دراسي 1", 0.04f, 0.04f, 0.42f, 0.28f, 0),
                RoomZone("فصل دراسي 2", 0.54f, 0.04f, 0.42f, 0.28f, 0),
                RoomZone("ممر الحركة", 0.04f, 0.36f, 0.92f, 0.16f, 3),
                RoomZone("مختبر ومكتبة", 0.04f, 0.56f, 0.54f, 0.40f, 1),
                RoomZone("إدارة وخدمات", 0.62f, 0.56f, 0.34f, 0.40f, 2)
            )
            BuildingType.MOSQUE -> listOf(
                RoomZone("صحن الصلاة الرئيسي", 0.05f, 0.05f, 0.90f, 0.62f, 0),
                RoomZone("المحراب والمنبر", 0.36f, 0.02f, 0.28f, 0.10f, 1),
                RoomZone("رواق ومدخل", 0.05f, 0.71f, 0.56f, 0.24f, 3),
                RoomZone("مواضئ وخدمات", 0.65f, 0.71f, 0.30f, 0.24f, 2)
            )
            BuildingType.COMMERCIAL_TOWER -> listOf(
                RoomZone("البهو والمصاعد", 0.34f, 0.32f, 0.32f, 0.36f, 3),
                RoomZone("مكتب / معرض A", 0.04f, 0.04f, 0.44f, 0.42f, 0),
                RoomZone("مكتب / معرض B", 0.52f, 0.04f, 0.44f, 0.42f, 1),
                RoomZone("مكتب / معرض C", 0.04f, 0.52f, 0.44f, 0.44f, 1),
                RoomZone("مكتب / معرض D", 0.52f, 0.52f, 0.44f, 0.44f, 2)
            )
            BuildingType.RESIDENTIAL_BUILDING -> listOf(
                RoomZone("شقة 1 - صالة وغرف", 0.04f, 0.04f, 0.40f, 0.92f, 0),
                RoomZone("الدرج والمصعد", 0.46f, 0.04f, 0.08f, 0.92f, 3),
                RoomZone("شقة 2 - صالة وغرف", 0.56f, 0.04f, 0.40f, 0.92f, 1)
            )
            else -> when (variant) {
                0 -> listOf(
                    RoomZone("مجلس الضيوف", 0.04f, 0.04f, 0.44f, 0.42f, 0),
                    RoomZone("صالة المعيشة", 0.52f, 0.04f, 0.44f, 0.42f, 1),
                    RoomZone("المطبخ والخدمات", 0.04f, 0.50f, 0.34f, 0.46f, 2),
                    RoomZone("نواة الدرج", 0.41f, 0.50f, 0.18f, 0.46f, 3),
                    RoomZone("جناح النوم", 0.62f, 0.50f, 0.34f, 0.46f, 0)
                )
                1 -> listOf(
                    RoomZone("صالة مفتوحة بانورامية", 0.04f, 0.04f, 0.92f, 0.40f, 1),
                    RoomZone("مطبخ أمريكي", 0.04f, 0.48f, 0.32f, 0.48f, 2),
                    RoomZone("جناح رئيسي", 0.40f, 0.48f, 0.36f, 0.48f, 0),
                    RoomZone("درج وحمامات", 0.79f, 0.48f, 0.17f, 0.48f, 3)
                )
                2 -> listOf(
                    RoomZone("غرفة معيشة", 0.04f, 0.04f, 0.56f, 0.44f, 0),
                    RoomZone("المطبخ والحمامات", 0.64f, 0.04f, 0.32f, 0.92f, 2),
                    RoomZone("غرفة نوم 1", 0.04f, 0.52f, 0.26f, 0.44f, 1),
                    RoomZone("غرفة نوم 2", 0.34f, 0.52f, 0.26f, 0.44f, 1)
                )
                else -> listOf(
                    RoomZone("جناح الاستقبال", 0.04f, 0.04f, 0.92f, 0.28f, 0),
                    RoomZone("فناء وحديقة وسطية", 0.30f, 0.36f, 0.40f, 0.30f, 3),
                    RoomZone("جناح العائلة", 0.04f, 0.36f, 0.23f, 0.60f, 1),
                    RoomZone("الأجنحة الخاصة", 0.73f, 0.36f, 0.23f, 0.60f, 1),
                    RoomZone("مطبخ وخدمات", 0.30f, 0.70f, 0.40f, 0.26f, 2)
                )
            }
        }
    }

    fun estimateDimensionsFromArea(areaSqm: Double): Pair<Double, Double> {
        val side = sqrt(areaSqm.coerceAtLeast(25.0))
        return side.roundToOneDecimal() to side.roundToOneDecimal()
    }

    private fun Double.roundToTwoDecimals(): Double = (this * 100.0).roundToInt() / 100.0
    private fun Double.roundToOneDecimal(): Double = (this * 10.0).roundToInt() / 10.0
}

private data class DesignSpec(
    val id: String,
    val title: String,
    val styleTag: String,
    val coverageDelta: Double,
    val setbackFront: Double,
    val setbackSides: Double,
    val summary: String,
    val structuralNotes: String,
    val zones: List<RoomZone>
)

private data class MaterialTemplate(
    val id: String,
    val name: String,
    val category: MaterialCategory,
    val unit: String,
    val qty: Double,
    val baseUsd: Double,
    val notes: String,
    val grades: List<MaterialTypeOption>
)
