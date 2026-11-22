package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PriceChange
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.DrawerDestination
import com.example.ui.MainTab

@Composable
fun AiFirebaseNoticeBanner(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = stringResource(id = R.string.ai_firebase_notice),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun BunyanDrawerContent(
    currentDestination: DrawerDestination,
    currentTab: MainTab,
    savedProjectCount: Int,
    onSelectNewProject: () -> Unit,
    onSelectMainTab: (MainTab) -> Unit,
    onSelectDrawerDestination: (DrawerDestination) -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(310.dp)
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            // Brand Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF0D2B47),
                                Color(0xFF1A436D)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_bunyan_logo),
                        contentDescription = stringResource(id = R.string.app_name),
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Column {
                        Text(
                            text = stringResource(id = R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(id = R.string.app_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFD6E4F5)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Actions
            NavigationDrawerItem(
                label = { Text("مشروع هندسي جديد") },
                selected = false,
                onClick = onSelectNewProject,
                icon = { Icon(Icons.Default.AddCircle, contentDescription = null) },
                modifier = Modifier
                    .padding(NavigationDrawerItemDefaults.ItemPadding)
                    .testTag("drawer_new_project")
            )

            NavigationDrawerItem(
                label = { Text("المشاريع المحفوظة ($savedProjectCount)") },
                selected = currentDestination == DrawerDestination.MAIN_WORKFLOW && currentTab == MainTab.HOME,
                onClick = { onSelectMainTab(MainTab.HOME) },
                icon = { Icon(Icons.Default.FolderSpecial, contentDescription = null) },
                modifier = Modifier
                    .padding(NavigationDrawerItemDefaults.ItemPadding)
                    .testTag("drawer_saved_projects")
            )

            NavigationDrawerItem(
                label = { Text("مسح الأرض والتصميم والتكلفة") },
                selected = currentDestination == DrawerDestination.MAIN_WORKFLOW && currentTab != MainTab.HOME,
                onClick = { onSelectMainTab(MainTab.LAND_SCAN) },
                icon = { Icon(Icons.Default.Architecture, contentDescription = null) },
                modifier = Modifier
                    .padding(NavigationDrawerItemDefaults.ItemPadding)
                    .testTag("drawer_workflow")
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp))

            Text(
                text = "الأدوات الهندسية المساعدة",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
            )

            NavigationDrawerItem(
                label = { Text("دليل أسعار المواد الافتراضية") },
                selected = currentDestination == DrawerDestination.DEFAULT_PRICES,
                onClick = { onSelectDrawerDestination(DrawerDestination.DEFAULT_PRICES) },
                icon = { Icon(Icons.Default.PriceChange, contentDescription = null) },
                modifier = Modifier
                    .padding(NavigationDrawerItemDefaults.ItemPadding)
                    .testTag("drawer_price_guide")
            )

            NavigationDrawerItem(
                label = { Text("الحاسبة الهندسية السريعة") },
                selected = currentDestination == DrawerDestination.QUICK_CALCULATOR,
                onClick = { onSelectDrawerDestination(DrawerDestination.QUICK_CALCULATOR) },
                icon = { Icon(Icons.Default.Calculate, contentDescription = null) },
                modifier = Modifier
                    .padding(NavigationDrawerItemDefaults.ItemPadding)
                    .testTag("drawer_quick_calc")
            )

            NavigationDrawerItem(
                label = { Text("عن مصمم التطبيق — م. طه الحدا") },
                selected = currentDestination == DrawerDestination.ABOUT_DESIGNER,
                onClick = { onSelectDrawerDestination(DrawerDestination.ABOUT_DESIGNER) },
                icon = { Icon(Icons.Default.Info, contentDescription = null) },
                modifier = Modifier
                    .padding(NavigationDrawerItemDefaults.ItemPadding)
                    .testTag("drawer_about_designer")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Featured Designer Card inside Sliding Drawer
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onSelectDrawerDestination(DrawerDestination.ABOUT_DESIGNER) }
                    .testTag("drawer_designer_card"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_engineer_avatar),
                        contentDescription = stringResource(id = R.string.designer_name),
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Engineering,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "تصميم وإشراف",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = stringResource(id = R.string.designer_name),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "اضغط لعرض السيرة والنبذة الهندسية",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
