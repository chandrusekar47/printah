package printah.osu.printah

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.renderscript.ScriptGroup
import android.support.annotation.IntegerRes
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.*

import kotlinx.android.synthetic.main.activity_display_printer.*
import java.io.File
import java.io.InputStream
import printah.osu.printah.R.mipmap.*


class DisplayPrinterActivity : AppCompatActivity(){

        inner class backgroundTask() : AsyncTask<Void, Void, String>() {
            override fun doInBackground(vararg params: Void?): String? {
                // ...
                Thread.sleep(100)
                Log.i("asyncTask","msg from Async task")
                return "hello world"
            }

            override fun onPreExecute() {
                super.onPreExecute()
                // ...
            }

            override fun onPostExecute(result: String?) {
                super.onPostExecute(result)
                // ...
            }
        }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_printer)

        readFromPrinterCSV()
        createCustomView()
        backgroundTask().execute()

    }

    private var sa: SimpleAdapter? = null
    private val po : MutableList<printerDeets> = ArrayList<printerDeets>() //contains building, room nums, printer names and stuff!
    var csvLines = mutableListOf<String>()
    var csvlist = ArrayList<HashMap<String, String>>()


    fun createCustomView()
    {
        //images - busy or free
        var imgid = ArrayList<Int>()
        for (i in 0 until po.size)
        {
           imgid.add(R.mipmap.free)
        }

        var item: HashMap<String, String>
        for (i in 0 until po.size)
        {
            item = HashMap()
            item.put("line1", po[i].getBuilding()+" "+po[i].getRoom())
            item.put("line2", po[i].getPrinterName())
            item.put("line3", po[i].getType())
            item.put("line4",R.mipmap.free.toString())
            csvlist.add(item)
        }
        Log.i("from view","msg from the custom view")


        val adapter = CustomListAdapter(this, csvlist)
        printerListView.adapter = adapter

        /*
        //configuring new adapter
        sa = SimpleAdbapter(this, csvlist,
                R.layout.printer_layout,
                arrayOf("line1","line4","line2","line3"),
                intArrayOf(R.id.building,R.id.icon, R.id.room, R.id.printer))


        //linking
        printerListView.adapter = sa
        */

        printerListView.onItemClickListener = AdapterView.OnItemClickListener{adapterView, view, position, id ->

            val sb = StringBuilder("Selected:\n")
            val selected = printerListView.getItemAtPosition(position as Int) as HashMap<String, String>

            sb.append(selected["line1"] + "\n")
            sb.append(selected["line2"] + "\n")
            sb.append(selected["line3"] + "\n")
            Toast.makeText(applicationContext, sb.toString(), Toast.LENGTH_SHORT).show()

            val myIntent = intent       // gets the previously created intent
            val URIval = myIntent.getStringExtra("pdfURI")
            val printerName = selected["line2"]
            val intent = Intent(applicationContext,PrintOptionsActivity::class.java)
            intent.putExtra("printerUri",printerName.toString())
            intent.putExtra("pdfURI",URIval.toString())

            startActivity(intent)
        }


    }

    fun readFromPrinterCSV()
    {
        val inputstream : InputStream =  resources.openRawResource(R.raw.printers).buffered()
        val lineList = mutableListOf<String>()
        csvLines = lineList

        inputstream.bufferedReader().useLines {
            lines ->lines.forEach {lineList.add(it)}
        }
        lineList.removeAt(0) //remove headings

        lineList.forEach {
            val a = (it.split(","))
            val obj : printerDeets = printerDeets()
            obj.setBuilding(a[0])
            obj.setRoom(a[1])
            obj.setPrinterName(a[2])
            obj.setType(a[3])
            po.add(obj)

        }
    }


}
