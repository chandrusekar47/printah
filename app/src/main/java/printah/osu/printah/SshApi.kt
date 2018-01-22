package printah.osu.printah

import android.util.Log
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream

class SshApi(private val username: String, private val password: String) {
    private val stdlinux = "stdlinux.cse.ohio-state.edu"
    private lateinit var session: Session

    fun login(): Boolean {
        val config = java.util.Properties()
        //accepts the key provided by the stdlinux box without checking
        config.put("StrictHostKeyChecking", "no")
        val jSch = JSch()
        val host = stdlinux
        session = jSch.getSession(username, host)
        session.setPassword(password)
        session.setConfig(config)
        return try {
            session.connect()
            Log.i(javaClass.name, "Login successful")
            session.isConnected
        } catch (e: Exception) {
            Log.e("Unable to login", e.message, e)
            false
        }
    }

    data class Result(val success: Boolean, val output: String, val error: String)

    private fun runCommand(command: String): Result {
        Log.i(javaClass.name, "Running command $command")
        var success = false
        val output = StringBuilder()
        val errorStream = ByteArrayOutputStream()
        try {
            val execChannel = session.openChannel("exec")
            (execChannel as ChannelExec).setCommand(command)
//          input to the command
            execChannel.setInputStream(null)
//          error from the command
            execChannel.setErrStream(errorStream)
//          output from the command
            val inputStream = execChannel.inputStream
            execChannel.connect()
            val tmp = ByteArray(1024 * 1024)
//          reads in steps of 1MB, need to check if java.util.Scanner can be used here
            while (true) {
                while (inputStream.available() > 0) {
                    val bytesRead = inputStream.read(tmp, 0, 1024 * 1024)
                    if (bytesRead < 0) break
//                  accumulate output
                    output.append(String(tmp, 0, bytesRead))
                }
//                channel closed after command completes
                if (execChannel.isClosed()) {
//                    read all remaining output
                    if (inputStream.available() > 0) continue
                    success = execChannel.exitStatus == 0
                    break
                }
                try {
                    Thread.sleep(1000)
                } catch (e: Exception) {
                }
            }
            execChannel.disconnect()
        } catch (e: Exception) {
            success = false
            errorStream.write(e.message?.toByteArray())
        } finally {
            return Result(success, output.toString(), errorStream.toString())
        }
    }

    private fun homedir(): Result {
        return runCommand("pwd")
    }

    fun scpFile(inputStream: InputStream): String {
        inputStream.use {
            val homeDir = homedir().output.trim('\n', '\t', ' ')
            Log.i(javaClass.name, "'$homeDir'")
            val remoteFileName = "print_job_" + (Math.random() * 10000).toInt()
            val separator = File.separator
            val remoteFileFullPath = "$homeDir$separator$remoteFileName"
            Log.i(javaClass.name, remoteFileFullPath)
            // exec 'scp -t rfile' remotely
            var command: String = "scp -t " + remoteFileFullPath
            val channel = session.openChannel("exec")
            (channel as ChannelExec).setCommand(command)
            val out = channel.getOutputStream()
            val input = channel.getInputStream()
            channel.connect()

            if (checkAck(input) != 0) {
                throw RuntimeException()
            }

            //not sure why scp is using this file name to create the file on the remote.
            val localFileName = remoteFileName
            // send "C0644 filesize filename\n", where filename should not include '/'
            val fileSize = inputStream.available() //todo: need to verify if this works for all cases
            command = "C0644 $fileSize $localFileName\n"
            out.write(command.toByteArray())
            out.flush()
            if (checkAck(input) != 0) {
                throw RuntimeException()
            }

            // send a content of localFileName
            val buf = ByteArray(1024)
            while (true) {
                val len = inputStream.read(buf, 0, buf.size)
                if (len <= 0) break
                out.write(buf, 0, len)
            }
            inputStream.close()
            buf[0] = 0
            out.write(buf, 0, 1)
            out.flush()
            if (checkAck(input) != 0) {
                throw RuntimeException()
            }
            out.close()

            channel.disconnect()
            return remoteFileFullPath
        }
    }

    @Throws(IOException::class)
    private fun checkAck(inputStream: InputStream): Int {
        val b = inputStream.read()
        // b may be 0 for success,
        //          1 for error,
        //          2 for fatal error,
        //          -1
        if (b == 0) return b
        if (b == -1) return b

        if (b == 1 || b == 2) {
            val sb = StringBuffer()
            var c: Int
            do {
                c = inputStream.read()
                sb.append(c.toChar())
            } while (c.toChar() != '\n')
            if (b == 1) { // error
                print(sb.toString())
            }
            if (b == 2) { // fatal error
                print(sb.toString())
            }
        }
        return b
    }

    fun printFile(desFileName: String, vararg lprOptions: String) {
        val optionsStr = lprOptions.joinToString(" ")
        Log.i(javaClass.name, "Filename: " + desFileName)
        val (success, _, error) = runCommand("lpr $optionsStr $desFileName")
        if (!success) {
            throw RuntimeException(error)
        }
    }

    fun close() {
        session.disconnect()
    }
}