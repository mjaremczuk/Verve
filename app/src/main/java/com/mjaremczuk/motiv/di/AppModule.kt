package com.mjaremczuk.motiv.di

import com.mjaremczuk.motiv.data.local.AppDatabase
import com.mjaremczuk.motiv.data.local.SettingsRepositoryImpl
import com.mjaremczuk.motiv.data.remote.QuotesApi
import com.mjaremczuk.motiv.data.repository.QuoteRepositoryImpl
import com.mjaremczuk.motiv.domain.repository.QuoteRepository
import com.mjaremczuk.motiv.domain.repository.SettingsRepository
import com.mjaremczuk.motiv.domain.usecase.GetCategoriesUseCase
import com.mjaremczuk.motiv.domain.usecase.GetNotificationSettingsUseCase
import com.mjaremczuk.motiv.domain.usecase.GetQuoteOfTheDayUseCase
import com.mjaremczuk.motiv.domain.usecase.GetRandomQuoteUseCase
import com.mjaremczuk.motiv.domain.usecase.ToggleNotificationsUseCase
import com.mjaremczuk.motiv.domain.usecase.UpdateNotificationTimeUseCase
import com.mjaremczuk.motiv.ui.viewmodel.QuoteViewModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

val appModule = module {
    // Database and DAO
    single { AppDatabase.getDatabase(androidContext()) }
    single { get<AppDatabase>().quoteDao() }

    // Moshi
    single {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    // OkHttpClient
    single {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "Verve-Motivation-App/1.0.0 (Android)")
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    // Retrofit API
    single {
        Retrofit.Builder()
            .baseUrl("https://cdn.jsdelivr.net/")
            .client(get())
            .addConverterFactory(MoshiConverterFactory.create(get()))
            .build()
            .create(QuotesApi::class.java)
    }

    // Repositories
    singleOf(::QuoteRepositoryImpl) { bind<QuoteRepository>() }
    singleOf(::SettingsRepositoryImpl) { bind<SettingsRepository>() }

    // Use Cases
    singleOf(::GetQuoteOfTheDayUseCase)
    singleOf(::GetRandomQuoteUseCase)
    singleOf(::GetCategoriesUseCase)
    singleOf(::GetNotificationSettingsUseCase)
    singleOf(::ToggleNotificationsUseCase)
    singleOf(::UpdateNotificationTimeUseCase)

    // ViewModel
    viewModelOf(::QuoteViewModel)
}
