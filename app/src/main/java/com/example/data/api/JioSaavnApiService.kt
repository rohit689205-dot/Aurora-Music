package com.example.data.api

import com.example.data.api.model.JioSaavnAutocompleteResponse
import com.example.data.api.model.JioSaavnSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface JioSaavnApiService {

    @GET("api.php")
    suspend fun searchSongs(
        @Query("__call") call: String = "search.getResults",
        @Query("_format") format: String = "json",
        @Query("p") page: Int = 1,
        @Query("n") limit: Int = 10,
        @Query("q") query: String
    ): Response<JioSaavnSearchResponse>

    @GET("api.php")
    suspend fun autocomplete(
        @Query("__call") call: String = "autocomplete.get",
        @Query("_format") format: String = "json",
        @Query("_marker") marker: Int = 0,
        @Query("query") query: String
    ): Response<JioSaavnAutocompleteResponse>
}
