# 2GIS Android Native SDK

2GIS Android Native SDK is an SDK that allows you to add a 2GIS map to your Android application. It can be used to display the map in your layout, add custom markers to it, and highlight various objects on the map, such as buildings, roads, and others.

> :warning: **2GIS Android Native SDK is under development.** It is not ready for production usage.


## Getting API Keys

Usage of this SDK requires an API key to connect to 2GIS servers and retrieve the geographical data. This API key is unique to the SDK and cannot be used with other 2GIS SDKs.

Additionally, if you plan to draw routes on the map, you will need a separate key—a [Directions API](https://docs.2gis.com/en/api/navigation/directions/overview) key—to calculate and display an optimal route.

To obtain either of these API keys, fill in the form at [dev.2gis.ae](https://dev.2gis.ae/order).


## Installation

To install the SDK:

1. Declare a custom repository in your `build.gradle` file:

```gradle
repositories {
    maven {
        url "http://maven.2gis.dev/sdk-maven-release"
    }
}
```

2. Add a build dependency:

```gradle
dependencies {
    implementation 'ru.dgis.sdk:sdk:latest.release'
}
```


## Running Example App

To run the example app, clone this Git repository and add your API keys to the [local.properties](https://developer.android.com/studio/build#properties-files) file in your project:

```
dgisMapApiKey=YOUR_MAP_KEY
dgisDirectoryApiKey=YOUR_DIRECTIONS_KEY
```

## Documentation

Full documentation, including [usage examples](https://docs.2gis.com/en/android/native/maps/examples) and [API reference](https://docs.2gis.com/en/android/native/maps/reference/DGis) with detailed descriptions of all classes and methods, can be found at [docs.2gis.com](https://docs.2gis.com/en/android/native/maps/overview).
