package com.avasoft.androiddemo.Helpers.Navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.avasoft.androiddemo.Pages.MapScreen.MapView

@Composable
fun NavigationComposable(navController: NavHostController, modifier: Modifier) {
    NavHost(
        navController = navController,
        startDestination = NavRoute.Login.route,
        modifier = modifier
    ) {
        composable(route = NavRoute.Login.route) {

        }

        composable(route = NavRoute.SignUp.route) {

        }

        composable(route = NavRoute.Location.route) {

        }

        composable(route = NavRoute.LocationConverter.route) {

        }

        composable(route = NavRoute.Map.route) {
            MapView()
        }
    }
}