package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Castle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Villa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.engine.EngineeringCalculator
import com.example.model.BuildingType
import com.example.model.ConstructionType
import com.example.model.EngineeringProject
import com.example.ui.components.AiFirebaseNoticeBanner
import com.example.ui.components.ArchitecturalBlueprintCanvas

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DesignScreen(
    project: EngineeringProject,
    isGenerating: Boolean,
    onSelectBuildingType: (BuildingType) -> Unit,
    onSelectConstructionType: (ConstructionType) -> Unit,
    onUpdateFloors: (Int) -> Unit,
    onGenerateDesigns: () -> Unit,
    onSelectDesign: (String) -> Unit,
    onContinueToMaterials: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 720.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Building Type Selector
        item {
            Text(
                text = "1. اختر نوعية المبنى",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(BuildingType.entries) { type ->
                    val isSelected = project.buildingType == type
                    Card(
                        modifier = Modifier
                            .width(142.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelectBuildingType(type) }
                            .testTag("building_type_${type.name}"),
                        shape = RoundedCornerShape(16.dp),
                        border = if (isSelected) {
                            BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
                        } else null,
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Icon(
                                imageVector = buildingTypeIcon(type),
                                contentDescription = type.arabicName,
                                tint = if (isSelected) {
                                    MaterialTheme.colorScheme.secondary
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = type.arabicName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = type.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }

        // Construction Type Selector
        item {
            Text(
                text = "2. اختر نوعية البناء والهيكل",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ConstructionType.entries.forEach { cType ->
                    val isSelected = project.constructionType == cType
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelectConstructionType(cType) }
                            .testTag("construction_type_${cType.name}"),
                        border = if (isSelected) {
                            BorderStroke(1.8.dp, MaterialTheme.colorScheme.primary)
                        } else null,
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onSelectConstructionType(cType) }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = cType.arabicName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = cType.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floors Selector + Generate Button
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "3. عدد الطوابق المطلوبة",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "الحد الموصى به لـ ${project.buildingType.arabicName}: ${project.buildingType.maxRecommendedFloors} طابق",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FilledTonalIconButton(
                                onClick = { onUpdateFloors(project.floors - 1) },
                                enabled = project.floors > 1,
                                modifier = Modifier.testTag("floors_decrement")
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "إنقاص طابق")
                            }
                            Text(
                                text = "${project.floors}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 14.dp)
                            )
                            FilledTonalIconButton(
                                onClick = { onUpdateFloors(project.floors + 1) },
                                enabled = project.floors < 20,
                                modifier = Modifier.testTag("floors_increment")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "زيادة طابق")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1, 2, 3, 4, 6, 10).forEach { preset ->
                            FilterChip(
                                selected = project.floors == preset,
                                onClick = { onUpdateFloors(preset) },
                                label = { Text("$preset طوابق") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onGenerateDesigns,
                        enabled = !isGenerating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("generate_designs_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("جارٍ توليد التصاميم...")
                        } else {
                            Icon(Icons.Default.Architecture, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("توليد عدة تصاميم هندسية لـ ${project.floors} طوابق")
                        }
                    }
                }
            }
        }

        // AI Sample Notice if designs were generated via AiRepository placeholder
        if (project.designsAreAiSample) {
            item {
                AiFirebaseNoticeBanner()
            }
        }

        // Generated Design Options
        item {
            Text(
                text = "التصاميم المقترحة (${project.designs.size} خيارات)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(project.designs, key = { it.id }) { design ->
            val isSelected = project.selectedDesign?.id == design.id
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .clickable { onSelectDesign(design.id) }
                    .testTag("design_option_${design.id}"),
                border = if (isSelected) {
                    BorderStroke(2.2.dp, MaterialTheme.colorScheme.secondary)
                } else null,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = design.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = design.styleTag,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        if (isSelected) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "مختار",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "مختار",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    ArchitecturalBlueprintCanvas(
                        design = design,
                        landAreaSqm = project.landScan.estimatedAreaSqm
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = design.summary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DesignMetricBadge(
                            label = "مساحة الطابق",
                            value = "${EngineeringCalculator.formatNumber(design.footprintAreaSqm)} م²",
                            modifier = Modifier.weight(1f)
                        )
                        DesignMetricBadge(
                            label = "البناء الكلي",
                            value = "${EngineeringCalculator.formatNumber(design.totalBuiltAreaSqm)} م²",
                            modifier = Modifier.weight(1f)
                        )
                        DesignMetricBadge(
                            label = "غرف/طابق",
                            value = "${design.roomsPerFloor} غرف",
                            modifier = Modifier.weight(0.85f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = design.structuralNotes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }

        // Continue to Materials & Costs
        item {
            Button(
                onClick = onContinueToMaterials,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("continue_to_materials_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text(
                    text = "التالي: جدول المواد وإدخال الأسعار والتكاليف",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
private fun DesignMetricBadge(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun buildingTypeIcon(type: BuildingType): ImageVector {
    return when (type) {
        BuildingType.HOUSE -> Icons.Default.Home
        BuildingType.VILLA -> Icons.Default.Villa
        BuildingType.PALACE -> Icons.Default.Castle
        BuildingType.RESIDENTIAL_BUILDING -> Icons.Default.Apartment
        BuildingType.HOSPITAL -> Icons.Default.LocalHospital
        BuildingType.SCHOOL -> Icons.Default.School
        BuildingType.COMMERCIAL_TOWER -> Icons.Default.Business
        BuildingType.MOSQUE -> Icons.Default.Mosque
    }
}
