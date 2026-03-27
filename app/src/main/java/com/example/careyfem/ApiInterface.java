package com.example.careyfem;

import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiInterface {

    @POST("api/auth/login/")
    Call<Map<String, String>> login(@Body Map<String, String> data);

    @POST("api/auth/signup/")
    Call<Map<String, String>> signup(@Body Map<String, String> data);

    @POST("api/auth/reset-password/")
    Call<Map<String, String>> resetPassword(@Body Map<String, String> data);

    @POST("api/core/ai-chat/")
    Call<Map<String, String>> getAIResponse(@Body Map<String, String> data);

    // Mood Analytics
    @GET("api/core/mood-analytics/")
    Call<Map<String, Object>> getMoodAnalytics();


    // Mood Tracker
    @POST("api/core/mood-tracker/")
    Call<Map<String, Object>> saveMood(@Body Map<String, Object> data);

    @GET("api/core/mood-tracker/")
    Call<java.util.List<Map<String, Object>>> getMoods();

    // Journal
    @POST("api/core/journal/")
    Call<Map<String, Object>> saveJournal(@Body Map<String, Object> data);

    @GET("api/core/journal/")
    Call<java.util.List<Map<String, Object>>> getJournals();

    // Safety
    @POST("api/safety/sos/")
    Call<Map<String, Object>> sendSOS(@Body Map<String, Object> data);

    @GET("api/safety/contacts/")
    Call<java.util.List<Map<String, Object>>> getContacts();

    @POST("api/safety/contacts/")
    Call<Map<String, Object>> saveContact(@Body Map<String, Object> data);
}
