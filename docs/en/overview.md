# Android SDK

Android SDK is an SDK that allows you to add a [2GIS map](https://2gis.ae/) to your Android application. It can be used to display the map in your layout, add custom markers to it, draw geometric shapes, calculate and display routes, get information about map objects, control the camera movement, and so on.

You can find usage examples in the [Examples](/en/android/sdk/examples) section. For a detailed description of all classes and methods, see [API Reference](/en/android/sdk/reference).

Geodata complies with [OGC standards](https://en.wikipedia.org/wiki/Open_Geospatial_Consortium).

## Getting API keys

Usage of this SDK requires an API key to connect to 2GIS servers and retrieve the geographical data. This API key is unique to the SDK and cannot be used with other 2GIS SDKs.

Additionally, if you plan to draw routes on the map or get extra information about map objects, you will need a separate key - a *Directory API* key.

To obtain either of these API keys, fill in the form at [dev.2gis.com](https://dev.2gis.com/order).

## Getting API keys since 4.x version

To work with MobileSDK, you need to get the key file `dgissdk.key` with the obligatory indication of the `appId` of the application for which this key is created. This key will be used to connect to 2GIS servers and retrieve the geographical data, as well as to use offline and the navigator. This key is unique to the SDK and cannot be used with other 2GIS SDKs.

To obtain this key file, fill in the form at [dev.2gis.com](https://dev.2gis.com/order).

The resulting key file must be added to the `assets` of the application.

## Installation

Android SDK is distributed in two versions: full and lite. The lite version does not include the routes and navigation functionality.

To install the SDK:

1. Declare a custom repository in your `build.gradle` file.

```gradle
repositories {
    maven {
        url "http://artifactory.2gis.dev/sdk-maven-release"
    }
}
```

2. Add one of the following build dependencies.

To get the lite version of the SDK:

```gradle
dependencies {
    implementation 'ru.dgis.sdk:sdk-map:latest.release'
}
```

To get the full version of the SDK:

```gradle
dependencies {
    implementation 'ru.dgis.sdk:sdk-full:latest.release'
}
```

## Demo project

You can find a demo app with the source code in our [GitHub repository](https://github.com/2gis/mobile-sdk-android-demo/).
