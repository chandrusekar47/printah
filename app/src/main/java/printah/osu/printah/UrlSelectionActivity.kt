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
import android.database.Cursor
import android.view.Gravity
import android.widget.Button
import android.widget.Toast


private val tag = MainActivity::class.java.simpleName

class UrlSelectionActivity : AppCompatActivity() {

    var list: ArrayList<Long> = ArrayList()
    lateinit var downloadManager : DownloadManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_url_selection)
        registerReceiver(onComplete,
                 IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(onComplete)
    }

    fun downloadURL(view: View) {
        if(isStoragePermissionGranted()){
            useDownloadManager()

            val statusButton = findViewById<Button>(R.id.statusButton)
            statusButton.setVisibility(View.VISIBLE)
            val cancelButton = findViewById<Button>(R.id.cancelButton)
            cancelButton.setVisibility(View.VISIBLE)
        }
    }

    fun checkStatus(view: View) {

        val refId = list[list.size-1]

        val downloadQuery = DownloadManager.Query()
        downloadQuery.setFilterById(refId)

        val cursor = downloadManager.query(downloadQuery)
        if (cursor.moveToFirst()) {
            downloadStatus(cursor)
        }
    }

    fun cancelDownload(view: View){
        val refId = list[list.size-1]
        downloadManager.remove(refId)
    }

    fun hideButtons(){
        val statusButton = findViewById<Button>(R.id.statusButton)
        statusButton.setVisibility(View.INVISIBLE)
        val cancelButton = findViewById<Button>(R.id.cancelButton)
        cancelButton.setVisibility(View.INVISIBLE)
    }

    private fun isStoragePermissionGranted(): Boolean {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.v(tag, "Permission is granted")
                return true
            } else {
                Log.v(tag, "Permission is revoked")
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                return false
            }
        } else {
            Log.v(tag, "Permission is granted")
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
        val pdfSplit = pdfUrl.split("/")
        val pdfName = pdfSplit[pdfSplit.size-1]

        val request = DownloadManager.Request(Uri.parse(pdfUrl))
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setAllowedOverRoaming(false)
        request.setDescription("Downloading the file")
        request.setTitle(pdfName)
        request.setVisibleInDownloadsUi(true)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,pdfName)

        val refId = downloadManager.enqueue(request)
        list.add(refId)

    }

    private var onComplete: BroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(ctxt: Context, intent: Intent) {

            val refId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            list.remove(refId)

            val pdfUri = downloadManager.getUriForDownloadedFile(refId)
            if(pdfUri!=null){
                val intentNew = Intent(applicationContext,MainActivity::class.java).apply {
                    putExtra("pdfURI",pdfUri.toString())
                }
                startActivity(intentNew)
            }
            hideButtons()
        }
    }

    private fun downloadStatus(cursor: Cursor) {

//      column for download  status
        val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
        val status = cursor.getInt(columnIndex)

//      column for reason code if the download failed or paused
        val columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
        val reason = cursor.getInt(columnReason)

         //get the download filename
        val filenameIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)
        val filename = cursor.getString(filenameIndex)

        var statusText = ""
        var reasonText = ""

        when (status) {
            DownloadManager.STATUS_FAILED -> {
                statusText = "STATUS_FAILED"
                when (reason) {
                    DownloadManager.ERROR_CANNOT_RESUME -> reasonText = "ERROR_CANNOT_RESUME"
                    DownloadManager.ERROR_DEVICE_NOT_FOUND -> reasonText = "ERROR_DEVICE_NOT_FOUND"
                    DownloadManager.ERROR_FILE_ALREADY_EXISTS -> reasonText = "ERROR_FILE_ALREADY_EXISTS"
                    DownloadManager.ERROR_FILE_ERROR -> reasonText = "ERROR_FILE_ERROR"
                    DownloadManager.ERROR_HTTP_DATA_ERROR -> reasonText = "ERROR_HTTP_DATA_ERROR"
                    DownloadManager.ERROR_INSUFFICIENT_SPACE -> reasonText = "ERROR_INSUFFICIENT_SPACE"
                    DownloadManager.ERROR_TOO_MANY_REDIRECTS -> reasonText = "ERROR_TOO_MANY_REDIRECTS"
                    DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> reasonText = "ERROR_UNHANDLED_HTTP_CODE"
                    DownloadManager.ERROR_UNKNOWN -> reasonText = "ERROR_UNKNOWN"
                }
            }
            DownloadManager.STATUS_PAUSED -> {
                statusText = "STATUS_PAUSED"
                when (reason) {
                    DownloadManager.PAUSED_QUEUED_FOR_WIFI -> reasonText = "PAUSED_QUEUED_FOR_WIFI"
                    DownloadManager.PAUSED_UNKNOWN -> reasonText = "PAUSED_UNKNOWN"
                    DownloadManager.PAUSED_WAITING_FOR_NETWORK -> reasonText = "PAUSED_WAITING_FOR_NETWORK"
                    DownloadManager.PAUSED_WAITING_TO_RETRY -> reasonText = "PAUSED_WAITING_TO_RETRY"
                }
            }
            DownloadManager.STATUS_PENDING -> statusText = "STATUS_PENDING"
            DownloadManager.STATUS_RUNNING -> statusText = "STATUS_RUNNING"
            DownloadManager.STATUS_SUCCESSFUL -> {
                statusText = "STATUS_SUCCESSFUL"
                reasonText = "Filename:\n" + filename
            }
        }

        val toast = Toast.makeText(this@UrlSelectionActivity,
            ("Download Status:" + "\n" + statusText + "\n" + reasonText),
            Toast.LENGTH_LONG)
        toast.setGravity(Gravity.TOP, 25, 400)
        toast.show()

    }
}
