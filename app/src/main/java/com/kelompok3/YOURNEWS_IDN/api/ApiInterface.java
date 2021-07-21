package com.kelompok3.YOURNEWS_IDN.api;

import com.kelompok3.YOURNEWS_IDN.models.Berita;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {

    @GET("top-headlines")
    Call<Berita> getNews(

            @Query("country") String country ,
            @Query("apiKey") String apiKey

    );

    @GET("top-headlines")
    Call<Berita> getNewsSearch(

        @Query("q") String keyword,
        @Query("country") String country ,
        @Query("apiKey") String apiKey
    );

    @GET("top-headlines")
    Call<Berita> getNewsFiltered(

            @Query("country") String country ,
            @Query("category") String category ,
            @Query("apiKey") String apiKey
    );

    @GET("top-headlines")
    Call<Berita> getNewsSearchFiltered(

            @Query("q") String keyword,
            @Query("country") String country ,
            @Query("category") String category ,
            @Query("apiKey") String apiKey
    );

    @GET("sources")
    Call<Berita> getSources(

            @Query("category") String category ,
            @Query("country") String country ,
            @Query("language") String language,
            @Query("apiKey") String apiKey
    );
    /*@GET("everything")
    Call<News> getNewsSearch(

            @Query("q") String keyword,
            //@Query("qlnTitle") String keyword, // klo mau di title doang
            @Query("language") String language,
            @Query("sortBy") String sortBy,
            @Query("apiKey") String apiKey

    );*/
}
