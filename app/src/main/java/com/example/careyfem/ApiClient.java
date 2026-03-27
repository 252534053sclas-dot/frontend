package com.example.careyfem;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "http://192.168.1.62:8000/"; // LAN IP For Physical Device/Emulator
    private static Retrofit retrofit = null;

    public static Retrofit getClient(android.content.Context context) {
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder().addInterceptor(chain -> {
            okhttp3.Request original = chain.request();
            SessionManager session = new SessionManager(context);
            String token = session.getToken();
            if (token != null) {
                okhttp3.Request.Builder builder = original.newBuilder()
                        .header("Authorization", "Bearer " + token)
                        .method(original.method(), original.body());
                return chain.proceed(builder.build());
            }
            return chain.proceed(original);
        }).build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit;
    }
}
