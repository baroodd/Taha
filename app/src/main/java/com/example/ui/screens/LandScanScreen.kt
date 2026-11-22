package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.R
import com.example.data.engine.EngineeringCalculator
import com.example.model.LandScanResult
import com.example.model.LandShape
import com.example.ui.components.AiFirebaseNoticeBanner
import java.io.File

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LandScanScreen(
    landScan: LandScanResult,
    isAnalyzing: Boolean,
    onPhotoSelected: (Uri) -> Unit,
    onShapeSelected: (LandShape) -> Unit,
    onDimensionsChanged: (
        lengthM: Double?,
        widthM: Double?,
        northM: Double?,
        southM: Double?,
        eastM: Double?,
        westM: Double?
    ) -> Unit,
    onRunCalibration: () -> Unit,
    onContinueToDesigns: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingCameraUri
        if (success && uri != null) {
            onPhotoSelected(uri)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createTempImageUri(context)
            pendingCameraUri = uri
            takePictureLauncher.launch(uri)
        } else {
            onShowMessage("تم رفض إذن الكاميرا. يمكنك اختيار صورة للأرض من المعرض أو إدخال الأبعاد مباشرة.")
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onPhotoSelected(uri)
        }
    }

    var lengthText by remember(landScan.lengthM) { mutableStateOf(cleanNum(landScan.lengthM)) }
    var widthText by remember(landScan.widthM) { mutableStateOf(cleanNum(landScan.widthM)) }
    var northText by remember(landScan.northSideM) { mutableStateOf(cleanNum(landScan.northSideM)) }
    var southText by remember(landScan.southSideM) { mutableStateOf(cleanNum(landScan.southSideM)) }
    var eastText by remember(landScan.eastSideM) { mutableStateOf(cleanNum(landScan.eastSideM)) }
    var westText by remember(landScan.westSideM) { mutableStateOf(cleanNum(landScan.westSideM)) }

    val perimeter = EngineeringCalculator.calculatePerimeter(
        shape = landScan.shape,
        lengthM = landScan.lengthM,
        widthM = landScan.widthM,
        northM = landScan.northSideM,
        southM = landScan.southSideM,
        eastM = landScan.eastSideM,
        westM = landScan.westSideM
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 720.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Photo + Blueprint Survey Overlay
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "تصوير الأرض ومعايرة المساحة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "التقط صورة لقطعة الأرض أو اختر صورة من المعرض لحساب المساحة تلقائياً",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                    ) {
                        if (landScan.photoUri != null) {
                            AsyncImage(
                                model = Uri.parse(landScan.photoUri),
                                contentDescription = "صورة قطعة الأرض",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.img_land_empty),
                                contentDescription = "مخطط قطعة الأرض",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        // Survey boundary grid overlay
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val pad = 26f
                            val bw = size.width - pad * 2
                            val bh = size.height - pad * 2
                            drawRect(
                                color = Color(0xDDF59E0B),
                                topLeft = Offset(pad, pad),
                                size = androidx.compose.ui.geometry.Size(bw, bh),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = 4f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 8f), 0f)
                                )
                            )
                            val corners = listOf(
                                Offset(pad, pad),
                                Offset(pad + bw, pad),
                                Offset(pad, pad + bh),
                                Offset(pad + bw, pad + bh)
                            )
                            corners.forEach { c ->
                                drawCircle(color = Color(0xFFFBBF24), radius = 8f, center = c)
                                drawCircle(color = Color(0xFF0F2744), radius = 4f, center = c)
                            }
                        }

                        // Area Pill
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(10.dp),
                            shape = RoundedCornerShape(50),
                            color = Color(0xE60F2744)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SquareFoot,
                                    contentDescription = null,
                                    tint = Color(0xFFFBBF24),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "المساحة المحسوبة: ${EngineeringCalculator.formatNumber(landScan.estimatedAreaSqm)} م²",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val hasPerm = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasPerm) {
                                    val uri = createTempImageUri(context)
                                    pendingCameraUri = uri
                                    takePictureLauncher.launch(uri)
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("capture_land_photo_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تصوير بالكاميرا")
                        }

                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pick_land_photo_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("من المعرض")
                        }
                    }
                }
            }
        }

        // Shape & Dimensions Calibration Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "معايرة أبعاد وشكل قطعة الأرض",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LandShape.entries.forEach { shape ->
                            FilterChip(
                                selected = landScan.shape == shape,
                                onClick = { onShapeSelected(shape) },
                                label = { Text(shape.arabicName) },
                                modifier = Modifier.testTag("shape_${shape.name}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    when (landScan.shape) {
                        LandShape.RECTANGLE -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = lengthText,
                                    onValueChange = {
                                        lengthText = it
                                        it.toDoubleOrNull()?.let { v ->
                                            onDimensionsChanged(v, null, null, null, null, null)
                                        }
                                    },
                                    label = { Text("الطول (متر)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("land_length_input")
                                )
                                OutlinedTextField(
                                    value = widthText,
                                    onValueChange = {
                                        widthText = it
                                        it.toDoubleOrNull()?.let { v ->
                                            onDimensionsChanged(null, v, null, null, null, null)
                                        }
                                    },
                                    label = { Text("العرض (متر)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("land_width_input")
                                )
                            }
                        }
                        LandShape.SQUARE -> {
                            OutlinedTextField(
                                value = lengthText,
                                onValueChange = {
                                    lengthText = it
                                    it.toDoubleOrNull()?.let { v ->
                                        onDimensionsChanged(v, v, null, null, null, null)
                                    }
                                },
                                label = { Text("طول الضلع (متر)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("land_square_side_input")
                            )
                        }
                        LandShape.TRAPEZOID -> {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = northText,
                                        onValueChange = {
                                            northText = it
                                            it.toDoubleOrNull()?.let { v ->
                                                onDimensionsChanged(null, null, v, null, null, null)
                                            }
                                        },
                                        label = { Text("الضلع الشمالي (م)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = southText,
                                        onValueChange = {
                                            southText = it
                                            it.toDoubleOrNull()?.let { v ->
                                                onDimensionsChanged(null, null, null, v, null, null)
                                            }
                                        },
                                        label = { Text("الضلع الجنوبي (م)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = eastText,
                                        onValueChange = {
                                            eastText = it
                                            it.toDoubleOrNull()?.let { v ->
                                                onDimensionsChanged(null, null, null, null, v, null)
                                            }
                                        },
                                        label = { Text("الضلع الشرقي (م)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = westText,
                                        onValueChange = {
                                            westText = it
                                            it.toDoubleOrNull()?.let { v ->
                                                onDimensionsChanged(null, null, null, null, null, v)
                                            }
                                        },
                                        label = { Text("الضلع الغربي (م)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onRunCalibration,
                        enabled = !isAnalyzing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("analyze_land_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("جارٍ التحليل والمعايرة...")
                        } else {
                            Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تحليل الصورة وحساب المساحة")
                        }
                    }
                }
            }
        }

        // Result & AI Sample Notice
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "المساحة الكلية للأرض",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${EngineeringCalculator.formatNumber(landScan.estimatedAreaSqm)} م²",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "المحيط الخارجي",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${EngineeringCalculator.formatNumber(perimeter)} م.ط",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = landScan.terrainNote,
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (landScan.isSampleAiOutput) {
                        Spacer(modifier = Modifier.height(10.dp))
                        AiFirebaseNoticeBanner()
                    }
                }
            }
        }

        // Next Step CTA
        item {
            Button(
                onClick = onContinueToDesigns,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("continue_to_designs_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text(
                    text = "التالي: نوع المبنى والتصاميم الهندسية",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}

private fun createTempImageUri(context: Context): Uri {
    val file = File(context.cacheDir, "land_capture_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}

private fun cleanNum(value: Double): String {
    return if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
}
