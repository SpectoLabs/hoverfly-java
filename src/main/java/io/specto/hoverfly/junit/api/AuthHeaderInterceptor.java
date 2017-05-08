package io.specto.hoverfly.junit.api;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

class AuthHeaderInterceptor implements Interceptor {

    private final String authToken;

    AuthHeaderInterceptor(String authToken) {
        this.authToken = authToken;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        Request request = originalRequest.newBuilder().addHeader("Authorization", "Bearer " + authToken).build();
        return chain.proceed(request);
    }
}
