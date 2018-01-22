package printah.osu.printah

import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_print_options.*
import java.io.File

class PrintOptionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_print_options)
        sidesSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayOf(PrintSidesOptions.OneSided, PrintSidesOptions.TwoSided))
        orientationSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayOf(PrintOrientationOptions.Portrait, PrintOrientationOptions.Landscape))
        printConfirmButton.setOnClickListener { onPrint() }
        numberOfCopies.setText("1")
        printAgainButton.setOnClickListener({ onPrint() })
        switchPrinterButton.setOnClickListener({ switchPrinter() })
    }

    private fun switchPrinter() {

    }

    private fun onPrint() {
        val sidesOptions = sidesSpinner.selectedItem as PrintSidesOptions
        val orientationOption = orientationSpinner.selectedItem as PrintOrientationOptions
        val numberOfCopies = numberOfCopies.text.toString().toInt()
        val myIntent = intent
        val fileToPrintUri = myIntent.getStringExtra("pdfURI")
        val printerName = myIntent.getStringExtra("printerUri")

        //val fileToPrintUri = Uri.fromFile(File("//android_asset/sample.pdf"))
        //val printerName = "lj_dl_395_a"
        showProgressIndicator()
        val sharedPreferences = getSharedPreferences(LoginActivity.preferencesFile, Context.MODE_PRIVATE)
        val username = sharedPreferences.getString(LoginActivity.userNameKey, "")
        val password = sharedPreferences.getString(LoginActivity.passwordKey, "")
        PrintAsyncTask(username,
                password,
                Uri.parse(fileToPrintUri),
                "-P", printerName,
                "-#", numberOfCopies.toString(),
                "-o", orientationOption.lprParameter,
                "-o", "sides=" + sidesOptions.lprParameter).execute()
    }

    private fun showProgressIndicator() {
        printing_overlay.visibility = View.VISIBLE
        progressText.setText(R.string.file_copy_progress_message)
    }

    internal fun updateProgressBar(progress: Array<out Int?>) {
        progress.filterNotNull().forEach { i: Int ->
            if (i > 50)
                progressText.setText(R.string.print_progress_message)
            progressBar.incrementProgressBy(i)
        }
    }

    internal fun onPrintingFailed() {
        failure_panel.visibility = View.VISIBLE
        progressText.setText(R.string.print_failed_message)
    }

    internal fun onPrintingSuccess() {
        progressText.setText(R.string.print_success_message)
        val homeActivityIntent = Intent(this, Main2Activity::class.java)
        homeActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Handler().postDelayed({ startActivity(homeActivityIntent) }, 800)
    }

    inner class PrintAsyncTask internal constructor(private val username: String,
                                                    private val password: String,
                                                    private val sourceFile: Uri,
                                                    vararg private val lprOptions: String) : AsyncTask<Void, Int, Boolean>() {
        override fun doInBackground(vararg params: Void?): Boolean {
            return try {
                val sshApi = SshApi(username, password)
                sshApi.login()
                publishProgress(25)
                val inputStream = contentResolver.openInputStream(sourceFile)
                val destFileName: String = sshApi.scpFile(inputStream)
                publishProgress(50)
                sshApi.printFile(destFileName, *lprOptions)
                publishProgress(90)
                sshApi.close()
                publishProgress(100)
                true
            } catch (e: Exception) {
                Log.e(javaClass.name, "Printing failed", e)
                false
            }
        }

        override fun onProgressUpdate(vararg values: Int?) {
            updateProgressBar(values)
        }

        override fun onPostExecute(result: Boolean?) {
            if (result != null && result) {
                onPrintingSuccess()
            } else {
                onPrintingFailed()
            }
        }
    }

}
