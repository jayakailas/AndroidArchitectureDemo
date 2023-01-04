package com.avasoft.androiddemo.Pages.SignUpScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.avasoft.androiddemo.Helpers.UIComponents.FailurePopUp
import com.avasoft.androiddemo.Helpers.UIComponents.Loader
import com.avasoft.androiddemo.Helpers.Utilities.EmailValidator.EmailValidator
import com.avasoft.androiddemo.R

@Composable
fun SignUpView(vm: SignUpVM = viewModel(), login: () -> Unit){
    Box() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            val focusManager = LocalFocusManager.current

            Text(
                stringResource(id = R.string.create_acc_header),
                fontWeight = FontWeight.SemiBold,
                fontSize = 30.sp
            )

            OutlinedTextField(
                value = vm.email,
                onValueChange = {
                    vm.setEmailAddress(it)
                    vm.setIsEmailError(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(56.dp)
                    .onFocusChanged {
                        try {
                            vm.onEmailFocusChange(it.isFocused)
                        } catch (ex: Exception) {
                            // handle exception
                        }
                    },
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.email_address)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        tint = MaterialTheme.colors.onBackground,
                        contentDescription = stringResource(id = R.string.email_icon_desc)
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
                    text = stringResource(id = R.string.email_err),
                    modifier = Modifier.align(Alignment.Start),
                    color = Color.Red
                )
            if (vm.isEmailExist)
                Text(
                    text = stringResource(id = R.string.email_exist),
                    modifier = Modifier.align(Alignment.Start),
                    color = Color.Red
                )

            OutlinedTextField(
                value = vm.password,
                onValueChange = {
                    vm.setPassWord(it)
                    vm.setIsPasswordError(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(56.dp)
                    .onFocusChanged {
                        try {
                            vm.onPasswordFocusChange(it.isFocused)
                        } catch (ex: Exception) {
                            // handle exception
                        }
                    },
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.pwd)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        tint = MaterialTheme.colors.onBackground,
                        contentDescription = stringResource(id = R.string.pwd_icon_desc)
                    )
                },
                trailingIcon = {
                    Icon(
                        painter = if(!vm.passwordVisibility) painterResource(id = R.drawable.ic_outline_visibility_24) else painterResource(id = R.drawable.ic_outline_visibility_off_24),
                        contentDescription = stringResource(id = R.string.pwd_icon_desc),
                        tint = Color.Unspecified,
                        modifier = Modifier.clickable(
                            interactionSource = remember{ MutableInteractionSource() },
                            indication = null
                        ){
                            vm.changePasswordVisibility()
                        }
                    )
                },
                visualTransformation = if(!vm.passwordVisibility) PasswordVisualTransformation() else VisualTransformation.None,
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
                    text = stringResource(id = R.string.pwd_err),
                    modifier = Modifier.align(Alignment.Start),
                    color = Color.Red
                )

            Button(
                onClick = {
                    vm.createClicked{ isSuccess ->
                        if(isSuccess)
                            login()
                    }
                },
                modifier = Modifier
                    .padding(top = 24.dp)
                    .height(48.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.create_acc_header),
                )
            }
        }

        Loader(isVisible = vm.isLoading)

        if(vm.failurePopUp){
            FailurePopUp(label = stringResource(id = R.string.create_acc_failed)) {
                vm.closePopUp()
            }
        }
    }
}