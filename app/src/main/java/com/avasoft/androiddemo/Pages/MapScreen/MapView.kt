package com.avasoft.androiddemo.Pages.MapScreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.avasoft.androiddemo.Helpers.UIComponents.Loader
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState

@Composable
fun MapView(vm: MapVM) {

    LaunchedEffect(key1 = Unit){
        vm.pageLoad()
    }

    if(vm.currentLat.isNotBlank() &&
        vm.currentLng.isNotBlank() &&
        vm.customLat.isNotBlank() &&
        vm.customLng.isNotBlank()
    ){
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                GoogleMap {
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
            Loader(vm.loadingState)
        }
    }
    else{
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(
                "Save coordinates in Location Tab",
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}