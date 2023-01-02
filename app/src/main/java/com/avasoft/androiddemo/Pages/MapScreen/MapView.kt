package com.avasoft.androiddemo.Pages.MapScreen

import androidx.compose.foundation.layout.Box
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
fun MapView(vm: MapVM) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            GoogleMap {
//                Marker(
//                    state = MarkerState(
//                        LatLng(
//                            vm.currentLat.toDouble(),
//                            vm.currentLng.toDouble()
//                        )
//                    )
//                )
//
//                Marker(
//                    state = MarkerState(
//                        LatLng(
//                            vm.customLat.toDouble(),
//                            vm.customLng.toDouble()
//                        )
//                    )
//                )
            }
        }

        if(vm.loadingState) {

        }
    }
}