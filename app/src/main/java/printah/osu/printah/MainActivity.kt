package printah.osu.printah

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
       pdfView.fromAsset("sample3.pdf").load()
        printButton.setOnClickListener{
            Log.i("yolo","heyyyy")
            Toast.makeText(this@MainActivity,"You clicked the Print button biyaatch",Toast.LENGTH_SHORT).show()
        }
    }
}
