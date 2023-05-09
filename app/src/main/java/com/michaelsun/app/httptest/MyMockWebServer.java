package com.michaelsun.app.httptest;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
import okio.BufferedSource;

public class MyMockWebServer {

    private static class MockWebServerSingletonHolder{
        static final MockWebServer mockWebServer = new MockWebServer();
    }

    public static MockWebServer getInstance(){
        return MockWebServerSingletonHolder.mockWebServer;
    }


    public void start(Context context){
        try {
            // 创建一个 SSL 上下文
//            InputStream inputStream = context.getResources().openRawResource(R.raw.mycert);
//            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
//            X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(inputStream);
//
//            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
//            keyStore.load(null, null);
//            keyStore.setCertificateEntry("certificate", certificate);
//
//            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
//            trustManagerFactory.init(keyStore);
//
//            SSLContext sslContext = SSLContext.getInstance("SSL");
//            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

            // 将 SSL 上下文设置到 MockWebServer 中
            MockWebServer mockWebServer = getInstance();
//            mockWebServer.useHttps(sslContext.getSocketFactory(), false);
            mockWebServer.start();
            String baseUrl = mockWebServer.url("/").host();
            mockWebServer.setDispatcher(dispatcher);
            int port = mockWebServer.getPort();
            Log.d("MyMockWebServer", "port : " + port + ", isStarted = " + mockWebServer.getHostName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    final Dispatcher dispatcher = new Dispatcher() {

        @Override
        public MockResponse dispatch(RecordedRequest recordedRequest) throws InterruptedException {

                if (recordedRequest.getPath().equals("/helloworld/test")) {
                    // 返回自定义响应
                    return new MockResponse().setBody("Custom Response").setResponseCode(200);
                } else {
                    // 转发到正常服务器
                    MockResponse mockResponse = null;
                    OkHttpClient client = new OkHttpClient.Builder().build();

                    // 创建 OkHttp 请求
                    Request request = new Request.Builder()
                            .url(recordedRequest.getRequestUrl().toString())
                            .method(recordedRequest.getMethod(), RequestBody.create(null, recordedRequest.getBody().clone().readByteArray()))
                            .headers(recordedRequest.getHeaders())
                            .build();

                    // 发送请求
                    Call call = client.newCall(request);
                    Response response = null;
                    try {
                        response = call.execute();
                        // 将 OkHttp 响应转换为 MockWebServer 响应
                        BufferedSource source = response.body().source();
                        source.request(Long.MAX_VALUE); // 确保缓冲区已满
                        Buffer buffer = source.buffer().clone();
                        mockResponse = new MockResponse()
                                .setResponseCode(response.code())
                                .setHeaders(response.headers())
                                .setBody(buffer);

                        // 关闭 OkHttp 响应
                        response.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return mockResponse;
            }
        }
    };
}
