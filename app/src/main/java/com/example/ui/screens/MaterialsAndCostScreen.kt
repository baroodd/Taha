package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.engine.EngineeringCalculator
import com.example.model.CurrencyOption
import com.example.model.EngineeringProject
import com.example.model.MaterialCategory
import com.example.model.MaterialItem
import com.example.ui.components.CostBreakdownChart

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MaterialsAndCostScreen(
    project: EngineeringProject,
    onUpdateProjectName: (String) -> Unit,
    onChangeCurrency: (CurrencyOption) -> Unit,
    onUpdateMaterialGrade: (String, String) -> Unit,
    onUpdateMaterialQuantity: (String, Double?) -> Unit,
    onUpdateMaterialUnitPrice: (String, Double) -> Unit,
    onToggleMaterialIncluded: (String, Boolean) -> Unit,
    onAddCustomMaterial: (String, String, Double, Double, MaterialCategory) -> Unit,
    onSaveProject: () -> Unit,
    buildShareText: () -> String
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf<MaterialCategory?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredMaterials = remember(project.materials, selectedCategory) {
        if (selectedCategory == null) project.materials
        else project.materials.filter { it.category == selectedCategory }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 720.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Grand Total Cost Hero Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF0D2B47), Color(0xFF1A436D))
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Text(
                            text = "ملخص التكلفة التقديرية الكاملة",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFFFBBF24)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${EngineeringCalculator.formatNumber(project.totalCost)} ${project.currency.symbol}",
                            style = MaterialTheme.typography.displaySmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SummaryPill(
                                label = "تكلفة المتر²",
                                value = "${EngineeringCalculator.formatNumber(project.costPerSqm)} ${project.currency.symbol}",
                                modifier = Modifier.weight(1f)
                            )
                            SummaryPill(
                                label = "تكلفة الطابق",
                                value = "${EngineeringCalculator.formatNumber(project.costPerFloor)} ${project.currency.symbol}",
                                modifier = Modifier.weight(1f)
                            )
                            SummaryPill(
                                label = "مساحة البناء",
                                value = "${EngineeringCalculator.formatNumber(project.builtAreaSqm)} م²",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onSaveProject,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("save_project_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("حفظ المشروع")
                            }
                            OutlinedButton(
                                onClick = {
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, project.projectName)
                                        putExtra(Intent.EXTRA_TEXT, buildShareText())
                                    }
                                    context.startActivity(
                                        Intent.createChooser(sendIntent, "مشاركة تقرير التكاليف")
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("share_report_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("مشاركة التقرير")
                            }
                        }
                    }
                }
            }
        }

        // Project Name & Currency Selection
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    OutlinedTextField(
                        value = project.projectName,
                        onValueChange = onUpdateProjectName,
                        label = { Text("اسم المشروع") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("project_name_input")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "عملة التسعير",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(CurrencyOption.entries) { currency ->
                            FilterChip(
                                selected = project.currency == currency,
                                onClick = { onChangeCurrency(currency) },
                                label = { Text("${currency.arabicName} (${currency.symbol})") },
                                modifier = Modifier.testTag("currency_${currency.code}")
                            )
                        }
                    }
                }
            }
        }

        // Cost Breakdown Chart
        item {
            CostBreakdownChart(
                materials = project.materials,
                currencySymbol = project.currency.symbol
            )
        }

        // Category Filter + Add Custom Material Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "جدول الكميات والمواد (${filteredMaterials.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.testTag("add_custom_material_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة بند")
                }
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("الكل") }
                    )
                }
                items(MaterialCategory.entries) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category.arabicName) }
                    )
                }
            }
        }

        // Editable Material Cards
        items(filteredMaterials, key = { it.id }) { item ->
            MaterialItemCard(
                item = item,
                currencySymbol = project.currency.symbol,
                onSelectGrade = { gradeId -> onUpdateMaterialGrade(item.id, gradeId) },
                onQuantityChange = { qty -> onUpdateMaterialQuantity(item.id, qty) },
                onUnitPriceChange = { price -> onUpdateMaterialUnitPrice(item.id, price) },
                onToggleIncluded = { inc -> onToggleMaterialIncluded(item.id, inc) }
            )
        }
    }

    if (showAddDialog) {
        AddCustomMaterialDialog(
            currencySymbol = project.currency.symbol,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, unit, qty, price, cat ->
                onAddCustomMaterial(name, unit, qty, price, cat)
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MaterialItemCard(
    item: MaterialItem,
    currencySymbol: String,
    onSelectGrade: (String) -> Unit,
    onQuantityChange: (Double?) -> Unit,
    onUnitPriceChange: (Double) -> Unit,
    onToggleIncluded: (Boolean) -> Unit
) {
    var qtyInput by remember(item.id, item.effectiveQuantity) {
        mutableStateOf(formatInputNumber(item.effectiveQuantity))
    }
    var priceInput by remember(item.id, item.unitPrice) {
        mutableStateOf(formatInputNumber(item.unitPrice))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("material_card_${item.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isIncluded) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${item.category.arabicName} • ${item.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = item.isIncluded,
                    onCheckedChange = onToggleIncluded
                )
            }

            if (item.availableGrades.size > 1) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    item.availableGrades.forEach { grade ->
                        FilterChip(
                            selected = item.selectedGradeId == grade.id,
                            onClick = { onSelectGrade(grade.id) },
                            label = { Text(grade.name, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = qtyInput,
                    onValueChange = { text ->
                        qtyInput = text
                        text.toDoubleOrNull()?.let { onQuantityChange(it) }
                    },
                    label = { Text("الكمية (${item.unit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    trailingIcon = if (item.customQuantity != null) {
                        {
                            IconButton(onClick = { onQuantityChange(null) }) {
                                Icon(
                                    imageVector = Icons.Default.RestartAlt,
                                    contentDescription = "إعادة للكمية الهندسية"
                                )
                            }
                        }
                    } else null,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("qty_input_${item.id}")
                )

                OutlinedTextField(
                    value = priceInput,
                    onValueChange = { text ->
                        priceInput = text
                        text.toDoubleOrNull()?.let { onUnitPriceChange(it) }
                    },
                    label = { Text("سعر الوحدة ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("price_input_${item.id}")
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (item.customQuantity != null) {
                        "التقدير الهندسي الأصلي: ${EngineeringCalculator.formatNumber(item.estimatedQuantity)} ${item.unit}"
                    } else {
                        "كمية محسوبة هندسياً"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${EngineeringCalculator.formatNumber(item.totalCost)} $currencySymbol",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (item.isIncluded) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SummaryPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x22FFFFFF))
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFD6E4F5)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddCustomMaterialDialog(
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, Double, MaterialCategory) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("وحدة") }
    var qtyText by remember { mutableStateOf("10") }
    var priceText by remember { mutableStateOf("50") }
    var category by remember { mutableStateOf(MaterialCategory.FINISHES) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة مادة أو مصروف مخصص") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المادة (مثلاً: مصعد، خزان ماء)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("الوحدة") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = qtyText,
                        onValueChange = { qtyText = it },
                        label = { Text("الكمية") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("سعر الوحدة ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MaterialCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.arabicName, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = qtyText.toDoubleOrNull() ?: 1.0
                    val price = priceText.toDoubleOrNull() ?: 0.0
                    onConfirm(name, unit, qty, price, category)
                },
                enabled = name.isNotBlank()
            ) {
                Text("إضافة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

private fun formatInputNumber(value: Double): String {
    return if (value % 1.0 == 0.0) value.toInt().toString() else String.format(java.util.Locale.US, "%.2f", value)
}
