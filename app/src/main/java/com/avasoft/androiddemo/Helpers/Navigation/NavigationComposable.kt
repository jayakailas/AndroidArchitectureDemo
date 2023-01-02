package com.avasoft.androiddemo.Helpers.Navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.avasoft.androiddemo.Pages.LoginScreen.LoginVMFactory
import com.avasoft.androiddemo.Pages.LoginScreen.LoginView
import com.avasoft.androiddemo.Pages.MapScreen.MapView
import com.avasoft.androiddemo.Pages.SignUpScreen.SignUpView

@Composable
fun NavigationComposable(navController: NavHostController, modifier: Modifier) {
    NavHost(
        navController = navController,
        startDestination = NavRoute.Login.route,
        modifier = modifier
    ) {
        composable(route = NavRoute.Login.route) {
            LoginView(
                vm = viewModel(
                    factory = LoginVMFactory(LocalContext.current.applicationContext as Application)
                ),
                navigateToSignUp = {
                    navController.navigate(NavRoute.SignUp.route)
                },
                login = { email ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("userEmail", email)
                    navController.navigate(NavRoute.Location.route + "/$email")
                }
            )
        }

        composable(route = NavRoute.SignUp.route) {
            SignUpView()
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