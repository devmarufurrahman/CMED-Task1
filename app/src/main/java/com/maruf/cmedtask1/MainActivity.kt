package com.maruf.cmedtask1

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity() {
    val STORAGE_DIRECTORY = "/Download/TestFolder/";
    private lateinit var progressBar: ProgressBar
    private lateinit var progressTv: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val downloadBtn = findViewById<MaterialButton>(R.id.downloadBtn);
         progressBar = findViewById(R.id.progressBar);
        progressTv = findViewById(R.id.progressTv)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_MEDIA_IMAGES,
                    android.Manifest.permission.READ_MEDIA_VIDEO,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ),
                1
            )
        } else {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(

                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                ),
                1
            )
        }




        downloadBtn.setOnClickListener{
            val fileName = System.currentTimeMillis().toString()
            downloadFile("https://jsoncompare.org/LearningContainer/SampleFiles/PDF/sample-pdf-with-images.pdf",fileName)
//            https://jsoncompare.org/LearningContainer/SampleFiles/PDF/sample-pdf-with-images.pdf
//            https://research.nhm.org/pdfs/10840/10840-001.pdf
        }

    }

    fun downloadFile(murl : String, fileName:String){
        val storageDirectory = Environment.getExternalStorageDirectory().toString() + STORAGE_DIRECTORY + "/${fileName}"
        val file  = File(Environment.getExternalStorageDirectory().toString() +STORAGE_DIRECTORY)
        if (!file.exists()){
            file.mkdirs()
        }

        GlobalScope.launch(Dispatchers.IO) {
            val url = URL(murl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept-Encoding", "identity")
            connection.connect()

            if (connection.responseCode in 200 .. 299){
                val fileSize = connection.contentLength.toLong()
                withContext(Dispatchers.Main){
                    progressBar.progress = 0
                }

                val inputStream = connection.inputStream
                val outputStream = FileOutputStream(storageDirectory)

                var bytesCopied: Long = 0;
                var buffer = ByteArray(1024)
                var bytes = inputStream.read(buffer)
                while (bytes > 0){
                    bytesCopied += bytes
                    val downloadProgress =((bytesCopied.toDouble() / fileSize) * 100).toInt()
                    withContext(Dispatchers.Main){
                        progressBar.progress = downloadProgress
                        progressTv.text = "$downloadProgress"
                    }
//                    Log.d("count download", "downloadFile: $downloadProgress")

                    outputStream.write(buffer, 0, bytes)
                    bytes = inputStream.read(buffer)
                }
                outputStream.close()
                inputStream.close()


                // Download complete notification using DownloadManager
                val request = DownloadManager.Request(Uri.parse(murl))
                request.setTitle(fileName)
                request.setDescription("Downloading $fileName")
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                request.allowScanningByMediaScanner()
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                manager.enqueue(request)

            } else{
                withContext(Dispatchers.Main){
                    Toast.makeText(this@MainActivity, "Not Successful", Toast.LENGTH_SHORT).show()
                }

            }

        }

//        val request = DownloadManager.Request(Uri.parse(murl))
//        request.setTitle(fileName)
//        request.setDescription("Downloading $fileName")
//
//        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
//        request.allowScanningByMediaScanner()
//
//        val manager = this.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//        manager.enqueue(request)

    }

}