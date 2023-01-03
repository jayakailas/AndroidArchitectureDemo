package com.avasoft.androiddemo.Pages.MapScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.avasoft.androiddemo.Helpers.UIComponents.Loader
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
                if(vm.currentLat.isNotBlank() &&
                    vm.currentLng.isNotBlank() &&
                    vm.customLat.isNotBlank() &&
                    vm.customLng.isNotBlank()
                ) {
                    Marker(
                        state = MarkerState(
                            LatLng(
                                vm.currentLat.toDouble(),
                                vm.currentLng.toDouble()
                            )
                        )
                    )

                    Marker(
                        state = MarkerState(
                            LatLng(
                                vm.customLat.toDouble(),
                                vm.customLng.toDouble()
                            )
                        )
                    )
                }
            }
        }

        Loader(vm.loadingState)
    }
}