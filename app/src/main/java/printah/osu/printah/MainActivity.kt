package printah.osu.printah

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent
import android.net.Uri


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val myIntent = intent       // gets the previously created intent
        val URIval = myIntent.getStringExtra("pdfURI")
        pdfView.fromUri(Uri.parse(URIval)).load()
        printButton.setOnClickListener{

            val pdfUri = URIval
            val intent = Intent(applicationContext,DisplayPrinterActivity::class.java)
            intent.putExtra("pdfURI",pdfUri.toString())
            startActivity(intent)

            Log.i("yolo","heyyyy")
            Toast.makeText(this@MainActivity,"You clicked the Print button biyaatch",Toast.LENGTH_SHORT).show()
        }
    }
}
