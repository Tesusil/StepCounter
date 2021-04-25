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
import javax.inject.Qualifier
import javax.inject.Singleton

@InstallIn(ApplicationComponent::class)
@Module
object ApplicationModule {
    private const val MAX_STEPS = 10000

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
        context: Context,
        googleSignInAccount: GoogleSignInAccount
    ): ApplicationRepository {
        return ApplicationRepository(context, googleSignInAccount)
    }

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class MaxSteps
}