package com.avasoft.androiddemo.Services

enum class ServiceStatus(val statusCode: Int) {
    Success(200),
    Created(201),
    Accepted(202),
    NoContent(204),
    ClientError(400),
    UnAuthorized(401),
    Forbidden(403),
    NotFound(404),
    Conflict(409),
    ServerError(500),
}