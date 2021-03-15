package ru.dgis.sdk.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/*
Пример демонстрирует реализацию собственного контрола масштаба (кнопки +/-).
Используем для этого готовую ViewModel - ZoomControlModel,
из View сообщаем о нажатиях кнопок - ZoomControlModel.setPressed,
деактивируем кнопки в зависимости от состояния модели - ZoomControlModel.isEnabled
 */
class CustomZoomControlActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_zoom_control)
    }
}
