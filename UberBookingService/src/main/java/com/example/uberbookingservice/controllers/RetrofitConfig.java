package com.example.uberbookingservice.controllers;

import com.example.uberbookingservice.apis.LocationServiceApi;
import com.example.uberbookingservice.apis.UberSocketApi;
import com.netflix.discovery.EurekaClient;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Configuration
public class RetrofitConfig {

    @Autowired(required = false)
    private EurekaClient eurekaClient;

    private String getServiceUrl(String serviceName, String defaultUrl) {
        if (eurekaClient != null) {
            try {
                String instanceUrl = eurekaClient.getNextServerFromEureka(serviceName, false).getHomePageUrl();
                if (instanceUrl != null && !instanceUrl.isBlank()) {
                    return instanceUrl;
                }
            } catch (Exception ignored) {
            }
        }
        return defaultUrl;
    }

    @Bean
    public LocationServiceApi locationServiceApi() {
        return new Retrofit.Builder()
                .baseUrl(getServiceUrl("UberProject-LocationService", "http://location-service:7478/"))
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient.Builder().build())
                .build()
                .create(LocationServiceApi.class);
    }

    @Bean
    public UberSocketApi uberSocketApi() {
        return new Retrofit.Builder()
                .baseUrl(getServiceUrl("ClientSocketService", "http://socket-service:8086/"))
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient.Builder().build())
                .build()
                .create(UberSocketApi.class);
    }
}
