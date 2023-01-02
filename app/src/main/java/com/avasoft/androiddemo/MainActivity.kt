package com.avasoft.androiddemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.avasoft.androiddemo.Helpers.BottomNavigation.BottomNavBar
import com.avasoft.androiddemo.Helpers.Navigation.NavRoute
import com.avasoft.androiddemo.Helpers.Navigation.NavigationComposable
import com.avasoft.androiddemo.ui.theme.AndroidDemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidDemoTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    var bottomBarState by rememberSaveable { (mutableStateOf(true)) }
                    val navController = rememberNavController()

                    val navBackStackEntry by navController.currentBackStackEntryAsState()

                    bottomBarState = when (navBackStackEntry?.destination?.route) {
                        NavRoute.SignUp.route -> {
                            false
                        }

                        NavRoute.Login.route -> {
                            false
                        }

                        else -> {
                            true
                        }
                    }
                    Scaffold(
                        bottomBar = {
                            BottomNavBar(bottomBarState = bottomBarState, navController = navController)
                        }
                    ) {
                        NavigationComposable(navController, Modifier.padding(it))
                    }
                }
            }
        }
    }
}