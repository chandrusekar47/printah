package printah.osu.printah

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.os.Environment
import android.util.Log
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.os.Build
import android.content.BroadcastReceiver
import android.content.IntentFilter


private val TAG = MainActivity::class.java.simpleName

class UrlSelectionActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_url_selection)
        registerReceiver(onComplete,
                 IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    override fun onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(onComplete);
    }

    fun getURL(view: View) {
        if(isStoragePermissionGranted()){
            useDownloadManager()
        }
    }

    private fun isStoragePermissionGranted(): Boolean {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted")
                return true
            } else {
                Log.v(TAG, "Permission is revoked")
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                return false
            }
        } else {
            Log.v(TAG, "Permission is granted")
            return true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            useDownloadManager()
        }
    }

    private fun useDownloadManager() {
        val editText = findViewById<EditText>(R.id.editText)
        val pdfUrl = editText.text.toString()
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(pdfUrl))

        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setAllowedOverRoaming(false)
        request.setDescription("Downloading the file")
        request.setVisibleInDownloadsUi(true)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,      "Sample" + ".pdf")

        downloadManager.enqueue(request)
    }

    var onComplete: BroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(ctxt: Context, intent: Intent) {

            val referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

//            val intent = Intent(applicationContext,MainActivity::class.java).apply {
//                putExtra("pdfURI",pdfUrl)
//            }
//            startActivity(intent

        }
    }
}
