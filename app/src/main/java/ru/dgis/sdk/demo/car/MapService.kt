package ru.dgis.sdk.demo.car

import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator

class MapService : CarAppService() {
    override fun createHostValidator(): HostValidator {
        // Avoid using ALLOW_ALL_HOSTS_VALIDATOR in production as it is insecure.
        // Refer to the Android Auto documentation for proper security practices.
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
    }

    // In this application ru.dgis.sdk.Context created inside Application's onCreate, hence
    // ru.dgis.sdk.Context is always available here.
    //
    // If use-case demands lazy of delayed initialization of ru.dgis.sdk.Context, i.e.
    // only when certain activity/fragment/etc opened, and it is not created in Application's onCreate,
    // ru.dgis.sdk.Context should be created here in onCreateSession or somewhere earlier.
    override fun onCreateSession(): Session {
        return MapSession()
    }
}
