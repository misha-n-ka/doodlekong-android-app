package ru.mkirilkin.doodlekong.di

import android.app.Application
import android.content.Context
import com.google.gson.Gson
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.lifecycle.android.AndroidLifecycle
import com.tinder.scarlet.retry.LinearBackoffStrategy
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.mkirilkin.doodlekong.data.remote.api.SetupApi
import ru.mkirilkin.doodlekong.data.remote.websocket.CustomGsonMessageAdapter
import ru.mkirilkin.doodlekong.data.remote.websocket.DrawingApi
import ru.mkirilkin.doodlekong.data.remote.websocket.FlowStreamAdapter
import ru.mkirilkin.doodlekong.repository.DefaultSetupRepository
import ru.mkirilkin.doodlekong.repository.SetupRepository
import ru.mkirilkin.doodlekong.util.Constants

@Module
@InstallIn(ActivityRetainedComponent::class)
object ActivityModule {

    @ActivityRetainedScoped
    @Provides
    fun provideSetupApi(okHttpClient: OkHttpClient): SetupApi {
        return Retrofit.Builder()
            .baseUrl(
                if (Constants.USE_LOCALHOST) Constants.HTTP_BASE_URL_LOCALHOST
                else Constants.HTTP_BASE_URL
            )
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(SetupApi::class.java)
    }

    @ActivityRetainedScoped
    @Provides
    fun provideDrawingApi(
        app: Application,
        okkHttpClient: OkHttpClient,
        gson: Gson
    ): DrawingApi {
        return Scarlet.Builder()
            .backoffStrategy(LinearBackoffStrategy(Constants.RECONNECT_INTERVAL))
            .lifecycle(AndroidLifecycle.ofApplicationForeground(app))
            .webSocketFactory(
                okkHttpClient.newWebSocketFactory(
                    if (Constants.USE_LOCALHOST) Constants.WEBSOCKET_BASE_URL_LOCALHOST else Constants.WEBSOCKET_BASE_URL
                )
            )
            .addStreamAdapterFactory(FlowStreamAdapter.Factory)
            .addMessageAdapterFactory(CustomGsonMessageAdapter.Factory(gson))
            .build()
            .create()
    }

    @ActivityRetainedScoped
    @Provides
    fun provideSetupRepository(
        setupApi: SetupApi,
        @ApplicationContext context: Context
    ): SetupRepository = DefaultSetupRepository(setupApi, context)
}
