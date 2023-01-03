package com.avasoft.androiddemo.Pages.LocationConversionScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.avasoft.androiddemo.Helpers.AppConstants.unitsList
import com.avasoft.androiddemo.Helpers.UIComponents.FailurePopUp
import com.avasoft.androiddemo.R

@Composable
fun LocationConversionView(vm: LocationConversionVM = viewModel()){

    LaunchedEffect(key1 = Unit){
        vm.pageLoad()
    }

    if(vm.distanceToShow.isNotBlank()){
        Box() {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Distance Conversion",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 30.sp
                )
                Text(
                    "Distance: ${vm.distanceToShow}"
                )


                var selectedItem by remember { mutableStateOf(unitsList[0].name) }
                var isExpanded by remember { mutableStateOf(false) }

                Box(modifier = Modifier) {

                    var size by remember { mutableStateOf(IntSize.Zero) }

                    Box(
                        modifier = Modifier
                            .height(60.dp)
                            .fillMaxWidth()
                            .onSizeChanged {
                                size = it
                            }
                    ) {
                        OutlinedTextField(modifier = Modifier
                            .height(60.dp)
                            .fillMaxWidth(),
                            value = selectedItem,
                            onValueChange = {},
                            label = {
                                Text(
                                    text = "Unit"
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_outline_keyboard_arrow_down_24),
                                    contentDescription = "Drop down arrow",
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .height(24.dp)
                                        .width(24.dp)
                                )
                            }
                        )
                        val interactionSource = remember { MutableInteractionSource() }
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Transparent)
                            .clickable(interactionSource = interactionSource, indication = null) {
                                isExpanded = !isExpanded
                            }) {
                        }
                    }

                    val dropdownHeight = if(unitsList.size < 5) ((unitsList.size *48)+16).dp else 256.dp
                    DropdownMenu(expanded = isExpanded,
                        onDismissRequest = { isExpanded = false },
                        modifier = Modifier.then(
                            with(LocalDensity.current) {
                                Modifier.size(
                                    width = size.width.toDp(),
                                    height = dropdownHeight,
                                )
                            }
                        )
                    ) {
                        unitsList.forEach {
                            DropdownMenuItem(onClick = {
                                selectedItem = it.name
                                vm.convert(it)
                                isExpanded = false
                            },
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text(text = it.name)
                            }
                        }
                    }
                }
            }

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
                "Distance is not yet calculated, please go to location tab and calculate distance.",
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}