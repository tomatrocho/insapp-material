package fr.insapp.insapp.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import auto.parcelgson.gson.AutoParcelGsonTypeAdapterFactory;
import fr.insapp.insapp.activities.MainActivity;
import fr.insapp.insapp.http.interceptors.JsonInterceptor;
import fr.insapp.insapp.http.interceptors.TokenInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by thomas on 09/07/2017.
 */

public class ServiceGenerator {

    public static String ROOT_URL;
    public static String CDN_URL;

    static {
        if (MainActivity.dev) {
            // TODO : Dès que l'api pour le debug est disponible, remettre les bonnes adresses de debug.
            ROOT_URL = "https://insapp.fr/api/v1/";
            //ROOT_URL = "https://dev.insapp.fr/api/v1/";
            CDN_URL = "https://insapp.fr/cdn/";
        }
        else {
            ROOT_URL = "https://insapp.fr/api/v1/";
            CDN_URL = "https://insapp.fr/cdn/";
        }
    }

    private static HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
    private static JsonInterceptor jsonInterceptor = new JsonInterceptor();
    private static TokenInterceptor tokenInterceptor = new TokenInterceptor();

    private static Client client;

    public static <S> S createService(Class<S> serviceClass, TypeAdapter... adapters) {
        Gson gson = new GsonBuilder().registerTypeAdapterFactory(new AutoParcelGsonTypeAdapterFactory()).create();
        Retrofit.Builder builder = new Retrofit.Builder().baseUrl(ServiceGenerator.ROOT_URL).addConverterFactory(GsonConverterFactory.create(gson));

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        httpClient.addInterceptor(jsonInterceptor);
        httpClient.addInterceptor(tokenInterceptor);
        httpClient.addInterceptor(loggingInterceptor);

        builder.client(httpClient.build());

        return builder.build().create(serviceClass);
    }

    public static Client create() {
        if (ServiceGenerator.client == null) {
            ServiceGenerator.client = ServiceGenerator.createService(Client.class);
        }

        return ServiceGenerator.client;
    }
}