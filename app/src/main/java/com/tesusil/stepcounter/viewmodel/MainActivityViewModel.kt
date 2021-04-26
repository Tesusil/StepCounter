package com.tesusil.stepcounter.viewmodel

import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.tesusil.stepcounter.di.ApplicationModule
import com.tesusil.stepcounter.repository.ApplicationRepository
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable


class MainActivityViewModel
@ViewModelInject constructor(
    private val repo: ApplicationRepository,
    @Assisted private val savedStateHandle: SavedStateHandle,
    @ApplicationModule.ObserveScheduler private val observeSchedulers: Scheduler,
    @ApplicationModule.SubscriptionScheduler private val subscribeSchedulers: Scheduler
) : ViewModel() {

    val totalStepsLiveData: LiveData<Int>
        get() = _totalStepsLiveData

    private val compositeDisposable: CompositeDisposable
    private val _totalStepsLiveData: MutableLiveData<Int> = MutableLiveData()
    private var afterFirstTick: Boolean = false

    init {
        repo.subscribeTotalStepsClient()
        compositeDisposable = CompositeDisposable()
    }

    fun subscribeForSteps(
        subscribeScheduler: Scheduler = subscribeSchedulers,
        observeScheduler: Scheduler = observeSchedulers,
        onNextAction: (Int) -> Unit = this::onNext,
        onErrorAction: (Throwable) -> Unit = this::onError,
        onCompleteAction: () -> Unit = this::onComplete
    ) {
        val disposable: Disposable = repo
            .getTotalStepsObserver()
            .subscribeOn(subscribeScheduler)
            .observeOn(observeScheduler)
            .subscribe(onNextAction, onErrorAction, onCompleteAction)
        compositeDisposable.add(disposable)
    }

    private fun onNext(argument: Int) {
        _totalStepsLiveData.postValue(argument)
        if(!afterFirstTick) {
            repo.unSubscribeTotalStepsClient()
            repo.subscribeLiveStepsClient()
            afterFirstTick = true
        }
    }

    private fun onError(exception: Throwable) {
        Log.e(TAG, "OnSubscribeError: ", exception)
    }

    private fun onComplete() {
        Log.i(TAG, "Subscription completed")
    }


    override fun onCleared() {
        if (compositeDisposable.size() > 0) {
            compositeDisposable.clear()
        }
        if(afterFirstTick) {
            repo.unSubscribeLiveStepsClient()
        } else{
            repo.unSubscribeTotalStepsClient()
        }
    }

    companion object {
        const val TAG = "MainActivityViewModel"
    }
}