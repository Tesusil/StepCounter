package com.tesusil.stepcounter

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.distinctUntilChanged
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.material.snackbar.Snackbar
import com.tesusil.stepcounter.databinding.ActivityMainBinding
import com.tesusil.stepcounter.di.ApplicationModule
import com.tesusil.stepcounter.view.MainBindingModel
import com.tesusil.stepcounter.viewmodel.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.max


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var googleSignInAccount: GoogleSignInAccount

    @Inject
    lateinit var fitnessOptions: FitnessOptions

    private val maxSteps = ApplicationModule.MAX_STEPS

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainActivityViewModel by viewModels()

    private val requestCode = 17
    private val maxProgress = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.model = MainBindingModel(0, maxSteps)
        binding.mainProgressBar.progress = 0

        checkPermissionsAndStartObserve(requestCode)

        viewModel.totalStepsLiveData.distinctUntilChanged().observe(this, Observer {
            val model = MainBindingModel(it, maxSteps)
            val newProgress = getPercentageProgress(it, maxSteps)
            binding.mainProgressBar.max = maxProgress
            binding.mainProgressBar.progress = newProgress.toInt()
            binding.model = model
        })
    }

    private fun requestPermissions(requestCode: Int) {
        val shouldProvideRationale =
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            )

        requestCode.let {
            if (shouldProvideRationale) { // ponowna prosba o dostep do danych, wyswietlenie uzasadnienia prosby
                Snackbar.make(
                    binding.root,
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.ok) { // wyswietlenie snackbara
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                            requestCode
                        )
                    }
                    .show()
            } else { // prosba o dostep do danych
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                    requestCode
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (PackageManager.PERMISSION_GRANTED) {
            grantResults[0] -> {
                fitSignIn(requestCode)
            }
            else -> {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun performActionForRequestCode() {
        viewModel.subscribeForSteps()
    }

    private fun oAuthPermissionsApproved() =
        GoogleSignIn.hasPermissions(googleSignInAccount, fitnessOptions)

    private fun permissionApproved(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            )
        } else {
            true
        }
    }

    private fun checkPermissionsAndStartObserve(requestCode: Int) {
        if (permissionApproved()) {
            fitSignIn(requestCode)
        } else {
            requestPermissions(requestCode)
        }
    }

    private fun fitSignIn(requestCode: Int) {
        if (oAuthPermissionsApproved()) {
            performActionForRequestCode()
        } else {
            requestCode.let {
                GoogleSignIn.requestPermissions(
                    this,
                    requestCode,
                    googleSignInAccount, fitnessOptions
                )
            }
        }
    }

    private fun getPercentageProgress(steps: Int, maxSteps: Int): Double {
        val divineResult: Double = steps.toDouble() / maxSteps.toDouble()
        return divineResult * 100.0
    }
}