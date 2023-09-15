package no.kristiania.echosimple

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import okio.ByteString.Companion.decodeHex
import java.security.MessageDigest

fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }


class MainActivity : ComponentActivity() {
    val TAG = "MainActivity"
    var ws: WebSocket? = null

    // https://developer.android.com/guide/components/activities/activity-lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Lifecycle - onCreate()")

        setContent {
            val context = LocalContext.current
            Column {
                Text("$TAG has started.")
                Button(onClick = {
                    Toast.makeText(context, "This is how to do a Toast in Jetpack Compose", Toast.LENGTH_LONG).show()
                }) {
                    Text("Skål Admiral von Schneider!")
                }
            }

            Toast.makeText(context, "Moo", Toast.LENGTH_LONG).show()
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "Lifecycle - onStart()")

        startWebSocket()
        testWebSocket()
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "Lifecycle - onStop()")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "Lifecycle - onPause()")

        stopWebSocket()
    }

    private fun startWebSocket() {
        val ws_url = "ws://192.168.50.79:8765"
        val wsRequest: Request = Request.Builder().url(ws_url).build()
        ws = OkHttpClient().newWebSocket(wsRequest, EchoWebSocketListener())
    }

    private fun stopWebSocket() {
        ws?.close(code = 1000, reason = "Client is exiting.")
        ws = null
    }

    private fun testWebSocket() {
        ws?.send("The Eagle has landed.")
        ws?.send(text = "A termite walks into a bar and asks, \"is the bartender here?\"")
        ws?.send(bytes = "deadbeefcafebabefeedface".decodeHex())

        val md5 = MessageDigest.getInstance("md5")
        md5.update("A message to you Rudy.".toByteArray())
        val digest = md5.digest()
        ws?.send(bytes = digest.toHex().decodeHex())
    }
}


private class EchoWebSocketListener : WebSocketListener() {
    val TAG = "EchoWebSocketListener"

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d(TAG, "WSL onOpen")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d(TAG, "WSL onMessage text:")

        output("Receiving text: $text")
    }

    // This will be unused in this assignment, but we'll leave it here
    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        Log.d(TAG, "WSL onMessage bytes:")

        output("Receiving bytes: " + bytes.hex())
    }


    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        Log.d(TAG, "WSL onClosing")

        webSocket.close(NORMAL_CLOSURE_STATUS, null)
        output("Closing : $code / $reason")
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        Log.d(TAG, "WSL onClosed")
    }

    companion object {
        private val NORMAL_CLOSURE_STATUS = 1000
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.d(TAG, "WSL onFailure")
        Log.e(TAG, "${t.message}")
    }

    private fun output(txt: String) {
        Log.d(TAG, txt)
    }
}