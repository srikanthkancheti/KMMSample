package com.search.flicker

import io.github.aakira.napier.Napier
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

class RemoteDataFetcher {
    object Model {
        @Serializable
        data class Result(val query: Query)
        @Serializable
        data class Query(val searchinfo: SearchInfo)
        @Serializable
        data class SearchInfo(val totalhits: Int)
    }

    private val httpClient = httpClient {
        install(Logging) {
            level = LogLevel.HEADERS
            logger = object : Logger {
                override fun log(message: String) {
                    Napier.v(tag = "HTTP Client", message = message)
                }
            }
        }
        install(JsonFeature) {
            val json = kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
                useAlternativeNames = false
            }
            serializer = KotlinxSerializer(json)
        }
    }.also {
        initLogger()
    }

    @Throws(Exception::class)
    suspend fun fetch(keyword: String): String {
        val hitCountModel = getFlickerSearchImages(keyword)
        return "Hello, ${Platform().platform}!\n$keyword:${hitCountModel.query.searchinfo.totalhits}"
    }

    private suspend fun getFlickerSearchImages(keyword: String): Model.Result {
        return httpClient.submitForm(
            url = "https://api.flickr.com/services/rest",
            formParameters = Parameters.build {
                append("api_key", "3de6060455725674d60a4fe3c0c57c93")
                append("method", "flickr.photos.search")
                append("tags", "search")
                append("tags", keyword)
                append("format", "json")
                append("nojsoncallback", "true")
                append("extras", "media")
                append("extras", "url_sq")
                append("extras", "url_m")
                append("per_page", "20")
                append("page", "1")
            },
            encodeInQuery = true
        )
    }
}