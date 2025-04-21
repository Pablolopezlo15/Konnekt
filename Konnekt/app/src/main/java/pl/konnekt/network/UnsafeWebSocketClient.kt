package pl.konnekt.network

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.security.cert.X509Certificate

class UnsafeWebSocketClient(
    serverUri: URI,
    private val onOpen: () -> Unit,
    private val onMessage: (String) -> Unit,
    private val onClose: (Int, String, Boolean) -> Unit,
    private val onError: (Exception) -> Unit
) : WebSocketClient(serverUri) {

    init {
        val trustAllCerts: Array<TrustManager> = arrayOf(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
        })

        val sslContext: SSLContext = SSLContext.getInstance("TLS").apply {
            init(null, trustAllCerts, java.security.SecureRandom())
        }
        
        setSocketFactory(sslContext.socketFactory)
    }

    override fun onOpen(handshakedata: ServerHandshake?) {
        onOpen.invoke()
    }

    override fun onMessage(message: String) {
        onMessage.invoke(message)
    }

    override fun onClose(code: Int, reason: String, remote: Boolean) {
        onClose.invoke(code, reason, remote)
    }

    override fun onError(ex: Exception) {
        onError.invoke(ex)
    }
}