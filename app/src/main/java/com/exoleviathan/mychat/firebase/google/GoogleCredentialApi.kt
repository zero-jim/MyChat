package com.exoleviathan.mychat.firebase.google

import android.content.Context

interface GoogleCredentialApi {
    @Throws(Exception::class)
    suspend fun getServerAccessToken(context: Context, tokenValue: (String?) -> Unit)
}