package com.example.servicesapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.core.app.ActivityCompat
import androidx.compose.ui.platform.LocalContext
import com.example.servicesapp.services.DownloadService
import com.example.servicesapp.ui.theme.ServicesAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // ✅ Call the superclass method
        setContent {
            ServicesAppTheme { // Ensure this theme exists
                PDFDownloadScreen()
            }
        }
    }
}

@Composable
fun PDFDownloadScreen() {
    var pdf1 by remember { mutableStateOf("") }
    var pdf2 by remember { mutableStateOf("") }
    var pdf3 by remember { mutableStateOf("") }
    var pdf4 by remember { mutableStateOf("") }
    var pdf5 by remember { mutableStateOf("") }

    val context = LocalContext.current

    // ✅ FIX: Correct Import for rememberLauncherForActivityResult
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Permission granted! Please retry download.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permission denied. Cannot download files.", Toast.LENGTH_SHORT).show()
        }
    }

    fun startDownload() {
        val urls = listOf(pdf1, pdf2, pdf3, pdf4, pdf5).filter { it.isNotEmpty() }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                return
            }
        }

        val intent = Intent(context, DownloadService::class.java)
        intent.putStringArrayListExtra("PDF_URLS", ArrayList(urls))
        context.startService(intent)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1B4B5A))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "App Icon",
            modifier = Modifier.size(64.dp),
            tint = Color.Unspecified
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "PDF Download Activity",
            fontSize = 22.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Enter PDF URLs to download:",
            fontSize = 14.sp,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        PDFInputField("PDF1 File Location:", pdf1) { pdf1 = it }
        PDFInputField("PDF2 File Location:", pdf2) { pdf2 = it }
        PDFInputField("PDF3 File Location:", pdf3) { pdf3 = it }
        PDFInputField("PDF4 File Location:", pdf4) { pdf4 = it }
        PDFInputField("PDF5 File Location:", pdf5) { pdf5 = it }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { startDownload() },
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF7C0)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Start download",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PDFInputField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 14.sp, color = Color.White)
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, shape = RoundedCornerShape(8.dp)),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

const val REQUEST_WRITE_STORAGE_PERMISSION = 101
