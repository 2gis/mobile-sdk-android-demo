# 2GIS NativeSDK Android Demo

> :warning: **2GIS NativeSDK is under development.** It is not ready for production usage.

Full documentation, including more usage examples and detailed descriptions of all classes and methods, can be found at [https://docs.2gis.com/ru/android/native/maps/overview](https://docs.2gis.com/ru/android/native/maps/overview).


## Getting an access key

Usage of this SDK requires an API key to connect to 2GIS servers and retrieve the geographical data. This API key is unique to the SDK and cannot be used with other 2GIS SDKs.

To obtain the key, contact us at [https://dev.2gis.ru/order/](https://dev.2gis.ru/order/).


## Installation

To install the SDK:

1. Declare a custom repository in your _build.gradle_ file:

```gradle
repositories {
    maven {
        url "http://maven.2gis.dev/libs-release"
    }
}
```

2. Add a build dependency:

```gradle
dependencies {
    implementation 'ru.dgis.sdk:sdk:latest.release'
}
```


## Usage

To run the example app, first add your API key to the [local.properties](https://developer.android.com/studio/build#properties-files) file in your project:

```
dgisMapApiKey=MAP_API_KEY
dgisDirectoryApiKey=DIRECTORY_API_KEY
```

You can find more usage examples at [https://docs.2gis.com/ru/android/native/maps/examples/unstable](https://docs.2gis.com/ru/android/native/maps/examples/unstable).
