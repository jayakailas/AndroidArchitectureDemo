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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.avasoft.androiddemo.Helpers.UIComponents.Loader
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

            OutlinedTextField(
                value = vm.customLat,
                onValueChange = {
                    Log.d("lat", it.toDoubleOrNull().toString())
                    if (it.isEmpty()){
                        vm.customLat = it
                    } else {
                        vm.customLat = when (it.toDoubleOrNull()) {
                            null -> vm.customLat //old value
                            else -> it   //new value
                        }
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
                        vm.customLong = when (it.toDoubleOrNull()) {
                            null -> vm.customLong //old value
                            else -> it   //new value
                        }
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

                }
            ) {
                Text(
                    "Calculate Distance"
                )
            }

            Text(
                "Distance:"
            )
        }
        
        Loader(isVisible = vm.isLoading.value!!)
    }
}