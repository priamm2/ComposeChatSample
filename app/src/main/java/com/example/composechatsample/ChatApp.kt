package com.example.composechatsample

import android.app.Application
import com.example.composechatsample.core.ToggleService
import com.example.composechatsample.data.UserCredentialsRepository
import com.example.composechatsample.data.PredefinedUserCredentials
import com.example.composechatsample.helper.DateFormatter

class ChatApp : Application() {

    override fun onCreate() {
        super.onCreate()
        credentialsRepository = UserCredentialsRepository(this)
        dateFormatter = DateFormatter.from(this)

        initializeToggleService()
        ChatHelper.initializeSdk(this, getApiKey())
    }

    private fun getApiKey(): String {
        return credentialsRepository.loadApiKey() ?: PredefinedUserCredentials.API_KEY
    }

    private fun initializeToggleService() {
        ToggleService.init(applicationContext)
    }

    companion object {
        lateinit var credentialsRepository: UserCredentialsRepository
            private set

        lateinit var dateFormatter: DateFormatter
            private set

        public const val autoTranslationEnabled: Boolean = true

        public const val isComposerLinkPreviewEnabled: Boolean = true
    }
}