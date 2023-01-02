package com.avasoft.androiddemo.Pages.MapScreen

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class MapVM: ViewModel() {

    var loadingState = mutableStateOf(false)
}
