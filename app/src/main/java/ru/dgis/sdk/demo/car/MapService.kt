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

    override fun onCreateSession(): Session {
        return MapSession()
    }
}
