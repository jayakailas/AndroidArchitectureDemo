package com.avasoft.androiddemo.Pages.MapScreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.avasoft.androiddemo.Helpers.UIComponents.FailurePopUp
import com.avasoft.androiddemo.Helpers.UIComponents.Loader
import com.avasoft.androiddemo.R
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun MapView(vm: MapVM) {

    LaunchedEffect(key1 = Unit){
        vm.pageLoad()
    }

    if(vm.currentLat != 0.0 &&
        vm.currentLng!= 0.0 &&
        vm.customLat!= 0.0 &&
        vm.customLng!= 0.0
    ){
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                GoogleMap(
                    cameraPositionState = CameraPositionState(
                        CameraPosition.Builder().apply {
                            target(LatLng(vm.currentLat, vm.currentLng))
                            zoom(12f)
                            tilt(40f)
                        }.build()
                    )
                ) {
                    Polyline(
                        points= vm.coordinatesList.toList(),
                        jointType= JointType.ROUND,
                        color = Color.Blue
                    )

                    Marker(
                        state = MarkerState(
                            LatLng(
                                vm.currentLat,
                                vm.currentLng
                            )
                        )
                    )

                    Marker(
                        state = MarkerState(
                            LatLng(
                                vm.customLat,
                                vm.customLng
                            )
                        )
                    )
                }
            }
            Loader(vm.loadingState)

            if(vm.failurePopUp){
                FailurePopUp(label = stringResource(id = R.string.err_occurred)) {
                    vm.closePopUp()
                }
            }
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
                stringResource(id = R.string.map_alert),
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}