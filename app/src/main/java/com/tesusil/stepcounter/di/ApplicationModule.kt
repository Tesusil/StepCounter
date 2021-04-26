package com.tesusil.stepcounter.di

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.tesusil.stepcounter.repository.ApplicationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Qualifier
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
object ApplicationModule {
    const val MAX_STEPS = 10000

    @Singleton
    @Provides
    fun provideFitnessOptions(): FitnessOptions {
        return FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
            .build()
    }

    @Singleton
    @Provides
    fun provideGoogleAccount(
        @ApplicationContext context: Context,
        fitnessOptions: FitnessOptions
    ): GoogleSignInAccount {
        return GoogleSignIn.getAccountForExtension(context, fitnessOptions)
    }

    @MaxSteps
    @Singleton
    @Provides
    fun provideMaxSteps(): Int {
        return MAX_STEPS
    }

    @Singleton
    @Provides
    fun provideApplicationRepository(
        @ApplicationContext context: Context,
        googleSignInAccount: GoogleSignInAccount,
        @MaxSteps maxSteps: Int
    ): ApplicationRepository {
        return ApplicationRepository(context, googleSignInAccount, maxSteps)
    }


    @Singleton
    @Provides
    @SubscriptionScheduler
    fun provideSubscriptionScheduler(): Scheduler {
        return Schedulers.io()
    }


    @Singleton
    @Provides
    @ObserveScheduler
    fun provideObserveScheduler(): Scheduler {
        return AndroidSchedulers.mainThread()
    }

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class MaxSteps

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class SubscriptionScheduler

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class ObserveScheduler
}