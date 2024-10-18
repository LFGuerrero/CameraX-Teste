package com.generic.qrcode

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.generic.camerax.R
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.io.IOException

class QrcodePdf : AppCompatActivity() {

    private val tvQrcodePreview by lazy { findViewById<TextView>(R.id.tv_qrcode_preview) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode_pdf)

        openPdfPicker {
            if (it != null) {
                val bitmap = pdfToBitmap(it)
                if (bitmap != null)
                    readQrcodeFromFile(bitmap)
            } else {
                Toast.makeText(this, "Nenhum PDF selecionado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun readQrcodeFromFile(bitmap: Bitmap) {
        scanQrCodeFromBitmap(bitmap) {
            tvQrcodePreview.text = getString(R.string.qrcode_detectado_s, it)
        }
    }

    private fun openPdfPicker(callback: (Uri?) -> Unit) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
        }

        val launcher = this.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                callback(result.data?.data)
            } else {
                callback(null)
            }
        }

        launcher.launch(intent)
    }

    private fun pdfToBitmap(uri: Uri): Bitmap? {
        try {
            val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r") ?: return null
            val pdfRenderer = PdfRenderer(parcelFileDescriptor)
            val page = pdfRenderer.openPage(0)
            val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            pdfRenderer.close()
            parcelFileDescriptor.close()
            return rescaleBitmap(bitmap, 0.5f)
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    private fun scanQrCodeFromBitmap(bitmap: Bitmap, callback: (String?) -> Unit) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()
        val scanner: BarcodeScanner = BarcodeScanning.getClient(options)

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    barcodes.forEachIndexed { index, barcode ->
                        Log.v("READER:", "RESULT $index: ${barcode.rawValue}")
                    }
                    callback(barcodes[0].rawValue)
                } else {
                    Log.v("READER:", "NO RESULT")
                    callback(null)
                }
            }
            .addOnFailureListener {
                it.printStackTrace()
                callback(null)
            }
    }

    private fun rescaleBitmap(bitmap: Bitmap, factor: Float): Bitmap {
        val newWidth = (bitmap.width * factor).toInt()
        val newHeight = (bitmap.height * factor).toInt()
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}