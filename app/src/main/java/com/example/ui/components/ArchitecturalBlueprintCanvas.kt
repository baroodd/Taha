package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.engine.EngineeringCalculator
import com.example.model.ArchitecturalDesignOption
import kotlin.math.sqrt

@Composable
fun ArchitecturalBlueprintCanvas(
    design: ArchitecturalDesignOption,
    landAreaSqm: Double,
    modifier: Modifier = Modifier
) {
    val blueprintBg = Color(0xFF0C223B)
    val gridLineColor = Color(0x2878A8E0)
    val setbackLineColor = Color(0x88E08E45)
    val zoneColors = listOf(
        Color(0x552E6DB4),
        Color(0x550F766E),
        Color(0x55C86D2D),
        Color(0x5564748B)
    )
    val wallStrokeColor = Color(0xFFE2EEFF)

    val sideApprox = sqrt(landAreaSqm.coerceAtLeast(60.0))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(blueprintBg)
            .border(1.dp, Color(0xFF284D78), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Blueprint grid
            val step = 24f
            var x = 0f
            while (x <= w) {
                drawLine(gridLineColor, Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
                x += step
            }
            var y = 0f
            while (y <= h) {
                drawLine(gridLineColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
                y += step
            }

            // Land plot outer boundary
            val plotPad = 14f
            drawRect(
                color = setbackLineColor,
                topLeft = Offset(plotPad, plotPad),
                size = Size(w - plotPad * 2, h - plotPad * 2),
                style = Stroke(
                    width = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                )
            )

            // Building footprint box inside setbacks
            val bPad = 28f
            val bx = bPad
            val by = bPad
            val bw = (w - bPad * 2).coerceAtLeast(40f)
            val bh = (h - bPad * 2).coerceAtLeast(40f)

            drawRoundRect(
                color = Color(0x22FFFFFF),
                topLeft = Offset(bx, by),
                size = Size(bw, bh),
                cornerRadius = CornerRadius(6f, 6f)
            )

            // Draw zones
            design.zones.forEach { zone ->
                val zx = bx + zone.x * bw
                val zy = by + zone.y * bh
                val zw = zone.w * bw
                val zh = zone.h * bh
                val fill = zoneColors[zone.colorIndex % zoneColors.size]
                drawRect(
                    color = fill,
                    topLeft = Offset(zx, zy),
                    size = Size(zw, zh)
                )
                drawRect(
                    color = wallStrokeColor,
                    topLeft = Offset(zx, zy),
                    size = Size(zw, zh),
                    style = Stroke(width = 2.4f)
                )
            }

            // Outer thick structural walls + corner columns
            drawRect(
                color = wallStrokeColor,
                topLeft = Offset(bx, by),
                size = Size(bw, bh),
                style = Stroke(width = 3.6f)
            )

            val colRadius = 4.5f
            val colPositions = listOf(
                Offset(bx, by),
                Offset(bx + bw / 2f, by),
                Offset(bx + bw, by),
                Offset(bx, by + bh / 2f),
                Offset(bx + bw / 2f, by + bh / 2f),
                Offset(bx + bw, by + bh / 2f),
                Offset(bx, by + bh),
                Offset(bx + bw / 2f, by + bh),
                Offset(bx + bw, by + bh)
            )
            colPositions.forEach { pos ->
                drawCircle(color = Color(0xFFF59E0B), radius = colRadius, center = pos)
            }
        }

        // Zone labels positioned over blueprint
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
        ) {
            design.zones.forEach { zone ->
                val alignX = (zone.x + zone.w / 2f)
                val alignY = (zone.y + zone.h / 2f)
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = zone.label,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .align(
                                androidx.compose.ui.BiasAlignment(
                                    horizontalBias = ((1f - alignX) * 2f - 1f).coerceIn(-0.9f, 0.9f),
                                    verticalBias = (alignY * 2f - 1f).coerceIn(-0.9f, 0.9f)
                                )
                            )
                            .background(Color(0x990C223B), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }

        // Engineering scale pill
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .background(Color(0xCC0A192C), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = "نسبة البناء ${(design.coverageRatio * 100).toInt()}% • ~${EngineeringCalculator.formatNumber(sideApprox)}×${EngineeringCalculator.formatNumber(sideApprox)} م",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFFBBF24)
            )
        }
    }
}
