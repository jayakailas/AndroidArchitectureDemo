package com.avasoft.androiddemo.Pages.LocationScreen

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.avasoft.androiddemo.Helpers.UIComponents.FailurePopUp
import com.avasoft.androiddemo.Helpers.UIComponents.Loader
import com.avasoft.androiddemo.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationView(vm: LocationVM = viewModel()){

    vm.scope = rememberCoroutineScope()

    vm.gpsLaunchRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = vm::gpsLaunchCallback
    )

    vm.locationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
        onPermissionsResult = vm::locationPermissionCallback
    )

    val loaderState by vm.isLoading.observeAsState(initial = false)

    Box(){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Location",
                fontWeight = FontWeight.SemiBold,
                fontSize = 30.sp
            )

            Row(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    "Latitude: ${vm.currentLat}"
                )

                Text(
                    "Longitude:${vm.currentLong}"
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp)
            ) {
                Text(
                    "Address: ",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )

                Text(
                    vm.currentAddress,
                )
            }

            OutlinedTextField(
                value = vm.customLat,
                onValueChange = {
                    Log.d("lat", it.toDoubleOrNull().toString())
                    if (it.isEmpty()){
                        vm.customLat = it
                    } else {
                        vm.setUserCustomLat(it)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(56.dp),
                placeholder = {
                    Text(
                        text = "Enter Latitude"
                    )
                }
            )

            OutlinedTextField(
                value = vm.customLong,
                onValueChange = {
                    if (it.isEmpty()){
                        vm.customLong = it
                    } else {
                        vm.setUserCustomLong(it)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(56.dp),
                placeholder = {
                    Text(
                        text = "Enter Longitude"
                    )
                }
            )

            Button(
                onClick = {
                    vm.calculateDistanceClicked()
                }
            ) {
                Text(
                    "Calculate Distance"
                )
            }

            Text(
                "Distance: ${vm.distance} kms"
            )
        }
        
        Loader(isVisible = loaderState)

        if(vm.failurePopUp){
            FailurePopUp(label = stringResource(id = R.string.err_occurred)) {
                vm.closePopUp()
            }
        }
    }
}