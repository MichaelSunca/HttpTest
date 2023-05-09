package com.michaelsun.app.httptest;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
import okio.BufferedSource;

public class MockWebServerInterceptor implements Interceptor {
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();

        if(request.url().toString().contains("/helloworld/test")){
            HttpUrl url = request.url().newBuilder()
                    .scheme("http")
                    .host(MyMockWebServer.getInstance().url("/").host()) // 使用 MockWebServer 的 URL
                    .port(MyMockWebServer.getInstance().url("/").port())
                    .build();
            request = request.newBuilder()
                    .url(url)
                    .build();
        }

        return chain.proceed(request);
    }

}
