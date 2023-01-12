package com.avasoft.androiddemo.Helpers.UIComponents

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.avasoft.androiddemo.Helpers.RouteConfig.NavRoute
import com.avasoft.androiddemo.Pages.ChatList.ChatListVMFactory
import com.avasoft.androiddemo.Pages.ChatList.ChatListView
import com.avasoft.androiddemo.Pages.LocationConversionScreen.LocationConversionVMFactory
import com.avasoft.androiddemo.Pages.LocationConversionScreen.LocationConversionView
import com.avasoft.androiddemo.Pages.LocationScreen.LocationVMFactory
import com.avasoft.androiddemo.Pages.LocationScreen.LocationView
import com.avasoft.androiddemo.Pages.LoginScreen.LoginVMFactory
import com.avasoft.androiddemo.Pages.LoginScreen.LoginView
import com.avasoft.androiddemo.Pages.MapScreen.MapView
import com.avasoft.androiddemo.Pages.SignUpScreen.SignUpVMFactory
import com.avasoft.androiddemo.Pages.SignUpScreen.SignUpView
import com.avasoft.androiddemo.Pages.MapScreen.MapVMFactory
import com.avasoft.androiddemo.Pages.Room.RoomVMFactory
import com.avasoft.androiddemo.Pages.Room.RoomView
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
                    navController.navigate(NavRoute.SignUp.route){
                        popUpTo(NavRoute.Login.route){
                            inclusive = true
                        }
                    }
                },
                login = {
                    navController.navigate(NavRoute.ChatList.route){
                        popUpTo(NavRoute.Login.route){
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(route = NavRoute.SignUp.route) {
            SignUpView(
                vm = viewModel(
                    factory = SignUpVMFactory(LocalContext.current.applicationContext as Application)
                )
            ){
                navController.navigate(NavRoute.ChatList.route){
                    popUpTo(NavRoute.SignUp.route){
                        inclusive = true
                    }
                }
            }
        }

        composable(route = NavRoute.Location.route) {
            LocationView(
                vm = viewModel(
                    factory = LocationVMFactory(LocalContext.current.applicationContext as Application,
                        userService = userRepository
                    )
                )
            )
        }

        composable(route = NavRoute.LocationConverter.route) {
            LocationConversionView(
                vm = viewModel(
                    factory = LocationConversionVMFactory(
                        app = LocalContext.current.applicationContext as Application,
                        userService = userRepository
                    )
                )
            )
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

        composable(route = NavRoute.ChatList.route) {
            ChatListView(
                NavigateToRoom = { roomId ->
                    navController.navigate(NavRoute.Room.route+"/$roomId")
                },
                vm = viewModel(
                    factory = ChatListVMFactory(
                        app = LocalContext.current.applicationContext as Application
                    )
                )
            )
        }

        composable(route = NavRoute.Room.route+"/{roomId}") {
            val roomId = it.arguments?.getString("roomId")?:""
            RoomView(
                vm = viewModel(
                    factory = RoomVMFactory(
                        roomId = roomId,
                        app = LocalContext.current.applicationContext as Application
                    )
                )
//                vm = viewModel(
//                    factory = ChatListVMFactory(
//                        app = LocalContext.current.applicationContext as Application
//                    )
//                )
            )
        }
    }
}