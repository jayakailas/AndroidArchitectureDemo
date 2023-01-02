package com.avasoft.androiddemo.Pages.LoginScreen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Email
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.avasoft.androiddemo.Helpers.UIComponents.Loader
import com.avasoft.androiddemo.Helpers.Utilities.EmailValidator.EmailValidator
import com.avasoft.androiddemo.R

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LoginView(vm: LoginViewModel = viewModel()){
    Box() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            var isLoadDone by remember {
                mutableStateOf(false)
            }

            val focusManager = LocalFocusManager.current

            Text(
                "Login",
                fontWeight = FontWeight.SemiBold,
                fontSize = 30.sp
            )

            OutlinedTextField(
                value = vm.email,
                onValueChange = {
                    vm.setEmailAddress(it)
                    vm.setIsEmailError(it.isBlank())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(56.dp)
                    .onFocusChanged {
                        try {
                            if (it.isFocused) {
                                isLoadDone = true
                            } else {
                                if (isLoadDone) {
                                    vm.setIsEmailError(!EmailValidator.isValidEmail(vm.email))
                                }
                            }
                        } catch (ex: Exception) {
                            // handle exception
                        }
                    },
                placeholder = {
                    Text(
                        text = "Email Address"
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        tint = MaterialTheme.colors.onBackground,
                        contentDescription = "Email Icon"
                    )
                },
                keyboardOptions = KeyboardOptions( imeAction = ImeAction.Done ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                singleLine = true
            )
            if (vm.isEmailError)
                Text(
                    text = "Enter your valid email",
                    modifier = Modifier.align(Alignment.Start),
                    color = Color.Red
                )

            OutlinedTextField(
                value = vm.password,
                onValueChange = {
                    vm.setPassWord(it)
                    vm.setIsPasswordError(it.isBlank())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(56.dp)
                    .onFocusChanged {
                        try {
                            if (it.isFocused) {
                                isLoadDone = true
                            } else {
                                if (isLoadDone)
                                    vm.setIsPasswordError(vm.password.isBlank())
                            }
                        }
                        catch (ex: Exception){
                            // handle exception
                        }
                    },
                placeholder = {
                    Text(
                        text = "Password"
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        tint = MaterialTheme.colors.onBackground,
                        contentDescription = "Password Icon"
                    )
                },
                trailingIcon = {
                    Icon(
                        painter = if(!vm.passwordVisibility) painterResource(id = R.drawable.ic_outline_visibility_24) else painterResource(id = R.drawable.ic_outline_visibility_off_24),
                        contentDescription = "passwd eye",
                        tint = Color.Unspecified,
                        modifier = Modifier.clickable(
                            interactionSource = remember{ MutableInteractionSource() },
                            indication = null
                        ){
                            vm.changePasswordVisibility()
                        }
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                singleLine = true
            )
            if (vm.isPasswordError)
                Text(
                    text = "Enter your valid password",
                    modifier = Modifier.align(Alignment.Start),
                    color = Color.Red
                )

            Button(
                onClick = {
                    vm.setIsEmailError(!EmailValidator.isValidEmail(vm.email))
                    vm.setIsPasswordError(vm.password.isBlank())
                    if(!vm.isEmailError && !vm.isPasswordError){
                        vm.loginClicked()
                    }
                },
                modifier = Modifier
                    .padding(top = 24.dp)
                    .height(48.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Login",
                )
            }
        }

        Loader(isVisible = vm.isLoading)
    }
}