package com.generic.qrcode

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.generic.camerax.R

class QrcodePdf : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode_pdf)


        openPdfPicker {
            if (it != null) {
                Toast.makeText(this, "PDF selecionado: $it", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Nenhum PDF selecionado", Toast.LENGTH_SHORT).show()
            }
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
}