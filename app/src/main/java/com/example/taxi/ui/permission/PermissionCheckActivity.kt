package com.example.taxi.ui.permission

import android.Manifest
import android.app.ActionBar.LayoutParams
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.taxi.R
import com.example.taxi.databinding.ActivityPermissionCheckBinding
import com.example.taxi.utils.LocationPermissionUtils
import com.google.android.material.button.MaterialButton

class PermissionCheckActivity : AppCompatActivity() {
    private var isPromptMode = false

    private lateinit var viewBinding: ActivityPermissionCheckBinding

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) handleLocationPermissionGranted()
        }

    private val backgroundLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) checkAndProceed()
        }

    private val batteryOptimizationResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) checkAndProceed()
        }

    private val locationSettingActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) handleLocationSettingsEnabled()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        isPromptMode = intent.getBooleanExtra(IS_PROMPT_MODE, false)

        if (isPromptMode) {
            checkAndAsk()
        } else {
            viewBinding = ActivityPermissionCheckBinding.inflate(layoutInflater)
            setContentView(viewBinding.root)
            if (!LocationPermissionUtils.isBackgroundPermissionGranted(this) || !LocationPermissionUtils.isBasicPermissionGranted(this)){
                showPermissionExplanationDialog()
            }
            viewBinding.buttonConfirmPermissions.setOnClickListener { checkAndAsk() }
        }
    }

    override fun onResume() {
        super.onResume()
        updateStatusImages()
        checkAndProceed()
    }

    private fun checkAndAsk() {
        when {
            checkPermissions() -> handlePermissionsGranted()
            shouldShowRationalePermission() -> showPermissionExplanationDialog()
            else -> requestPermissions()
        }
    }

    private fun checkPermissions(): Boolean =
        LocationPermissionUtils.isBasicPermissionGranted(this) &&
                LocationPermissionUtils.isBackgroundPermissionGranted(this) &&
                Settings.canDrawOverlays(this) &&
                LocationPermissionUtils.isPowerSavingModeEnabled(this)

    private fun shouldShowRationalePermission(): Boolean =
        LocationPermissionUtils.shouldShowRationaleBasicPermission(this)

    private fun requestPermissions() {
        when {
            !LocationPermissionUtils.isBasicPermissionGranted(this) ->
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            !LocationPermissionUtils.isLocationEnabled(this) -> LocationPermissionUtils.askEnableLocationRequest(this) { locationEnabled() }
            !LocationPermissionUtils.isBackgroundPermissionGranted(this) ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    backgroundLocationPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
            !Settings.canDrawOverlays(this) -> showOverlayPermissionDialog()
            !LocationPermissionUtils.isPowerSavingModeEnabled(this) -> showBatteryPermissionDialog()
        }
    }

    private fun showPermissionExplanationDialog() {
        val dialog = Dialog(this).apply {
            setContentView(R.layout.dialog_ask_permission)
            window?.apply {
                setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
                setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                setGravity(Gravity.CENTER)
            }
        }

        dialog.findViewById<MaterialButton>(R.id.button_permissions).setOnClickListener {
            dialog.dismiss()
            requestPermissions()
        }

        dialog.findViewById<MaterialButton>(R.id.button_skip).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showOverlayPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.neеded_permission))
            .setMessage(getString(R.string.draw_permission))
            .setPositiveButton(getString(R.string.go_to_settings)) { _, _ ->
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivity(intent)
            }
            .create()
            .show()
    }

    private fun showBatteryPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.neеded_permission))
            .setMessage(getString(R.string.battery_permission))
            .setPositiveButton(getString(R.string.go_to_settings)) { _, _ ->
                requestIgnoreBatteryOptimizations()
            }
            .create()
            .show()
    }

    private fun updateStatusImages() {
        updateImageView(viewBinding.imgBattery, LocationPermissionUtils.isPowerSavingModeEnabled(this))
        updateImageView(viewBinding.imgOverlay, Settings.canDrawOverlays(this))
        updateImageView(viewBinding.imgLocation, LocationPermissionUtils.isBasicPermissionGranted(this))
        updateImageView(viewBinding.imgGps, LocationPermissionUtils.isLocationEnabled(this) && LocationPermissionUtils.isBackgroundPermissionGranted(this))
    }

    private fun updateImageView(imageView: ImageView, isActive: Boolean) {
        val paddingInPx = (6 * resources.displayMetrics.density + 0.5f).toInt()

        imageView.apply {
            setImageResource(if (isActive) R.drawable.ic_check else R.drawable.ic_cancel_settings)
            setPadding(if (isActive) 0 else paddingInPx, if (isActive) 0 else paddingInPx, if (isActive) 0 else paddingInPx, if (isActive) 0 else paddingInPx)
        }
    }

    private fun handlePermissionsGranted() {
        if(!LocationPermissionUtils.isLocationEnabled(this)){
            LocationPermissionUtils.askEnableLocationRequest(this) { locationEnabled() }
        }else if (!Settings.canDrawOverlays(this)) {
            showOverlayPermissionDialog()
        } else if (!LocationPermissionUtils.isPowerSavingModeEnabled(this)) {
            showBatteryPermissionDialog()
        }
    }

    private fun handleLocationPermissionGranted() {
        if (checkPermissions()) {
            LocationPermissionUtils.askEnableLocationRequest(this) { locationEnabled() }
        } else {
            requestPermissions()
        }
    }

    private fun handleLocationSettingsEnabled() {
        LocationPermissionUtils.compute(this)
        finish()
    }

    private fun requestIgnoreBatteryOptimizations() {
        if (!LocationPermissionUtils.isPowerSavingModeEnabled(this)) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).setData(Uri.parse("package:$packageName"))
            batteryOptimizationResultLauncher.launch(intent)
        }
    }

    private fun locationEnabled() {
        checkAndProceed()
    }

    private fun checkAndProceed() {
        if (checkPermissions()) {
            proceed()
        }
    }

    private fun proceed() {
        LocationPermissionUtils.compute(this)
        setResult(RESULT_OK)
        finish()
    }

    private fun cancel() {
        finish()
    }

    companion object {
        private const val IS_PROMPT_MODE = "IS_PROMPT_MODE"

        fun getOpenIntent(context: Context, isPromptMode: Boolean = false): Intent {
            return Intent(context, PermissionCheckActivity::class.java).apply {
                putExtra(IS_PROMPT_MODE, isPromptMode)
            }
        }
    }
}
