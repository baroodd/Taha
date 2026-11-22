package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiRepository
import com.example.data.ai.PlaceholderAiRepository
import com.example.data.engine.EngineeringCalculator
import com.example.data.local.AppDatabase
import com.example.data.local.PreferencesRepository
import com.example.data.local.ProjectRepository
import com.example.model.BuildingType
import com.example.model.ConstructionType
import com.example.model.CurrencyOption
import com.example.model.EngineeringProject
import com.example.model.LandShape
import com.example.model.MaterialCategory
import com.example.model.MaterialItem
import com.example.model.MaterialTypeOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

enum class MainTab(val arabicTitle: String) {
    HOME("الرئيسية"),
    LAND_SCAN("مسح الأرض"),
    DESIGNS("التصاميم"),
    MATERIALS("المواد والتكلفة")
}

enum class DrawerDestination {
    MAIN_WORKFLOW,
    DEFAULT_PRICES,
    QUICK_CALCULATOR,
    ABOUT_DESIGNER
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val projectRepository = ProjectRepository(database.projectDao())
    private val preferencesRepository = PreferencesRepository(application)
    private val aiRepository: AiRepository = PlaceholderAiRepository()

    val savedProjects: StateFlow<List<EngineeringProject>> = projectRepository.savedProjects
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedTab = MutableStateFlow(MainTab.HOME)
    val selectedTab: StateFlow<MainTab> = _selectedTab.asStateFlow()

    private val _drawerDestination = MutableStateFlow(DrawerDestination.MAIN_WORKFLOW)
    val drawerDestination: StateFlow<DrawerDestination> = _drawerDestination.asStateFlow()

    private val _isAnalyzingLand = MutableStateFlow(false)
    val isAnalyzingLand: StateFlow<Boolean> = _isAnalyzingLand.asStateFlow()

    private val _isGeneratingDesigns = MutableStateFlow(false)
    val isGeneratingDesigns: StateFlow<Boolean> = _isGeneratingDesigns.asStateFlow()

    private val _customDefaultPricesUsd = MutableStateFlow<Map<String, Double>>(emptyMap())
    val customDefaultPricesUsd: StateFlow<Map<String, Double>> = _customDefaultPricesUsd.asStateFlow()

    private val _snackbarEvents = MutableSharedFlow<String>()
    val snackbarEvents = _snackbarEvents.asSharedFlow()

    private val _project = MutableStateFlow(createInitialProject(CurrencyOption.SAR))
    val project: StateFlow<EngineeringProject> = _project.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.preferredCurrency.collect { savedCurrency ->
                _project.update { current ->
                    if (current.currency == savedCurrency) current
                    else recalculateMaterialsForCurrency(current, savedCurrency)
                }
            }
        }
    }

    private fun createInitialProject(currency: CurrencyOption): EngineeringProject {
        val landArea = 400.0
        val buildingType = BuildingType.VILLA
        val constructionType = ConstructionType.REINFORCED_TRADITIONAL
        val floors = 2
        val designs = EngineeringCalculator.generateDesigns(
            landAreaSqm = landArea,
            buildingType = buildingType,
            constructionType = constructionType,
            floors = floors,
            isAiSample = false
        )
        val selected = designs.first()
        val materials = EngineeringCalculator.calculateMaterials(
            totalBuiltAreaSqm = selected.totalBuiltAreaSqm,
            footprintAreaSqm = selected.footprintAreaSqm,
            floors = floors,
            buildingType = buildingType,
            constructionType = constructionType,
            currency = currency
        )
        return EngineeringProject(
            projectName = "فيلا سكنية - مشروع جديد",
            buildingType = buildingType,
            constructionType = constructionType,
            floors = floors,
            selectedDesignId = selected.id,
            designs = designs,
            designsAreAiSample = false,
            materials = materials,
            currency = currency
        )
    }

    fun selectTab(tab: MainTab) {
        _drawerDestination.value = DrawerDestination.MAIN_WORKFLOW
        _selectedTab.value = tab
    }

    fun openDrawerDestination(destination: DrawerDestination) {
        _drawerDestination.value = destination
    }

    fun startNewProject() {
        val currentCurrency = _project.value.currency
        _project.value = createInitialProject(currentCurrency)
        _drawerDestination.value = DrawerDestination.MAIN_WORKFLOW
        _selectedTab.value = MainTab.LAND_SCAN
    }

    fun updateProjectName(name: String) {
        _project.update { it.copy(projectName = name) }
    }

    fun updateLandDimensions(
        shape: LandShape? = null,
        lengthM: Double? = null,
        widthM: Double? = null,
        northM: Double? = null,
        southM: Double? = null,
        eastM: Double? = null,
        westM: Double? = null
    ) {
        _project.update { current ->
            val old = current.landScan
            val nextShape = shape ?: old.shape
            val l = (lengthM ?: old.lengthM).coerceIn(3.0, 1000.0)
            val w = if (nextShape == LandShape.SQUARE) l else (widthM ?: old.widthM).coerceIn(3.0, 1000.0)
            val n = (northM ?: old.northSideM).coerceIn(3.0, 1000.0)
            val s = (southM ?: old.southSideM).coerceIn(3.0, 1000.0)
            val e = (eastM ?: old.eastSideM).coerceIn(3.0, 1000.0)
            val wst = (westM ?: old.westSideM).coerceIn(3.0, 1000.0)
            val area = EngineeringCalculator.calculateLandArea(nextShape, l, w, n, s, e, wst)
            val updatedLand = old.copy(
                shape = nextShape,
                lengthM = l,
                widthM = w,
                northSideM = n,
                southSideM = s,
                eastSideM = e,
                westSideM = wst,
                estimatedAreaSqm = area
            )
            refreshDesignsAndMaterials(current.copy(landScan = updatedLand))
        }
    }

    fun onLandPhotoSelected(uri: Uri) {
        viewModelScope.launch {
            _isAnalyzingLand.value = true
            try {
                val bitmap = withContext(Dispatchers.IO) { decodeBitmap(uri) }
                val currentLand = _project.value.landScan
                val result = aiRepository.analyzeLandPhoto(
                    bitmap = bitmap,
                    photoUri = uri.toString(),
                    referenceLengthM = currentLand.lengthM,
                    referenceWidthM = currentLand.widthM,
                    shape = currentLand.shape
                )
                _project.update { current ->
                    refreshDesignsAndMaterials(current.copy(landScan = result))
                }
                _snackbarEvents.emit("تم تحليل صورة الأرض وحساب المساحة (${EngineeringCalculator.formatNumber(result.estimatedAreaSqm)} م²)")
            } catch (e: Exception) {
                _snackbarEvents.emit("تعذر قراءة الصورة، يرجى المحاولة مرة أخرى")
            } finally {
                _isAnalyzingLand.value = false
            }
        }
    }

    fun runLandCalibrationEstimate() {
        viewModelScope.launch {
            _isAnalyzingLand.value = true
            try {
                val currentLand = _project.value.landScan
                val bitmap = currentLand.photoUri?.let { uriStr ->
                    withContext(Dispatchers.IO) { decodeBitmap(Uri.parse(uriStr)) }
                }
                val result = aiRepository.analyzeLandPhoto(
                    bitmap = bitmap,
                    photoUri = currentLand.photoUri,
                    referenceLengthM = currentLand.lengthM,
                    referenceWidthM = currentLand.widthM,
                    shape = currentLand.shape
                )
                _project.update { current ->
                    refreshDesignsAndMaterials(current.copy(landScan = result))
                }
            } finally {
                _isAnalyzingLand.value = false
            }
        }
    }

    fun selectBuildingType(type: BuildingType) {
        _project.update { current ->
            val adjustedFloors = current.floors.coerceAtMost(type.maxRecommendedFloors)
            val autoName = if (current.id == 0L) {
                "${type.arabicName} - $adjustedFloors طوابق"
            } else {
                current.projectName
            }
            refreshDesignsAndMaterials(
                current.copy(
                    projectName = autoName,
                    buildingType = type,
                    floors = adjustedFloors
                )
            )
        }
    }

    fun selectConstructionType(type: ConstructionType) {
        _project.update { current ->
            refreshDesignsAndMaterials(current.copy(constructionType = type))
        }
    }

    fun updateFloors(floors: Int) {
        val safeFloors = floors.coerceIn(1, 20)
        _project.update { current ->
            val autoName = if (current.id == 0L) {
                "${current.buildingType.arabicName} - $safeFloors طوابق"
            } else {
                current.projectName
            }
            refreshDesignsAndMaterials(current.copy(projectName = autoName, floors = safeFloors))
        }
    }

    fun generateArchitecturalDesigns() {
        viewModelScope.launch {
            _isGeneratingDesigns.value = true
            try {
                val cur = _project.value
                val (designs, isSample) = aiRepository.generateArchitecturalDesigns(
                    areaSqm = cur.landScan.estimatedAreaSqm,
                    buildingType = cur.buildingType,
                    constructionType = cur.constructionType,
                    floors = cur.floors
                )
                val selected = designs.firstOrNull { it.id == cur.selectedDesignId } ?: designs.first()
                val updatedMaterials = EngineeringCalculator.calculateMaterials(
                    totalBuiltAreaSqm = selected.totalBuiltAreaSqm,
                    footprintAreaSqm = selected.footprintAreaSqm,
                    floors = cur.floors,
                    buildingType = cur.buildingType,
                    constructionType = cur.constructionType,
                    currency = cur.currency,
                    customDefaultPricesUsd = _customDefaultPricesUsd.value,
                    existingItems = cur.materials
                )
                _project.update {
                    it.copy(
                        designs = designs,
                        designsAreAiSample = isSample,
                        selectedDesignId = selected.id,
                        materials = updatedMaterials
                    )
                }
                _snackbarEvents.emit("تم توليد ${designs.size} تصاميم هندسية لـ ${cur.floors} طوابق")
            } finally {
                _isGeneratingDesigns.value = false
            }
        }
    }

    fun selectDesign(designId: String) {
        _project.update { current ->
            val selected = current.designs.firstOrNull { it.id == designId } ?: return@update current
            val customItems = current.materials.filter { it.id.startsWith("custom_") }
            val computed = EngineeringCalculator.calculateMaterials(
                totalBuiltAreaSqm = selected.totalBuiltAreaSqm,
                footprintAreaSqm = selected.footprintAreaSqm,
                floors = current.floors,
                buildingType = current.buildingType,
                constructionType = current.constructionType,
                currency = current.currency,
                customDefaultPricesUsd = _customDefaultPricesUsd.value,
                existingItems = current.materials
            )
            current.copy(
                selectedDesignId = selected.id,
                materials = computed + customItems
            )
        }
    }

    fun updateMaterialGrade(materialId: String, gradeId: String) {
        _project.update { current ->
            val updated = current.materials.map { item ->
                if (item.id != materialId) item
                else {
                    val oldMult = item.selectedGrade.priceMultiplier.coerceAtLeast(0.1)
                    val newGrade = item.availableGrades.firstOrNull { it.id == gradeId } ?: item.selectedGrade
                    val baseUnitPrice = item.unitPrice / oldMult
                    val adjustedPrice = ((baseUnitPrice * newGrade.priceMultiplier) * 100.0).roundToInt() / 100.0
                    item.copy(selectedGradeId = gradeId, unitPrice = adjustedPrice)
                }
            }
            current.copy(materials = updated)
        }
    }

    fun updateMaterialQuantity(materialId: String, quantity: Double?) {
        _project.update { current ->
            val updated = current.materials.map { item ->
                if (item.id != materialId) item
                else item.copy(customQuantity = quantity?.coerceAtLeast(0.0))
            }
            current.copy(materials = updated)
        }
    }

    fun updateMaterialUnitPrice(materialId: String, unitPrice: Double) {
        _project.update { current ->
            val updated = current.materials.map { item ->
                if (item.id != materialId) item
                else item.copy(unitPrice = unitPrice.coerceAtLeast(0.0))
            }
            current.copy(materials = updated)
        }
    }

    fun toggleMaterialIncluded(materialId: String, included: Boolean) {
        _project.update { current ->
            val updated = current.materials.map { item ->
                if (item.id != materialId) item else item.copy(isIncluded = included)
            }
            current.copy(materials = updated)
        }
    }

    fun addCustomMaterial(
        name: String,
        unit: String,
        quantity: Double,
        unitPrice: Double,
        category: MaterialCategory
    ) {
        if (name.isBlank()) return
        val newItem = MaterialItem(
            id = "custom_${System.currentTimeMillis()}",
            name = name.trim(),
            category = category,
            unit = unit.ifBlank { "وحدة" }.trim(),
            estimatedQuantity = quantity.coerceAtLeast(0.0),
            customQuantity = null,
            unitPrice = unitPrice.coerceAtLeast(0.0),
            selectedGradeId = "custom",
            availableGrades = listOf(MaterialTypeOption("custom", "مخصص", 1.0)),
            isIncluded = true,
            notes = "بند مضاف من المستخدم"
        )
        _project.update { current ->
            current.copy(materials = current.materials + newItem)
        }
    }

    fun changeCurrency(currency: CurrencyOption) {
        viewModelScope.launch {
            preferencesRepository.setPreferredCurrency(currency)
        }
        _project.update { current ->
            recalculateMaterialsForCurrency(current, currency)
        }
    }

    fun updateDefaultPriceUsd(materialId: String, priceInCurrentCurrency: Double) {
        val rate = _project.value.currency.rateFromUsd.coerceAtLeast(0.01)
        val priceUsd = priceInCurrentCurrency / rate
        _customDefaultPricesUsd.update { it + (materialId to priceUsd) }
        _project.update { current ->
            val updated = current.materials.map { item ->
                if (item.id == materialId) item.copy(unitPrice = priceInCurrentCurrency) else item
            }
            current.copy(materials = updated)
        }
    }

    fun saveCurrentProject() {
        viewModelScope.launch {
            val savedId = projectRepository.saveProject(_project.value)
            _project.update { it.copy(id = savedId, updatedAt = System.currentTimeMillis()) }
            _snackbarEvents.emit("تم حفظ المشروع محلياً بنجاح")
        }
    }

    fun loadProject(project: EngineeringProject) {
        _project.value = project
        _drawerDestination.value = DrawerDestination.MAIN_WORKFLOW
        _selectedTab.value = MainTab.MATERIALS
    }

    fun deleteProject(projectId: Long) {
        viewModelScope.launch {
            projectRepository.deleteProject(projectId)
            if (_project.value.id == projectId) {
                _project.update { it.copy(id = 0L) }
            }
            _snackbarEvents.emit("تم حذف المشروع")
        }
    }

    fun buildShareReportText(): String {
        val p = _project.value
        val cur = p.currency.symbol
        val design = p.selectedDesign
        val sb = StringBuilder()
        sb.appendLine("تقرير التكلفة والكميات الهندسي — تطبيق بنيان الهندسي")
        sb.appendLine("تصميم: المهندس طه الحدا")
        sb.appendLine("==============================")
        sb.appendLine("المشروع: ${p.projectName}")
        sb.appendLine("مساحة الأرض: ${EngineeringCalculator.formatNumber(p.landScan.estimatedAreaSqm)} م²")
        sb.appendLine("نوع المبنى: ${p.buildingType.arabicName}")
        sb.appendLine("نظام البناء: ${p.constructionType.arabicName}")
        sb.appendLine("عدد الطوابق: ${p.floors}")
        if (design != null) {
            sb.appendLine("التصميم المختار: ${design.title}")
            sb.appendLine("المساحة البنائية الكلية: ${EngineeringCalculator.formatNumber(design.totalBuiltAreaSqm)} م²")
        }
        sb.appendLine("------------------------------")
        sb.appendLine("جدول المواد والتكاليف:")
        p.materials.filter { it.isIncluded }.forEach { item ->
            sb.appendLine(
                "- ${item.name} (${item.selectedGrade.name}): " +
                    "${EngineeringCalculator.formatNumber(item.effectiveQuantity)} ${item.unit} × " +
                    "${EngineeringCalculator.formatNumber(item.unitPrice)} $cur = " +
                    "${EngineeringCalculator.formatNumber(item.totalCost)} $cur"
            )
        }
        sb.appendLine("==============================")
        sb.appendLine("التكلفة الإجمالية التقديرية: ${EngineeringCalculator.formatNumber(p.totalCost)} $cur")
        sb.appendLine("تكلفة المتر المربع: ${EngineeringCalculator.formatNumber(p.costPerSqm)} $cur/م²")
        return sb.toString()
    }

    private fun refreshDesignsAndMaterials(project: EngineeringProject): EngineeringProject {
        val designs = EngineeringCalculator.generateDesigns(
            landAreaSqm = project.landScan.estimatedAreaSqm,
            buildingType = project.buildingType,
            constructionType = project.constructionType,
            floors = project.floors,
            isAiSample = project.designsAreAiSample
        )
        val selected = designs.firstOrNull { it.id == project.selectedDesignId } ?: designs.first()
        val customItems = project.materials.filter { it.id.startsWith("custom_") }
        val updatedMaterials = EngineeringCalculator.calculateMaterials(
            totalBuiltAreaSqm = selected.totalBuiltAreaSqm,
            footprintAreaSqm = selected.footprintAreaSqm,
            floors = project.floors,
            buildingType = project.buildingType,
            constructionType = project.constructionType,
            currency = project.currency,
            customDefaultPricesUsd = _customDefaultPricesUsd.value,
            existingItems = project.materials
        )
        return project.copy(
            designs = designs,
            selectedDesignId = selected.id,
            materials = updatedMaterials + customItems
        )
    }

    private fun recalculateMaterialsForCurrency(
        project: EngineeringProject,
        newCurrency: CurrencyOption
    ): EngineeringProject {
        val ratio = newCurrency.rateFromUsd / project.currency.rateFromUsd.coerceAtLeast(0.001)
        val updated = project.materials.map { item ->
            val converted = ((item.unitPrice * ratio) * 100.0).roundToInt() / 100.0
            item.copy(unitPrice = converted)
        }
        return project.copy(currency = newCurrency, materials = updated)
    }

    private fun decodeBitmap(uri: Uri): Bitmap? {
        val resolver = getApplication<Application>().contentResolver
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(resolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                val maxDim = 1024
                val w = info.size.width
                val h = info.size.height
                if (w > maxDim || h > maxDim) {
                    val scale = maxDim.toFloat() / maxOf(w, h).toFloat()
                    decoder.setTargetSize((w * scale).roundToInt(), (h * scale).roundToInt())
                }
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(resolver, uri)
        }
    }
}
