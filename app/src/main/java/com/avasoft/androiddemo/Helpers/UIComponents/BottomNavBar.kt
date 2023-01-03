package com.avasoft.androiddemo.Helpers.BottomNavigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.avasoft.androiddemo.Helpers.AppConstants.GlobalConstants
import com.avasoft.androiddemo.Helpers.RouteConfig.BottomNavItems
import com.avasoft.androiddemo.Helpers.RouteConfig.NavRoute

@Composable
fun BottomNavBar(bottomBarState: Boolean, navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentTab by GlobalConstants.currentTab.observeAsState(initial = NavRoute.Location.route)
    val userEmail = navBackStackEntry?.savedStateHandle?.get<String>("userEmail")

    AnimatedVisibility(
        visible = bottomBarState,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
    ) {
        BottomNavigation() {
            BottomNavItems.forEach { item ->
                BottomNavigationItem(
                    selected = item.route == currentTab,
                    onClick = {
                        val popUptoRoute  = currentTab
                        GlobalConstants.currentTab.postValue(item.route)
                        navController.navigate(item.route) {
                            popUpTo(popUptoRoute) {
                                this.inclusive = true
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            painter = painterResource(id = item.icon),
                            contentDescription = "icon"
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(id = item.label)
                        )
                    }
                )
            }
        }
    }
}