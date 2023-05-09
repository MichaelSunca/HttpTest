package com.michaelsun.app.httptest;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import com.michaelsun.app.httptest.databinding.ActivityMainBinding;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnStartService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        init();
                    }
                }).start();
            }
        });
        binding.btnHttpRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }


    private void init(){
        new MyMockWebServer().start(getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



    public void run() throws Exception {
        Request request = new Request.Builder()
                .url("https://192.168.10.101:8080/helloworld/test")
                .build();
        // 创建一个 SSL 上下文
//        InputStream inputStream = getResources().openRawResource(R.raw.mycert);
//        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
//        X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(inputStream);
//        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
//        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
//        keyStore.load(null, null);
//        keyStore.setCertificateEntry("certificate", certificate);
//        trustManagerFactory.init(keyStore);
//        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
//
//        SSLContext sslContext = SSLContext.getInstance("SSL");
//        sslContext.init(null, trustManagers, null);
//        SSLSocketFactory socketFactory = sslContext.getSocketFactory();
        OkHttpClient client = new OkHttpClient.Builder()
//                .sslSocketFactory(socketFactory, (X509TrustManager) trustManagers[0])
                .addInterceptor(new MockWebServerInterceptor())
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    Headers responseHeaders = response.headers();
                    for (int i = 0, size = responseHeaders.size(); i < size; i++) {
                        System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
                    }

                    System.out.println(responseBody.string());
                }
            }
        });
    }
}