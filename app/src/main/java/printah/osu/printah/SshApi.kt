package printah.osu.printah

import android.util.Log
import com.jcraft.jsch.JSch

class SshApi {
    private val stdlinux = "stdlinux.cse.ohio-state.edu"

    fun isUserValid(username: String, password: String): Boolean {
        val config = java.util.Properties()
        //accepts the key provided by the stdlinux box without checking
        config.put("StrictHostKeyChecking", "no")
        val jSch = JSch()
        val host = stdlinux
        val session = jSch.getSession(username, host)
        session.setPassword(password)
        session.setConfig(config)
        return try {
            session.connect()
            Log.i(javaClass.name, "Login successful")
            val connected = session.isConnected
            session.disconnect()
            connected
        } catch (e: Exception) {
            Log.e("Unable to login", e.message, e)
            false
        }
    }



}