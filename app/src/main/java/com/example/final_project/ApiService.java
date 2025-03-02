package com.example.final_project;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    @POST("v1/models/gemini-1.5-pro:generateContent")
    Call<GeminiResponse> getGeminiResponse(
            @Body GeminiRequest request,
            @Query("key") String apiKey
    );
}