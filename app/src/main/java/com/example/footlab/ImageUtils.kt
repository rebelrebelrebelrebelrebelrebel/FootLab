import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ImageUtils {
    fun saveBase64Image(context: Context, base64String: String?): String? {
        // Decode the base64 string to byte array
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)


        // Create a bitmap from the byte array
        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)


        // Specify the file path to save the image
        val imageFile = File(context.getExternalFilesDir(null), "classified_image.png")

        try {
            FileOutputStream(imageFile).use { fos ->
                bitmap.compress(
                    Bitmap.CompressFormat.PNG,
                    100,
                    fos
                ) // Save bitmap as PNG
                fos.flush()
                return imageFile.absolutePath // Return the image URL
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }
}