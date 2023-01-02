package com.avasoft.androiddemo.Helpers.BottomNavigation

import com.avasoft.androiddemo.Helpers.Navigation.NavRoute
import com.avasoft.androiddemo.R

data class BottomNavBarItem(
    val route: String,
    val icon: Int,
    val label: Int
)

val BottomNavItems = listOf(
    BottomNavBarItem(route = NavRoute.Location.route, label = R.string.location, icon = R.drawable.location),
    BottomNavBarItem(route = NavRoute.LocationConverter.route, label = R.string.location_converter, icon = R.drawable.converter),
    BottomNavBarItem(route = NavRoute.Map.route, label = R.string.map, icon = R.drawable.map)
)
