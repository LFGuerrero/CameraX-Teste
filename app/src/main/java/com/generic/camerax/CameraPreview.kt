package com.generic.camerax

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraXConfig
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController.COORDINATE_SYSTEM_VIEW_REFERENCED
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.generic.camerax.Commons.BARCODE_EXTRA
import com.generic.camerax.databinding.CameraPreviewBinding
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode

class CameraPreview : AppCompatActivity(), CameraXConfig.Provider {

    private val binding by lazy { CameraPreviewBinding.inflate(layoutInflater) }
    private lateinit var barcodeScanner: BarcodeScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        setBarcodeCameraRead()
    }

    /**
     * Inicializa a camera e faz a analise da imagem capturada
     * para encontrar um codigo de barras valido
     */
    private fun setBarcodeCameraRead() {
        val cameraController = LifecycleCameraController(baseContext)
        val previewView: PreviewView = binding.cameraPreview

        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()

        barcodeScanner = BarcodeScanning.getClient(options)

        cameraController.setImageAnalysisAnalyzer(
            ContextCompat.getMainExecutor(this),
            MlKitAnalyzer(
                listOf(barcodeScanner),
                COORDINATE_SYSTEM_VIEW_REFERENCED,
                ContextCompat.getMainExecutor(this)
            ) { result: MlKitAnalyzer.Result? ->
                val barcodeResults = result?.getValue(barcodeScanner)
                when {
                    barcodeResults == null || barcodeResults.size == 0 || barcodeResults.first() == null -> return@MlKitAnalyzer

                    barcodeResults[0].rawValue?.isValidBarcode() == true -> {
                        val resultIntent = Intent()
                        resultIntent.putExtra(BARCODE_EXTRA, barcodeResults[0].rawValue)

                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    }

                    else -> return@MlKitAnalyzer
                }
            }
        )

        cameraController.bindToLifecycle(this)
        previewView.controller = cameraController
    }

    /**
     * Valida o numero de caracteres do codigo de barras
     */
    private fun String.isValidBarcode(): Boolean = this.length in 36..48

    /**
     * Finaliza o detector de codigo de barras
     */
    override fun onDestroy() {
        super.onDestroy()
        barcodeScanner.close()
    }

    /**
     * Escolher a camera manualmente pode reduzir o tempo de inicializaçao da camera
     */
    override fun getCameraXConfig(): CameraXConfig {
        return CameraXConfig.Builder.fromConfig(Camera2Config.defaultConfig())
            .setAvailableCamerasLimiter(CameraSelector.DEFAULT_BACK_CAMERA)
            .setMinimumLoggingLevel(Log.ERROR)
            .build()
    }
}