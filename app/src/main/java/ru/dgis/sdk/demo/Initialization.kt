package ru.dgis.sdk.demo

import ru.dgis.sdk.Context
import ru.dgis.sdk.DGis
import ru.dgis.sdk.platform.LogLevel
import ru.dgis.sdk.platform.LogOptions
import ru.dgis.sdk.platform.VendorConfig
import ru.dgis.sdk.platform.VendorConfigFromString

fun initializeDGis(appContext: android.content.Context): Context {
    val vendorConfig = VendorConfig(
        fromString = VendorConfigFromString(
            "[\n" +
                    "  {\n" +
                    "    \"name\": \"Mobile SDK for Balady App Momrah Production overrides\",\n" +
                    "    \"comment\": \"\",\n" +
                    "    \"overrides\": {\n" +
                    "      \"dgis/native-sdk/keys/user_web_api_key_for_on_premise\": \"28ed92f1-de4b-4060-b159-2dc19e9b8bf5\",\n" +
                    "      \"dgis/native-sdk/map/tileserver_url\": \"https://urbi-lifestyle.momrah.gov.sa\",\n" +
                    "      \"dgis/native-sdk/map/tileserver_tileset\": \"native\",\n" +
                    "      \"dgis/native-sdk/map/OnlineIndoorBuildingFetcher/indoor_base_url\": \"https://urbi-lifestyle.momrah.gov.sa\",\n" +
                    "      \"Modules/directory/webapi_url\": \"https://urbi-lifestyle.momrah.gov.sa\",\n" +
                    "      \"dgis/native-sdk/directory/webapi_url\": \"https://urbi-lifestyle.momrah.gov.sa\",\n" +
                    "      \"dgis/native-sdk/routing/car_routing_server_url\": \"https://urbi-lifestyle.momrah.gov.sa\",\n" +
                    "      \"Modules/traffic/regions/regions_base_webapi_url\": \"https://urbi-lifestyle.momrah.gov.sa/2.0/region\",\n" +
                    "      \"Modules/twins/webapi_url\": \"https://urbi-lifestyle.momrah.gov.sa/v1/twins/search/dgis\",\n" +
                    "      \"bss/server_url\": \"https://s1.bss.urbi.ae/bss/3\",\n" +
                    "      \"Modules/traffic/score_base_webapi_url\": \"https://jam.api.urbi.ae\",\n" +
                    "      \"Modules/traffic/TrafficCollector/server_url\": \"https://tjam.mis.urbi.ae/traffic-jam/0\",\n" +
                    "      \"dgis/native-sdk/keys/server_url\": \"https://keys.api.urbi.ae\",\n" +
                    "      \"dgis/native-sdk/road_events/server_url\": \"https://tugc.urbi.ae/\",\n" +
                    "      \"LegacyApplicationSettings/NetworkModule/StateServerUrl1\": \"https://nsdk-s01.uss.urbi.ae/ver4/\",\n" +
                    "      \"LegacyApplicationSettings/NetworkModule/StateServerUrl2\": \"https://nsdk-s02.uss.urbi.ae/ver4/\",\n" +
                    "      \"LegacyApplicationSettings/NetworkModule/StateServerUrl3\": \"https://nsdk-s03.uss.urbi.ae/ver4/\",\n" +
                    "      \"LegacyApplicationSettings/NetworkModule/StateServerUrl4\": \"https://nsdk-s04.uss.urbi.ae/ver4/\",\n" +
                    "      \"LegacyApplicationSettings/NetworkModule/StateServerUrl5\": \"https://nsdk-s05.uss.urbi.ae/ver4/\",\n" +
                    "      \"LegacyApplicationSettings/NetworkModule/StateServerUrl6\": \"https://nsdk-s06.uss.urbi.ae/ver4/\",\n" +
                    "      \"LegacyApplicationSettings/NetworkModule/StateServerUrl7\": \"https://nsdk-s07.uss.urbi.ae/ver4/\",\n" +
                    "      \"LegacyApplicationSettings/NetworkModule/StateServerUrl8\": \"https://nsdk-s08.uss.urbi.ae/ver4/\",\n" +
                    "      \"LegacyApplicationSettings/NetworkModule/StateServerUrl9\": \"https://nsdk-s09.uss.urbi.ae/ver4/\",\n" +
                    "      \"LegacyApplicationSettings/NetworkModule/StateServerUrl10\": \"https://nsdk-s10.uss.urbi.ae/ver4/\",\n" +
                    "      \"LegacyApplicationSettings/NetworkModule/StateServerUrl11\": \"https://nsdk-s11.uss.urbi.ae/ver4/\",\n" +
                    "      \"LegacyApplicationSettings/NetworkModule/StateServerUrl12\": \"https://nsdk-s12.uss.urbi.ae/ver4/\",\n" +
                    "      \"LegacyApplicationSettings/NetworkModule/StateServerUrl13\": \"https://nsdk-s13.uss.urbi.ae/ver4/\",\n" +
                    "      \"LegacyApplicationSettings/NetworkModule/StateServerUrl14\": \"https://nsdk-s14.uss.urbi.ae/ver4/\",\n" +
                    "      \"LegacyApplicationSettings/NetworkModule/StateServerUrl15\": \"https://nsdk-s15.uss.urbi.ae/ver4/\",\n" +
                    "      \"LegacyApplicationSettings/NetworkModule/StateServerUrl16\": \"https://nsdk-s16.uss.urbi.ae/ver4/\",\n" +
                    "      \"LegacyApplicationSettings/NetworkModule/RegionServerUrl1\": \"https://nsdk-s01.uss.urbi.ae/ver4/\",\n" +
                    "      \"LegacyApplicationSettings/NetworkModule/RegionServerUrl2\": \"https://nsdk-s02.uss.urbi.ae/ver4/\",\n" +
                    "      \"LegacyApplicationSettings/NetworkModule/RegionServerUrl3\": \"https://nsdk-s03.uss.urbi.ae/ver4/\",\n" +
                    "      \"LegacyApplicationSettings/NetworkModule/RegionServerUrl4\": \"https://nsdk-s04.uss.urbi.ae/ver4/\",\n" +
                    "      \"LegacyApplicationSettings/NetworkModule/RegionServerUrl5\": \"https://nsdk-s05.uss.urbi.ae/ver4/\",\n" +
                    "      \"LegacyApplicationSettings/NetworkModule/RegionServerUrl6\": \"https://nsdk-s06.uss.urbi.ae/ver4/\",\n" +
                    "      \"LegacyApplicationSettings/NetworkModule/RegionServerUrl7\": \"https://nsdk-s07.uss.urbi.ae/ver4/\",\n" +
                    "      \"LegacyApplicationSettings/NetworkModule/RegionServerUrl8\": \"https://nsdk-s08.uss.urbi.ae/ver4/\",\n" +
                    "      \"LegacyApplicationSettings/NetworkModule/RegionServerUrl9\": \"https://nsdk-s09.uss.urbi.ae/ver4/\",\n" +
                    "      \"LegacyApplicationSettings/NetworkModule/RegionServerUrl10\": \"https://nsdk-s10.uss.urbi.ae/ver4/\",\n" +
                    "      \"LegacyApplicationSettings/NetworkModule/RegionServerUrl11\": \"https://nsdk-s11.uss.urbi.ae/ver4/\",\n" +
                    "      \"LegacyApplicationSettings/NetworkModule/RegionServerUrl12\": \"https://nsdk-s12.uss.urbi.ae/ver4/\",\n" +
                    "      \"LegacyApplicationSettings/NetworkModule/RegionServerUrl13\": \"https://nsdk-s13.uss.urbi.ae/ver4/\",\n" +
                    "      \"LegacyApplicationSettings/NetworkModule/RegionServerUrl14\": \"https://nsdk-s14.uss.urbi.ae/ver4/\",\n" +
                    "      \"LegacyApplicationSettings/NetworkModule/RegionServerUrl15\": \"https://nsdk-s15.uss.urbi.ae/ver4/\",\n" +
                    "      \"LegacyApplicationSettings/NetworkModule/RegionServerUrl16\": \"https://nsdk-s16.uss.urbi.ae/ver4/\",\n" +
                    "      \"dgis/native-sdk/routing/free_roam_server_url\": \"https://urbi-lifestyle.momrah.gov.sa/free_roam/2.0\"\n" +
                    "    }\n" +
                    "  }\n" +
                    "]"
        )
    )
    return DGis.initialize(
        appContext,
        vendorConfig = vendorConfig,
        logOptions = LogOptions(
            customLevel = LogLevel.WARNING,
            customSink = createLogSink()
        )
    )
}
