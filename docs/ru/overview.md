# Overview

## Introduction
2GIS Android NativeSDK is an SDK that allows you to add a 2GIS map to your Android application. It can be used to display the map in your layout, add custom markers to it, and highlight various objects on the map, such as buildings, roads, and others.


## Getting an access key

Usage of this SDK requires an API key to connect to 2GIS servers and retrieve the geographical data. This API key is unique to the SDK and cannot be used with other 2GIS SDKs. 

To obtain the key, [contact us](https://dev.2gis.ru/order/).


## Installation

To install the SDK:

1. Declare a custom repository in your build.gradle file:
```gradle
repositories {
    maven {
        url "http://artifactory.2gis.dev/sdk-maven-release"
    }
}
```
2. Add a build dependency:
```gradle
dependencies {
    implementation 'ru.dgis.sdk:sdk-full:latest.release'
}
```

After that, you should be good to go. Check the [Examples section](/ru/android/native/maps/examples) to see how to display the map in your application. Alternatively, check the [API Reference](/ru/android/native/maps/reference/DGis) to learn more about specific classes and methods.
