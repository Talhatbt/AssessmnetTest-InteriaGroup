package com.assessmenttest.utility

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.sqlite.db.SimpleSQLiteQuery
import com.assessmenttest.constants.Consts
import com.assessmenttest.database.AppDatabase
import com.assessmenttest.interfaces.StringCallBack
import com.assessmenttest.models.TravellingData
import com.assessmenttest.ui.MainApp
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.text.ParseException
import java.text.SimpleDateFormat


object Utils {

    /*
    *  This method will import the CSV into local database
    * */
    fun importCSVFromFile(){

        var file = File(
            MainApp.getContext().filesDir
                .toString() + "/" + Consts.FILE_NAME
        )

        val targetStream: InputStream = FileInputStream(file)
        var reader =
            BufferedReader(InputStreamReader(targetStream))
        val csvReader =
            CSVReader(reader)/* path of local storage (it should be your csv file locatioin)*/
        var nextLine: Array<String>? = null
        var rowCount = 0
        GlobalScope.launch(Dispatchers.IO) {
            do {
                var value = ""
                nextLine = csvReader.readNext()
                nextLine?.toMutableList()
                nextLine?.let { nextLine ->

                    if (rowCount == 0) {
                        // this part is for reading colum of each row
                        var column = nextLine?.joinToString(
                            separator = ","
                        )
                    } else {
                        // this part is for reading value of each row
                        value = nextLine?.joinToString(
                            prefix = "'",
                            separator = "','",
                            postfix = "'"
                        )
                    }

                    if (rowCount >= 1) {
                        if (!value.isNullOrEmpty()) {
                            var convertedString = nextLine?.joinToString(
                                prefix = "'",
                                separator = "','",
                                postfix = "'"
                            )
                            pushTravellingData(
                                values = convertedString
                            )
                        }
                    }
                }
                rowCount++
            } while ((nextLine) != null)
        }
    }

    private suspend fun pushTravellingData(columns: StringBuilder? = null, values: String? = null) =
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


    /*
    * This method will return the full geocoding addresss
    * */
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

    /*
    *  Calculate distance between two geopoints
    * */
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

    /*
    *  This method will copy file into app local folder. As from android -11 there is restriction
    *  to load file from the storage
    *
    * */
    fun copyFileToInternalStorage(
        uri: Uri,
        newDirName: String? = null, callback: StringCallBack
    ): String? {
        val returnCursor: Cursor = MainApp.getContext().contentResolver.query(
            uri, arrayOf(
                OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE
            ), null, null, null
        )!!


        /*
     * Get the column indexes of the data in the Cursor,
     *     * move to the first row in the Cursor, get the data,
     *     * and display it.
     * */
        val nameIndex: Int = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex: Int = returnCursor.getColumnIndex(OpenableColumns.SIZE)
        returnCursor.moveToFirst()
//        val name: String = returnCursor.getString(nameIndex)
        val name: String = Consts.FILE_NAME
        val size = returnCursor.getLong(sizeIndex).toString()
        val output: File
        output = File(MainApp.getContext().filesDir.toString() + "/" + name)

        try {
            val inputStream: InputStream? =
                MainApp.getContext().contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(output)
            var read = 0
            val bufferSize = 1024
            val buffers = ByteArray(bufferSize)
            while (inputStream?.read(buffers).also {
                    if (it != null) {
                        read = it
                    }
                } != -1) {
                outputStream.write(buffers, 0, read)
            }
            inputStream?.close()
            outputStream.close()

            callback.onCallBack("Success")

        } catch (e: Exception) {
            e.message?.let { Log.e("Exception", it) }
        }
        return output.path
    }

    fun parseDate(parseDate: String): String {

        var outDate = ""
        val inFormat = SimpleDateFormat("dd/mm/yyyy")
        val outFormat = SimpleDateFormat("yyyy/mm/dd")
        try {
            var date = inFormat.parse(parseDate)
            var outDate = outFormat.format(date);
            println("Out Date ->$outDate")
            return outDate
        } catch (e: ParseException) {
            // incase there was a date parse issue
            outDate = parseDateFallback(parseDate)
            e.printStackTrace()
        }

        return outDate
    }


    fun parseDateFallback(dateStr: String): String {
        var outDate = ""
        val inFormat = SimpleDateFormat("yyyy-mm-dd")
        val outFormat = SimpleDateFormat("yyyy/mm/dd")
        try {
            var date = inFormat.parse(dateStr)
            var outDate = outFormat.format(date);
            println("Out Date ->$outDate")
            return outDate
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return outDate
    }


    /*
    * Will get the file extension
    * */
    fun getMimeType(context: Context, uri: Uri): String? {
        val extension: String

        //Check uri format to avoid null
        extension = if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            //If scheme is a content
            val mime: MimeTypeMap = MimeTypeMap.getSingleton()
            mime.getExtensionFromMimeType(context.contentResolver.getType(uri)).toString()
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            MimeTypeMap.getFileExtensionFromUrl(
                Uri.fromFile(File(uri.path)).toString()
            )
        }
        return extension
    }
}