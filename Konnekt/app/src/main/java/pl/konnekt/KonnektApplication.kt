import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import pl.konnekt.network.UnsafeOkHttpClient

class KonnektApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient(UnsafeOkHttpClient.getUnsafeOkHttpClient())
            .build()
    }
}