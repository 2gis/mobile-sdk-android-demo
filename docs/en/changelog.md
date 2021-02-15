# Release notes


## v0.6.0
**Release Date:** 15.02.2021
- разделили SDK на 2 артефакта. **sdk-map** - версия для тех кому нужна только карта и справочник. **sdk-full** - более полная, содержит в себе навигатор. В *build.gradle* необходимо указать нужную версию(напр. `implementation 'ru.dgis.sdk:sdk-map:0.6.0'`)
- карта и контролы теперь доступны и в темной теме
- [добавили уровень логирования](/ru/android/native/maps/reference/ru.dgis.sdk.DGis#nav-lvl1--initialize). По умолчанию SDK пишет только Warning и более критичные сообщения
- методы для вычисления [позиции камеры](/ru/android/native/maps/reference/ru.dgis.sdk.map.calcPosition) и [Zoom Level](/ru/android/native/maps/reference/ru.dgis.sdk.map.zoomOutToFit) по заданной геометрии
- [источник для данных с кластеризацией](/ru/android/native/maps/reference/ru.dgis.sdk.map.GeometryMapObjectSourceBuilder#nav-lvl1--createSourceWithClustering)
- добавили возможность задать [координату с высотой для маркера](/ru/android/native/maps/reference/ru.dgis.sdk.map.MarkerBuilder#nav-lvl1--setPosition)
- для динамических объектов на карте, добавили возможность [определить пользовательские данные](/ru/android/native/maps/reference/ru.dgis.sdk.map.MarkerBuilder#nav-lvl1--setUserData)
- поддержали создание маркера с текстом, без иконки
- *Ломающее изменение:* иконку маркера необходимо указывать через [объект стилей](/ru/android/native/maps/reference/ru.dgis.sdk.map.MarkerBuilder#nav-lvl1--setStyle). Формирование стиля маркера с иконкой может занимать значительное время т.к. для этого может понадобится растеризация/сжатие изображения. Мы настоятельно рекомендуем делать это в фоновом потоке и переиспользовать MarkerStyle для однотипных маркеров.


## v0.5.0
**Release Date:** 04.02.2021
- исправлен баг с ключами API(проявлялось как 403 от сервера тайлов)
- звуковое оповещение о превышении скорости в навигаторе
- получение пробочного балла в [TrafficManager](/ru/android/native/maps/reference/ru.dgis.sdk.traffic.TrafficManager)
- landscape Ui в навигаторе


## v0.4.6
**Release Date:** 26.01.2021
- приглушение других звуков при проигрывании инструкций навигатора
- редизайн Ui навигатора 
- исправление ошибок


## v0.4.5
**Release Date:** 20.01.2021
- вернули *map.camera* 


## v0.4.4
**Release Date:** 19.01.2021
- *ViewportPoint* переименован в *ScreenPoint*, *ViewportSize* -> *ScreenSize*
- фикс слоя для маркеров
- добавлен [GestureManager](/ru/android/native/maps/reference/GestureManager)
- добавили возможность отменять [Future](/ru/android/native/maps/reference/Future#nav-lvl1--cancel)
- изменили работу с **Any** типами (см. [пример](/ru/android/native/maps/reference/SuggestHandler#nav-lvl1--match))


## v0.4.3
**Release Date:** 22.12.2020
- режим слежения за маркером местоположения
- поддержка offline данных в downloads директории
- воспроизведение голосовых инструкций с учетом скорости на маршруте
- [searchById](/ru/android/native/maps/reference/SearchManager#nav-lvl1--searchById) в модуле справочника
- добавили свойство [renderView](/ru/android/native/maps/reference/MapView#nav-lvl1--renderView) у объекта MapView


## v0.4.2
**Release Date:** 15.12.2020
- голосовые инструкции в навигаторе во время ведения по маршруту
- поддержали жест наклона карты


## v0.4.1
**Release Date:** 09.12.2020
- добавлен новый [DgisSource](/ru/android/native/maps/reference/DgisSource). Для работы c объектами 2GIS
- *Ломающее изменение:* DgisSourceCreator был удален. Вместо него стоит использовать статические методы [DgisSource](/ru/android/native/maps/reference/DgisSource)
- в [RouteParams](/ru/android/native/maps/reference/RouteParams) добавлена поддержка промежуточных точек


## v0.4.0
**Release Date:** 03.12.2020
- уменьшили размер библиотеки
- UI контролы карты и навигатора
- баблики с дополнительной информацией по найденному маршруту


## v0.3.3
**Release Date:** 24.11.2020
- [добавили возможность делать подпись к маркерам](/ru/android/native/maps/reference/MarkerBuilder#nav-lvl2--setText)
- [для объектов справочника доступны дополнительные атрибуты](/ru/android/native/maps/reference/DirectoryObject#nav-lvl2--attributes)
- *Ломающее изменение:* необходимо отказаться от методов tryCastTo* в пользу обычного приведения типов
- уменьшили размер зашитых в библиотеку данных


## v0.3.2
**Release Date:** 17.11.2020
- данные, необходимые для инициализации SDK, теперь зашиты в пакет. Больше нет необходимости в предустановленных данных
- отображение поисковой выдачи с генерализацией как в мобильном 2GIS
- работа с FollowManager. Позволяет следить за точкой на карте
- для карты Online Data Source выбирается по умолчанию
- научили наш 3d engine работать с SVG
- в стриме событий навигатора добавилась координата следующего маневра


## v0.3.1
**Release Date:** 09.11.2020
- поддержка drag для объектов карты
- в RouteEditor теперь можно выбирать маршрут по клику в него
- "съедание" маршрута при запуске навигатора
- исправления в расчете ETA


## v0.3.0
**Release Date:** 02.11.2020
- [добавлены методы отображения маркеров на карте](/ru/android/native/maps/examples#nav-lvl1--Добавление_маркера_на_карту)
- [оповещение о маневрах, камерах, улице, времени до конца маршрута и т. д.](/ru/android/native/maps/examples#nav-lvl1--События_по_маршруту_во_время_ведения)
- [получение информации об объектах по клику в карту](/ru/android/native/maps/examples#nav-lvl1--Получение_информации_по_клику_в_карту)


## v0.2.2
**Release Date:** 27.10.2020
- исправлен баг c *ClassLoader* при использовании *sharedUserId*
- в найденном маршруте можно получить расстояние в метрах через `route.length`
- добавлен обработчик для уменьшения потребления памяти
- в `MapView` добавлена работа с `TouchEventsObserver`


## v0.2.1
**Release Date:** 20.10.2020
- [отображение маркера текущего местоположения](/ru/android/native/maps/examples#nav-lvl1--Отображение_маркера_текущего_местоположения)
- добавили использование API key
- починили работу https
- изменен интерфейс NavigationManager


## v0.2.0
**Release Date:** 08.10.2020
- [поддержали добавление кастомного источника геопозиции](/ru/android/native/maps/examples#nav-lvl1--Создание_и_использование_собственного_источника_позиции)
- [работа с объектами из GeoJSON](/ru/android/native/maps/examples#nav-lvl1--%D0%94%D0%BE%D0%B1%D0%B0%D0%B2%D0%BB%D0%B5%D0%BD%D0%B8%D0%B5_%D0%BE%D0%B1%D1%8A%D0%B5%D0%BA%D1%82%D0%BE%D0%B2_%D0%B8%D0%B7_GeoJson)
- [поиск о отображение маршрута на карте](/ru/android/native/maps/examples#nav-lvl1--Построение_маршрута_и_его_отображение_на_карте)


## v0.1.2
**Release Date:** 22.09.2020
- добавление кастомных геометрий

подробнее: https://telegra.ph/NativeSDK-012-09-22


## v0.1.1
**Release Date:** 15.09.2020
- темная тема карты

подробнее: https://telegra.ph/NativeSDK-011-09-15


## v0.1.0
**Release Date:** 08.09.2020
- online карта
- изменения в API для инициализации SDK
- гибридный справочник(online + offline)

подробнее: https://telegra.ph/NativeSDK-010-09-08


## v0.0.1
**Release Date:** 21.08.2020
- online справочник

подробнее: https://telegra.ph/NativeSDK-001-08-20

