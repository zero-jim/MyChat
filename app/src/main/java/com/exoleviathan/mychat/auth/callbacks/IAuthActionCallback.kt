package com.exoleviathan.mychat.auth.callbacks

interface IAuthActionCallback {
    fun onSuccess()
    fun onError(msg: String?, msgDetails: String?)
}