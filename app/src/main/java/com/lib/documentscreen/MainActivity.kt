package com.lib.documentscreen

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_BASE
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_BASE_WITH_FILTER
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.radiusagent.documentscreen.R
import com.radiusagent.documentscreen.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var scannerLauncher: ActivityResultLauncher<IntentSenderRequest>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        registerDocScanner()
        setupClick()
    }

    /**
     * This function is used to setup onclick listener
     */
    private fun setupClick() {
        binding.tvModeFull.setOnClickListener(this)
        binding.tvModeBase.setOnClickListener(this)
        binding.tvModeWithFilter.setOnClickListener(this)
    }

    /**
     * This function is used to register the callback for scanner
     */
    private fun registerDocScanner() {
        scannerLauncher = registerForActivityResult(StartIntentSenderForResult()) { result ->
            handleActivityResult(result)
        }
    }

    /**
     * This function is used for handling the response from scanner
     */
    private fun handleActivityResult(activityResult: ActivityResult?) {
        val resultCode = activityResult?.resultCode
        val result = GmsDocumentScanningResult.fromActivityResultIntent(activityResult?.data)
        if (resultCode == Activity.RESULT_OK && result != null) {
            val pages = result.pages
            if (!pages.isNullOrEmpty()) {
            }

            result.pdf?.uri?.path?.let { path ->
                val externalUri =
                    FileProvider.getUriForFile(this, "$packageName.provider", File(path))
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_STREAM, externalUri)
                    type = "application/pdf"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(shareIntent, "share pdf"))
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
        } else {
        }
    }

    /**
     * This function is used to launch Doc Scanner
     */
    private fun launchScanner(mode: Int) {
        val options =
            GmsDocumentScannerOptions.Builder().setGalleryImportAllowed(true).setPageLimit(2)
                .setResultFormats(RESULT_FORMAT_JPEG, RESULT_FORMAT_PDF).setScannerMode(mode)
                .build()
        createDocScannerClient(options)
    }

    /**
     * This function is used to create the gms document scanning client
     */
    private fun createDocScannerClient(options: GmsDocumentScannerOptions) {
        GmsDocumentScanning.getClient(options).getStartScanIntent(this)
            .addOnSuccessListener { intentSender: IntentSender ->
                scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            }.addOnFailureListener() { e: Exception ->
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.tv_mode_full -> {
                launchScanner(SCANNER_MODE_FULL)
            }

            R.id.tv_mode_base -> {
                launchScanner(SCANNER_MODE_BASE)
            }

            R.id.tv_mode_with_filter -> {
                launchScanner(SCANNER_MODE_BASE_WITH_FILTER)
            }
        }
    }

}