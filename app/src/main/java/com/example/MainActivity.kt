package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Architecture
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.DrawerDestination
import com.example.ui.MainTab
import com.example.ui.MainViewModel
import com.example.ui.components.BunyanDrawerContent
import com.example.ui.screens.AboutDesignerScreen
import com.example.ui.screens.DefaultPricesScreen
import com.example.ui.screens.DesignScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LandScanScreen
import com.example.ui.screens.MaterialsAndCostScreen
import com.example.ui.screens.QuickCalculatorScreen
import com.example.ui.theme.BunyanTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                BunyanTheme {
                    BunyanApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BunyanApp(
    viewModel: MainViewModel = viewModel()
) {
    val project by viewModel.project.collectAsStateWithLifecycle()
    val savedProjects by viewModel.savedProjects.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val drawerDestination by viewModel.drawerDestination.collectAsStateWithLifecycle()
    val isAnalyzingLand by viewModel.isAnalyzingLand.collectAsStateWithLifecycle()
    val isGeneratingDesigns by viewModel.isGeneratingDesigns.collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Handle system Back cleanly when in drawer sub-screens or non-home tabs
    BackHandler(
        enabled = drawerState.isOpen ||
            drawerDestination != DrawerDestination.MAIN_WORKFLOW ||
            selectedTab != MainTab.HOME
    ) {
        when {
            drawerState.isOpen -> scope.launch { drawerState.close() }
            drawerDestination != DrawerDestination.MAIN_WORKFLOW ->
                viewModel.selectTab(selectedTab)
            selectedTab != MainTab.HOME ->
                viewModel.selectTab(MainTab.HOME)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            BunyanDrawerContent(
                currentDestination = drawerDestination,
                currentTab = selectedTab,
                savedProjectCount = savedProjects.size,
                onSelectNewProject = {
                    scope.launch { drawerState.close() }
                    viewModel.startNewProject()
                },
                onSelectMainTab = { tab ->
                    scope.launch { drawerState.close() }
                    viewModel.selectTab(tab)
                },
                onSelectDrawerDestination = { dest ->
                    scope.launch { drawerState.close() }
                    viewModel.openDrawerDestination(dest)
                }
            )
        }
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useNavigationRail = maxWidth >= 600.dp

            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = when (drawerDestination) {
                                    DrawerDestination.ABOUT_DESIGNER -> "عن مصمم التطبيق"
                                    DrawerDestination.DEFAULT_PRICES -> "دليل الأسعار الافتراضية"
                                    DrawerDestination.QUICK_CALCULATOR -> "الحاسبة الهندسية"
                                    DrawerDestination.MAIN_WORKFLOW -> when (selectedTab) {
                                        MainTab.HOME -> stringResource(id = R.string.app_name)
                                        MainTab.LAND_SCAN -> "مسح الأرض وحساب المساحة"
                                        MainTab.DESIGNS -> "المبنى والتصاميم الهندسية"
                                        MainTab.MATERIALS -> "المواد والتكاليف الكاملة"
                                    }
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = { scope.launch { drawerState.open() } },
                                modifier = Modifier.testTag("open_drawer_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = stringResource(id = R.string.drawer_open_desc)
                                )
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = {
                                    viewModel.openDrawerDestination(DrawerDestination.ABOUT_DESIGNER)
                                },
                                modifier = Modifier.testTag("topbar_about_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "عن المهندس طه الحدا"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                },
                bottomBar = {
                    if (!useNavigationRail && drawerDestination == DrawerDestination.MAIN_WORKFLOW) {
                        NavigationBar {
                            MainTab.entries.forEach { tab ->
                                val selected = selectedTab == tab
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { viewModel.selectTab(tab) },
                                    icon = {
                                        Icon(
                                            imageVector = tabIcon(tab, selected),
                                            contentDescription = tab.arabicTitle
                                        )
                                    },
                                    label = { Text(tab.arabicTitle) },
                                    modifier = Modifier.testTag("bottom_tab_${tab.name}")
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useNavigationRail && drawerDestination == DrawerDestination.MAIN_WORKFLOW) {
                        NavigationRail {
                            MainTab.entries.forEach { tab ->
                                val selected = selectedTab == tab
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { viewModel.selectTab(tab) },
                                    icon = {
                                        Icon(
                                            imageVector = tabIcon(tab, selected),
                                            contentDescription = tab.arabicTitle
                                        )
                                    },
                                    label = { Text(tab.arabicTitle) },
                                    modifier = Modifier.testTag("rail_tab_${tab.name}")
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        AnimatedContent(
                            targetState = drawerDestination to selectedTab,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(220)) togetherWith
                                    fadeOut(animationSpec = tween(180))
                            },
                            label = "main_screen_transition"
                        ) { (dest, tab) ->
                            when (dest) {
                                DrawerDestination.ABOUT_DESIGNER -> {
                                    AboutDesignerScreen(
                                        onBackToWorkflow = { viewModel.selectTab(tab) }
                                    )
                                }
                                DrawerDestination.DEFAULT_PRICES -> {
                                    DefaultPricesScreen(
                                        materials = project.materials,
                                        currencySymbol = project.currency.symbol,
                                        onUpdateUnitPrice = viewModel::updateDefaultPriceUsd,
                                        onBackToWorkflow = { viewModel.selectTab(tab) }
                                    )
                                }
                                DrawerDestination.QUICK_CALCULATOR -> {
                                    QuickCalculatorScreen(
                                        onBackToWorkflow = { viewModel.selectTab(tab) }
                                    )
                                }
                                DrawerDestination.MAIN_WORKFLOW -> when (tab) {
                                    MainTab.HOME -> HomeScreen(
                                        activeProject = project,
                                        savedProjects = savedProjects,
                                        onStartNewProject = viewModel::startNewProject,
                                        onNavigateTab = viewModel::selectTab,
                                        onLoadProject = viewModel::loadProject,
                                        onDeleteProject = viewModel::deleteProject,
                                        onOpenAboutDesigner = {
                                            viewModel.openDrawerDestination(DrawerDestination.ABOUT_DESIGNER)
                                        }
                                    )
                                    MainTab.LAND_SCAN -> LandScanScreen(
                                        landScan = project.landScan,
                                        isAnalyzing = isAnalyzingLand,
                                        onPhotoSelected = viewModel::onLandPhotoSelected,
                                        onShapeSelected = { shape ->
                                            viewModel.updateLandDimensions(shape = shape)
                                        },
                                        onDimensionsChanged = { l, w, n, s, e, wst ->
                                            viewModel.updateLandDimensions(
                                                lengthM = l,
                                                widthM = w,
                                                northM = n,
                                                southM = s,
                                                eastM = e,
                                                westM = wst
                                            )
                                        },
                                        onRunCalibration = viewModel::runLandCalibrationEstimate,
                                        onContinueToDesigns = { viewModel.selectTab(MainTab.DESIGNS) },
                                        onShowMessage = { msg ->
                                            scope.launch { snackbarHostState.showSnackbar(msg) }
                                        }
                                    )
                                    MainTab.DESIGNS -> DesignScreen(
                                        project = project,
                                        isGenerating = isGeneratingDesigns,
                                        onSelectBuildingType = viewModel::selectBuildingType,
                                        onSelectConstructionType = viewModel::selectConstructionType,
                                        onUpdateFloors = viewModel::updateFloors,
                                        onGenerateDesigns = viewModel::generateArchitecturalDesigns,
                                        onSelectDesign = viewModel::selectDesign,
                                        onContinueToMaterials = { viewModel.selectTab(MainTab.MATERIALS) }
                                    )
                                    MainTab.MATERIALS -> MaterialsAndCostScreen(
                                        project = project,
                                        onUpdateProjectName = viewModel::updateProjectName,
                                        onChangeCurrency = viewModel::changeCurrency,
                                        onUpdateMaterialGrade = viewModel::updateMaterialGrade,
                                        onUpdateMaterialQuantity = viewModel::updateMaterialQuantity,
                                        onUpdateMaterialUnitPrice = viewModel::updateMaterialUnitPrice,
                                        onToggleMaterialIncluded = viewModel::toggleMaterialIncluded,
                                        onAddCustomMaterial = viewModel::addCustomMaterial,
                                        onSaveProject = viewModel::saveCurrentProject,
                                        buildShareText = viewModel::buildShareReportText
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun tabIcon(tab: MainTab, selected: Boolean) = when (tab) {
    MainTab.HOME -> if (selected) Icons.Filled.Home else Icons.Outlined.Home
    MainTab.LAND_SCAN -> if (selected) Icons.Filled.CameraAlt else Icons.Outlined.CameraAlt
    MainTab.DESIGNS -> if (selected) Icons.Filled.Architecture else Icons.Outlined.Architecture
    MainTab.MATERIALS -> if (selected) Icons.Filled.Calculate else Icons.Outlined.Calculate
}
