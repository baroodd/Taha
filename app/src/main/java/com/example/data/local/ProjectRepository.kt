package com.example.data.local

import com.example.data.engine.EngineeringCalculator
import com.example.model.BuildingType
import com.example.model.ConstructionType
import com.example.model.CurrencyOption
import com.example.model.EngineeringProject
import com.example.model.LandScanResult
import com.example.model.LandShape
import com.example.model.MaterialCategory
import com.example.model.MaterialItem
import com.example.model.MaterialTypeOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class ProjectRepository(private val projectDao: ProjectDao) {

    val savedProjects: Flow<List<EngineeringProject>> = projectDao.observeAllProjects().map { list ->
        list.mapNotNull { entity -> runCatching { deserializeProject(entity) }.getOrNull() }
    }

    suspend fun saveProject(project: EngineeringProject): Long {
        val now = System.currentTimeMillis()
        val entity = ProjectEntity(
            id = project.id,
            projectName = project.projectName.ifBlank { "${project.buildingType.arabicName} - ${project.floors} طوابق" },
            buildingTypeName = project.buildingType.name,
            constructionTypeName = project.constructionType.name,
            floors = project.floors,
            landAreaSqm = project.landScan.estimatedAreaSqm,
            totalBuiltAreaSqm = project.builtAreaSqm,
            totalCost = project.totalCost,
            currencyCode = project.currency.code,
            selectedDesignId = project.selectedDesignId,
            payloadJson = serializeProject(project.copy(updatedAt = now)),
            updatedAt = now
        )
        return projectDao.insertOrUpdate(entity)
    }

    suspend fun deleteProject(projectId: Long) {
        projectDao.deleteById(projectId)
    }

    private fun serializeProject(project: EngineeringProject): String {
        val root = JSONObject()
        val land = JSONObject().apply {
            put("photoUri", project.landScan.photoUri ?: JSONObject.NULL)
            put("estimatedAreaSqm", project.landScan.estimatedAreaSqm)
            put("lengthM", project.landScan.lengthM)
            put("widthM", project.landScan.widthM)
            put("northSideM", project.landScan.northSideM)
            put("southSideM", project.landScan.southSideM)
            put("eastSideM", project.landScan.eastSideM)
            put("westSideM", project.landScan.westSideM)
            put("shape", project.landScan.shape.name)
            put("terrainNote", project.landScan.terrainNote)
            put("confidenceNote", project.landScan.confidenceNote)
            put("isSampleAiOutput", project.landScan.isSampleAiOutput)
        }
        root.put("landScan", land)
        root.put("designsAreAiSample", project.designsAreAiSample)

        val materialsArray = JSONArray()
        project.materials.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("name", item.name)
                put("category", item.category.name)
                put("unit", item.unit)
                put("estimatedQuantity", item.estimatedQuantity)
                put("customQuantity", item.customQuantity ?: JSONObject.NULL)
                put("unitPrice", item.unitPrice)
                put("selectedGradeId", item.selectedGradeId)
                put("isIncluded", item.isIncluded)
                put("notes", item.notes)
                val gradesArr = JSONArray()
                item.availableGrades.forEach { g ->
                    gradesArr.put(JSONObject().apply {
                        put("id", g.id)
                        put("name", g.name)
                        put("priceMultiplier", g.priceMultiplier)
                    })
                }
                put("grades", gradesArr)
            }
            materialsArray.put(obj)
        }
        root.put("materials", materialsArray)
        return root.toString()
    }

    private fun deserializeProject(entity: ProjectEntity): EngineeringProject {
        val root = JSONObject(entity.payloadJson)
        val landObj = root.getJSONObject("landScan")
        val shape = LandShape.entries.firstOrNull { it.name == landObj.optString("shape") }
            ?: LandShape.RECTANGLE
        val landScan = LandScanResult(
            photoUri = if (landObj.isNull("photoUri")) null else landObj.optString("photoUri"),
            estimatedAreaSqm = landObj.optDouble("estimatedAreaSqm", entity.landAreaSqm),
            lengthM = landObj.optDouble("lengthM", 20.0),
            widthM = landObj.optDouble("widthM", 20.0),
            northSideM = landObj.optDouble("northSideM", 20.0),
            southSideM = landObj.optDouble("southSideM", 20.0),
            eastSideM = landObj.optDouble("eastSideM", 20.0),
            westSideM = landObj.optDouble("westSideM", 20.0),
            shape = shape,
            terrainNote = landObj.optString("terrainNote"),
            confidenceNote = landObj.optString("confidenceNote"),
            isSampleAiOutput = landObj.optBoolean("isSampleAiOutput", false)
        )

        val buildingType = BuildingType.entries.firstOrNull { it.name == entity.buildingTypeName }
            ?: BuildingType.VILLA
        val constructionType = ConstructionType.entries.firstOrNull { it.name == entity.constructionTypeName }
            ?: ConstructionType.REINFORCED_TRADITIONAL
        val currency = CurrencyOption.entries.firstOrNull { it.code == entity.currencyCode }
            ?: CurrencyOption.SAR

        val designsSample = root.optBoolean("designsAreAiSample", true)
        val designs = EngineeringCalculator.generateDesigns(
            landAreaSqm = landScan.estimatedAreaSqm,
            buildingType = buildingType,
            constructionType = constructionType,
            floors = entity.floors,
            isAiSample = designsSample
        )

        val materialsArray = root.optJSONArray("materials") ?: JSONArray()
        val materials = mutableListOf<MaterialItem>()
        for (i in 0 until materialsArray.length()) {
            val obj = materialsArray.getJSONObject(i)
            val cat = MaterialCategory.entries.firstOrNull { it.name == obj.optString("category") }
                ?: MaterialCategory.STRUCTURAL
            val gradesArr = obj.optJSONArray("grades") ?: JSONArray()
            val grades = mutableListOf<MaterialTypeOption>()
            for (gIdx in 0 until gradesArr.length()) {
                val gObj = gradesArr.getJSONObject(gIdx)
                grades.add(
                    MaterialTypeOption(
                        id = gObj.getString("id"),
                        name = gObj.getString("name"),
                        priceMultiplier = gObj.optDouble("priceMultiplier", 1.0)
                    )
                )
            }
            materials.add(
                MaterialItem(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    category = cat,
                    unit = obj.getString("unit"),
                    estimatedQuantity = obj.optDouble("estimatedQuantity", 0.0),
                    customQuantity = if (obj.isNull("customQuantity")) null else obj.optDouble("customQuantity"),
                    unitPrice = obj.optDouble("unitPrice", 0.0),
                    selectedGradeId = obj.optString("selectedGradeId", "std"),
                    availableGrades = grades,
                    isIncluded = obj.optBoolean("isIncluded", true),
                    notes = obj.optString("notes")
                )
            )
        }

        return EngineeringProject(
            id = entity.id,
            projectName = entity.projectName,
            landScan = landScan,
            buildingType = buildingType,
            constructionType = constructionType,
            floors = entity.floors,
            selectedDesignId = entity.selectedDesignId,
            designs = designs,
            designsAreAiSample = designsSample,
            materials = materials,
            currency = currency,
            updatedAt = entity.updatedAt
        )
    }
}
