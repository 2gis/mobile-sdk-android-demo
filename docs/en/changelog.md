# Release notes


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

