package com.avasoft.androiddemo.Services

data class ServiceResult<T>(
    val status: ServiceStatus,
    val message: String?,
    val content: T?
)