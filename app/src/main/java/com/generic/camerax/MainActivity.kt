package com.generic.camerax

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.generic.camerax.Commons.BARCODE_EXTRA
import com.generic.camerax.databinding.ActivityMainBinding
import com.generic.qrcode.QrcodePdf

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    /**
     * Retorno vindo da leitura da camera
     */
    private val cameraResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val resultado = result.data?.getStringExtra(BARCODE_EXTRA)
            binding.tvBarcodePreview.text = resultado ?: ""

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCallQrcode.setOnClickListener {
            initQrcode()
        }

        binding.btnCallCamera.setOnClickListener {
            initCamera()
        }

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun initQrcode() {
        val qrcodeIntent = Intent(this, QrcodePdf::class.java)
        startActivity(qrcodeIntent)
    }

    /**
     * Chama a tela da camera e espera pelo retorno
     */
    private fun initCamera() {
        val cameraIntent = Intent(this, CameraPreview::class.java)
        cameraResult.launch(cameraIntent)
    }

    /**
     * Retorno da permissao
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
//                initCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    /**
     * Valida se tem a permissao de acessar a camera
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA
            ).toTypedArray()
    }
}