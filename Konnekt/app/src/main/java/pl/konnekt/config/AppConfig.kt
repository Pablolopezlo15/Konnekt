package pl.konnekt.config

object AppConfig {
    //AWS 54.145.77.55 - LOCAL 192.168.1.54:8000
    const val IP = "192.168.1.41:8000"
    val WEBSOCKET_URI = "wss://$IP"
    val BASE_URL = "https://$IP"
}