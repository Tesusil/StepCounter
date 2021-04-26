package com.tesusil.stepcounter.repository

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.OnDataPointListener
import com.google.android.gms.fitness.request.SensorRequest
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

class ApplicationRepository(
    private val context: Context,
    private val googleAccount: GoogleSignInAccount,
    private val maxSteps: Int
) {


    private val totalStepSubject: BehaviorSubject<Int> =
        BehaviorSubject.create()
    private val errorSubject: BehaviorSubject<Exception> = BehaviorSubject.create()

    private val stepsListenerType: String = "steps"
    private val listenerDelayTime: Long = 500L
    private val listenerDelayTimeUnit: TimeUnit = TimeUnit.MILLISECONDS

    private val totalStepListener: OnDataPointListener = getLiveStepsListener {
        val newValue = it % maxSteps
        totalStepSubject.onNext(newValue)
    }

    private val liveStepListener: OnDataPointListener = getLiveStepsListener {
        val newValue = (it + totalStepSubject.value) % maxSteps
        totalStepSubject.onNext(newValue)
    }

    private val totalStepsRequest: SensorRequest = SensorRequest.Builder()
        .setDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
        .setSamplingRate(listenerDelayTime, listenerDelayTimeUnit)
        .build()

    private val liveStepsRequest: SensorRequest = SensorRequest.Builder()
        .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
        .setSamplingRate(listenerDelayTime, listenerDelayTimeUnit)
        .build()

    fun subscribeTotalStepsClient() {
        requestTotalSteps()
    }

    fun subscribeLiveStepsClient() {
        requestLiveSteps()
    }

    fun unSubscribeTotalStepsClient(
        onSuccessAction: () -> Unit = this::onSensorClientUnSubscribeSuccess,
        onErrorAction: (Exception) -> Unit = this::onSensorClientUnSubscribeError
    ) {
        Fitness.getSensorsClient(context, googleAccount)
            .remove(totalStepListener)
            .addOnSuccessListener { onSuccessAction.invoke() }
            .addOnFailureListener { onErrorAction.invoke(it) }
    }

    fun unSubscribeLiveStepsClient(
        onSuccessAction: () -> Unit = this::onSensorClientUnSubscribeSuccess,
        onErrorAction: (Exception) -> Unit = this::onSensorClientUnSubscribeError
    ) {
        Fitness.getSensorsClient(context, googleAccount)
            .remove(liveStepListener)
            .addOnSuccessListener { onSuccessAction.invoke() }
            .addOnFailureListener { onErrorAction.invoke(it) }
    }

    fun getTotalStepsObserver(): Observable<Int> {
        return totalStepSubject
    }

    private fun requestTotalSteps(
        onSuccessAction: () -> Unit = this::onSensorSubscribeSuccess,
        onErrorAction: (Exception) -> Unit = this::onSensorSubscribeError
    ) {
        Fitness.getSensorsClient(context, googleAccount)
            .add(totalStepsRequest, totalStepListener)
            .addOnSuccessListener {
                onSuccessAction.invoke()
            }
            .addOnFailureListener { exception ->
                onErrorAction.invoke(exception)
            }
    }

    private fun requestLiveSteps(
        onSuccessAction: () -> Unit = this::onSensorSubscribeSuccess,
        onErrorAction: (Exception) -> Unit = this::onSensorSubscribeError
    ) {
        Fitness.getSensorsClient(context, googleAccount)
            .add(liveStepsRequest, liveStepListener)
            .addOnSuccessListener {
                onSuccessAction.invoke()
            }
            .addOnFailureListener { exception ->
                onErrorAction.invoke(exception)
            }
    }

    private fun getLiveStepsListener(
        fieldName: String = stepsListenerType,
        itemAction: (Int) -> Unit
    ): OnDataPointListener {
        return OnDataPointListener { dataPoint ->
            for (field in dataPoint.dataType.fields) {
                if (field.name.equals(fieldName)) {
                    val value = dataPoint.getValue(field).asInt()
                    itemAction(value)
                }
            }
        }
    }

    private fun onSensorSubscribeSuccess() {
        Log.i(TAG, "Sensor listener registered!")
    }

    private fun onSensorSubscribeError(exception: Exception) {
        Log.e(TAG, "Sensor listener subscribe error: ", exception)
        errorSubject.onNext(exception)
    }

    private fun onSensorClientUnSubscribeSuccess() {
        Log.i(TAG, "Sensor listener unregistered!")
    }

    private fun onSensorClientUnSubscribeError(exception: Exception) {
        Log.e(TAG, "Sensor listener unsubscribe error: ", exception)
        errorSubject.onNext(exception)
    }

    companion object {
        private const val DEFAULT_START_VALUE: Int = 0
        private const val TAG = "ApplicationRepository"
    }
}