package com.avasoft.androiddemo.Pages.MapScreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.avasoft.androiddemo.Services.UserService.LocalUserService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapVM(repository: LocalUserService): ViewModel() {

    var loadingState by mutableStateOf(false)
    var email by mutableStateOf("")
    var currentLat by mutableStateOf("")
    var currentLng by mutableStateOf("")
    var customLat by mutableStateOf("")
    var customLng by mutableStateOf("")


    init {
        viewModelScope.launch(Dispatchers.IO) {
//            val result = repository.checkUserAlreadyExists(email = email)
//            currentLat = result.content?.currentLat?:""
//            currentLng = result.content?.currentLong?:""
//            customLat = result.content?.customLat?:""
//            customLng = result.content?.customLong?:""
        }
    }


}

class MapVMFactory(val repository: LocalUserService): ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MapVM(repository) as T
    }
}
