package com.avasoft.androiddemo.Helpers.Navigation

import android.app.Application
import android.util.Log
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
import com.avasoft.androiddemo.Pages.MapScreen.MapVMFactory
import com.avasoft.androiddemo.Services.DemoDatabase
import com.avasoft.androiddemo.Services.UserService.LocalUserService

@Composable
fun NavigationComposable(navController: NavHostController, modifier: Modifier) {
    val databaseInstance = DemoDatabase.getInstance(LocalContext.current.applicationContext)
    val userDao = databaseInstance.userDao()
    val userRepository = LocalUserService(userDao)

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
                    navController.navigate(NavRoute.Map.route)
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
            MapView(
                vm = viewModel(
                    factory = MapVMFactory(
                        app = LocalContext.current.applicationContext as Application,
                        repository = userRepository
                    )
                )
            )
        }
    }
}