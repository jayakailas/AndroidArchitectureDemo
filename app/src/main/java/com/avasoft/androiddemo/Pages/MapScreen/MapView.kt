package com.avasoft.androiddemo.Pages.MapScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState

@Composable
fun MapView(vm: MapVM = viewModel()) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        GoogleMap {

        }
    }
}