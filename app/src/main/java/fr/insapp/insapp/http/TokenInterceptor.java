package fr.insapp.insapp.http;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.io.IOException;

import fr.insapp.insapp.App;
import fr.insapp.insapp.activities.IntroActivity;
import fr.insapp.insapp.models.credentials.LoginCredentials;
import fr.insapp.insapp.models.credentials.SessionCredentials;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;

/**
 * Created by thomas on 10/07/2017.
 */

public class TokenInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        SharedPreferences preferences = App.getAppContext().getSharedPreferences("Credentials", Context.MODE_PRIVATE);

        SessionCredentials sessionCredentials = new Gson().fromJson(preferences.getString("session", ""), SessionCredentials.class);

        if (sessionCredentials != null) {
            final String sessionToken = sessionCredentials.getSessionToken().getToken();

            HttpUrl url = request.url().newBuilder().addQueryParameter("token", sessionToken).build();
            request = request.newBuilder().url(url).build();
        }

        Response response = chain.proceed(request);

        switch (response.code()) {

            // does the token need to be refreshed ? (unauthorized)

            case 401:
                LoginCredentials loginCredentials = new Gson().fromJson(preferences.getString("login", ""), LoginCredentials.class);

                Call<SessionCredentials> call = ServiceGenerator.create().logUser(loginCredentials);
                SessionCredentials refreshedSessionCredentials = call.execute().body();

                HttpUrl url = request.url().newBuilder().setQueryParameter("token", refreshedSessionCredentials.getSessionToken().getToken()).build();
                request = request.newBuilder().url(url).build();

                return chain.proceed(request);

            // did the user log in somewhere else ?

            case 404:
                Context context = App.getAppContext();
                context.startActivity(new Intent(context, IntroActivity.class));

                return new Response.Builder().code(600).request(request).build();

            default:
                return response;
        }
    }
}