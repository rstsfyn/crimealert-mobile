package com.restusofyan.crimealert_mobile.data.api

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.restusofyan.crimealert_mobile.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY

        try {
            // Install Conscrypt as the security provider
            // Conscrypt has better support for modern TLS and SSL renegotiation
            val conscryptProvider = org.conscrypt.Conscrypt.newProvider()
            java.security.Security.insertProviderAt(conscryptProvider, 1)
            
            Log.d("AppModule", "Conscrypt provider installed successfully")
        } catch (e: Exception) {
            Log.e("AppModule", "Failed to install Conscrypt provider", e)
        }

        // Use production-safe trust manager that validates certificates
        val trustManager = SafeTrustManager()

        // Install the trust manager with Conscrypt
        val sslContext = SSLContext.getInstance("TLS", "Conscrypt")
        sslContext.init(null, arrayOf<TrustManager>(trustManager), SecureRandom())

        // Create custom socket factory that supports SSL renegotiation
        val sslSocketFactory = TLSSocketFactory(sslContext.socketFactory)

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            // SSL Configuration to handle renegotiation with Conscrypt
            .sslSocketFactory(sslSocketFactory, trustManager)
            // Hostname verifier (use default, but can be customized if needed)
            .hostnameVerifier { hostname, session ->
                // Default hostname verification
                javax.net.ssl.HttpsURLConnection.getDefaultHostnameVerifier()
                    .verify(hostname, session)
            }
            // Increase timeouts to handle slow SSL handshakes
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .callTimeout(120, TimeUnit.SECONDS)
            // Connection pool to reuse connections
            .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
            // Retry on connection failure
            .retryOnConnectionFailure(true)
            // Follow redirects
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    }
}
