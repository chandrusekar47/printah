package printah.osu.printah

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.view.View


class Main2Activity : AppCompatActivity() {

    private val SELECT_FILE_REQUEST = 1  // The request code
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
    }

    fun openFolder(view: View) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        val uri = Uri.parse(Environment.getExternalStorageDirectory().path)
        intent.setDataAndType(uri, "*/*")
        startActivityForResult(Intent.createChooser(intent, "Open folder"),SELECT_FILE_REQUEST)
    }

    fun specifyURL(view: View) {
        val intent = Intent(applicationContext,UrlSelectionActivity::class.java)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Check which request we're responding to
        if (requestCode == SELECT_FILE_REQUEST) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {
                val pdfUri = data!!.data
                val intent = Intent(applicationContext,MainActivity::class.java)
                intent.putExtra("pdfURI",pdfUri.toString())
                startActivity(intent)
            }
        }
    }
}
