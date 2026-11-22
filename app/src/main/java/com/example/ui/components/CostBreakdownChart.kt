package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.engine.EngineeringCalculator
import com.example.model.MaterialCategory
import com.example.model.MaterialItem
import com.example.ui.theme.ChartFinishesColor
import com.example.ui.theme.ChartLaborColor
import com.example.ui.theme.ChartMepColor
import com.example.ui.theme.ChartStructuralColor

@Composable
fun CostBreakdownChart(
    materials: List<MaterialItem>,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    val total = materials.sumOf { it.totalCost }.coerceAtLeast(1.0)
    val categories = listOf(
        Triple(MaterialCategory.STRUCTURAL, "العظم والهيكل", ChartStructuralColor),
        Triple(MaterialCategory.FINISHES, "التشطيبات", ChartFinishesColor),
        Triple(MaterialCategory.MEP, "الكهرباء والسباكة", ChartMepColor),
        Triple(MaterialCategory.LABOR, "العمالة والمصنعية", ChartLaborColor)
    )

    val slices = categories.map { (cat, label, color) ->
        val amount = materials.filter { it.category == cat }.sumOf { it.totalCost }
        val pct = (amount / total).toFloat().coerceIn(0f, 1f)
        Quad(label, amount, pct, color)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "توزيع التكاليف حسب مراحل البناء",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier.size(124.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(116.dp)) {
                        var startAngle = -90f
                        slices.forEach { slice ->
                            val sweep = slice.pct * 360f
                            if (sweep > 0.5f) {
                                drawArc(
                                    color = slice.color,
                                    startAngle = startAngle,
                                    sweepAngle = sweep - 3f,
                                    useCenter = false,
                                    style = Stroke(width = 22f, cap = StrokeCap.Round)
                                )
                                startAngle += sweep
                            }
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "التكلفة",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${slices.count { it.amount > 0 }} مراحل",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    slices.forEach { slice ->
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(slice.color)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = slice.label,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = "${(slice.pct * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = slice.color
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            LinearProgressIndicator(
                                progress = { slice.pct },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = slice.color,
                                trackColor = slice.color.copy(alpha = 0.16f)
                            )
                            Text(
                                text = "${EngineeringCalculator.formatNumber(slice.amount)} $currencySymbol",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class Quad(
    val label: String,
    val amount: Double,
    val pct: Float,
    val color: Color
)
