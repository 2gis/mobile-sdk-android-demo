# 2GIS Android SDK

2GIS Android SDK is an SDK that allows you to add a [2GIS map](https://2gis.ae/) to your Android application. It can be used to display the map in your layout, add custom markers to it, draw geometric shapes, calculate and display routes, get information about map objects, control the camera movement, and so on.

Geodata complies with [OGC standards](https://en.wikipedia.org/wiki/Open_Geospatial_Consortium).

## Getting API keys

Usage of this SDK requires an API key to connect to 2GIS servers and retrieve the geographical data. This API key is unique to the SDK and cannot be used with other 2GIS SDKs.

Additionally, if you plan to draw routes on the map or get extra information about map objects, you will need a separate key - a *Directory API* key.

To obtain either of these API keys, fill in the form at [dev.2gis.com](https://dev.2gis.com/order).

## Installation

2GIS Android SDK is distributed in two versions: full and lite. The lite version does not include the routes and navigation functionality.

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

## Running the demo app

To run the demo app, clone this Git repository and add your key file `dgissdk.key` in `app/src/main/assets/`.
Also, it is necessary to set `applicationId` of demo app to `app_id` value of your key file.

## Documentation

Full documentation, including [usage examples](https://docs.2gis.com/en/android/sdk/examples) and [API reference](https://docs.2gis.com/en/android/sdk/reference/ru.dgis.sdk.DGis) with detailed descriptions of all classes and methods, can be found at [docs.2gis.com](https://docs.2gis.com/en/android/sdk/overview).

## License

The demo application is licensed under the BSD 2-Clause "Simplified" License. See the [LICENSE](https://github.com/2gis/native-sdk-android-demo/blob/master/LICENSE) file for more information.
