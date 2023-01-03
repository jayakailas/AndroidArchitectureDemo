package com.avasoft.androiddemo.Helpers.AppConstants

import androidx.lifecycle.MutableLiveData
import com.avasoft.androiddemo.Helpers.RouteConfig.NavRoute

object GlobalConstants {
    const val USER_SHAREDPREFERENCE = "userSP"
    const val USER_EMAIL = "user"

    val currentTab = MutableLiveData(NavRoute.Location.route)
}