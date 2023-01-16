package ru.dgis.sdk.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.dgis.sdk.Context

class IndoorControlActivity : AppCompatActivity() {
    lateinit var sdkContext: Context
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sdkContext = (applicationContext as Application).sdkContext
        setContentView(R.layout.activity_indoor_control)
    }
}
