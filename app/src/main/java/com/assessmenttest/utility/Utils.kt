package com.assessmenttest.utility

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.text.TextUtils
import android.util.DisplayMetrics
import android.widget.Toast
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import androidx.sqlite.db.SimpleSQLiteQuery
import com.assessmenttest.database.AppDatabase
import com.assessmenttest.models.TravellingData
import com.assessmenttest.ui.MainApp
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.vision.barcode.Barcode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.*
import java.security.GeneralSecurityException
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory


object Utils {

    @Throws(GeneralSecurityException::class, IOException::class)
    fun getEncryptedSharedPreferences(): SharedPreferences? {
        val masterKeyAlias: String = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedSharedPreferences.create(
            "com.assessment.preferences",
            masterKeyAlias,
            MainApp.getContext(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun importCSV() {
        var reader =
            BufferedReader(InputStreamReader(MainApp.getContext().assets?.open("assessment.csv")));

        val csvReader =
            CSVReader(reader)/* path of local storage (it should be your csv file locatioin)*/
        var nextLine: Array<String>? = null
        var count = 0
        val columns = StringBuilder()
        GlobalScope.launch(Dispatchers.IO) {
            do {
                val value = StringBuilder()
                nextLine = csvReader.readNext()
                nextLine?.let { nextLine ->
                    for (i in 1 until nextLine.size - 1) {
                        if (count == 0) {                             // the count==0 part only read
                            if (i == nextLine.size - 2) {             //your csv file column name
                                columns.append(nextLine[i])
                                count = 1
                            } else
                                columns.append(nextLine[i]).append(",")
                        } else {                         // this part is for reading value of each row
                            if (i == nextLine.size - 2) {
                                value.append("'").append(nextLine[i]).append("'")
                                count = 2
                            } else
                                value.append("'").append(nextLine[i]).append("',")
                        }
                    }
                    if (count == 2) {
                        if (!value.isNullOrEmpty())
                            pushCustomerData(
                                columns,
                                value
                            )//write here your code to insert all values
                    }
                }
            } while ((nextLine) != null)
        }

        Toast.makeText(MainApp.getContext(), "Imported SuccessFully", Toast.LENGTH_SHORT).show()
    }

    private suspend fun pushCustomerData(columns: StringBuilder, values: StringBuilder) =
        withContext(Dispatchers.IO) {
            val query = SimpleSQLiteQuery(
                "INSERT INTO travelling (date,street,postal_code,city) values($values)",
                arrayOf()
            )

            AppDatabase.getDatabase(MainApp.getContext())?.travelDao()?.insertDataRawFormat(query)
        }


    fun isLocationEnabled(context: Context): Boolean {
        var locationMode = 0
        val locationProviders: String
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            locationMode = try {
                Settings.Secure.getInt(
                    context.contentResolver,
                    Settings.Secure.LOCATION_MODE
                )
            } catch (e: SettingNotFoundException) {
                e.printStackTrace()
                return false
            }
            locationMode != Settings.Secure.LOCATION_MODE_OFF
        } else {
            locationProviders = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.LOCATION_PROVIDERS_ALLOWED
            )
            !TextUtils.isEmpty(locationProviders)
        }
    }


    fun getLocationFromAddress(context: Context?, strAddress: String?): LatLng? {
        val coder = Geocoder(context)
        val address: List<Address>?
        var points: LatLng? = null
        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5)
            if (address == null) {
                return null
            }
            points = if (address.isNotEmpty()) {
                val location: Address = address[0]
                LatLng(location.latitude, location.longitude)
            } else LatLng(0.0, 0.0)
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        return points
    }

    fun getMarkerIconFromDrawable(drawable: Drawable): BitmapDescriptor? {
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    fun calculateDistance(
        startLatLng: LatLng,
        endLatLng: LatLng,
        streetLocStart: String,
        streetLocEnd: String
    ): Double {
        val startPoint = Location(streetLocStart)
        startPoint.latitude = startLatLng.latitude
        startPoint.longitude = startLatLng.longitude

        val endPoint = Location(streetLocEnd)
        endPoint.latitude = endLatLng.latitude
        endPoint.longitude = endLatLng.longitude

        return startPoint.distanceTo(endPoint).toDouble()
    }

    @SuppressLint("RestrictedApi")
    fun parseLocationResult(result: JSONObject): ArrayList<Any> {

        val tag = arrayOf("lat", "lng")
        var listOfGeopoints = ArrayList<Any>()
        val str: String = result.toString()
        val input: InputStream = ByteArrayInputStream(str.toByteArray())
        val builder: DocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc: Document? = builder.parse(input)
        if (doc != null) {
            val nl1: NodeList = doc.getElementsByTagName(tag[0])
            val nl2: NodeList = doc.getElementsByTagName(tag[1])
            if (nl1.length > 0) {
                listOfGeopoints = ArrayList()
                for (i in 0 until nl1.length) {
                    val node1: Node = nl1.item(i)
                    val node2: Node = nl2.item(i)
                    val lat: Double = node1.textContent.toDouble()
                    val lng: Double = node2.textContent.toDouble()
                    listOfGeopoints.add(
                        Barcode.GeoPoint(
                            (lat * 1E6).toInt().toDouble(),
                            (lng * 1E6).toInt().toDouble()
                        )
                    )
                }
            } else {
                // No points found
            }
        }

        return listOfGeopoints
    }

    fun dpToPx(dp: Int): Int {
        val displayMetrics: DisplayMetrics =
            MainApp.getContext().getResources().getDisplayMetrics()
        val px =
            dp * (displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
        return Math.round(px)
    }

    fun calculateDistanceBetweenDestinations(
        list: MutableList<TravellingData>,
        index: Int
    ): Double? {

        var startingPoint = list[index].geoPoints
        var endingPoint = list[index + 1].geoPoints

        var startingLocationStreet = list[index].street
        var endingLocationStreet = list[index + 1].street

        return startingPoint?.let { startPoint ->
            endingPoint?.let { endPoint ->
                Utils.calculateDistance(
                    startPoint,
                    endPoint,
                    startingLocationStreet.toString(),
                    endingLocationStreet.toString()
                )
            }
        }
    }
}