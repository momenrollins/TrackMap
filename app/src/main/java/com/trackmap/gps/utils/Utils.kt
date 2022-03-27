package com.trackmap.gps.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.text.Html
import android.text.InputFilter
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.trackmap.gps.R
import com.trackmap.gps.homemap.model.MapListDataModel
import com.trackmap.gps.homemap.model.MapListDataModelGps3
import com.trackmap.gps.preference.MyPreference
import com.trackmap.gps.preference.PrefKey
import java.io.BufferedReader
import java.io.File
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@SuppressLint("NewApi")
object Utils {
    private var dialog: Dialog? = null

    /**
     * get Current Date in Date object
     *
     * @return Date object
     */
    val todayDate: Date
        get() {
            val calToday = Calendar.getInstance()
            calToday.set(Calendar.HOUR_OF_DAY, 0)
            calToday.set(Calendar.MINUTE, 0)
            calToday.set(Calendar.SECOND, 0)
            calToday.set(Calendar.MILLISECOND, 0)
            return calToday.time

        }

    fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable!!.setBounds(
            0,
            0,
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    /**
     * get file dirctory name for video
     */
    fun getCarListingFile(mContext: Context): File {
        val file = File(mContext.cacheDir.path + "/" + Constants.fileName)

        if (file.exists()) {
            file.delete()

        }
        file.createNewFile()

        return file
    }

    fun deleteFile(context: Context) {
        val file = File(context.cacheDir.path + "/" + Constants.fileName)

        if (file.exists()) {
            file.delete()
        }
    }

    fun clearMyFiles(context: Context) {
        val files: Array<File> = context.getFilesDir().listFiles()
        if (files != null) for (file in files) {
            file.delete()
            Log.d("TAG", "clearMyFiles:  ${file}")
        } else Log.d("TAG", "clearMyFiles: notfound $files")
    }

    fun writeCarListingDataOnFile(mContext: Context, mapData: MapListDataModel) {
        val gson = Gson()
        val jsonText = gson.toJson(mapData)
        val file = getCarListingFile(mContext)
        file.writeText(jsonText)
    }

    fun writeCarListingDataOnFileGps3(mContext: Context, mapData: MapListDataModelGps3) {
        val gson = Gson()
        val jsonText = gson.toJson(mapData)
        val file = getCarListingFileGps3(mContext)
        file.writeText(jsonText)
    }

    private fun getCarListingFileGps3(mContext: Context): File {
        val file = File(mContext.cacheDir.path + "/" + Constants.fileNameGps3)

        if (file.exists()) {
            file.delete()

        }
        file.createNewFile()

        return file
    }

    fun getCarListingData(mContext: Context): MapListDataModel {
        return if (File(mContext.cacheDir.path + "/" + Constants.fileName).exists()) {
            val bufferedReader: BufferedReader =
                File(mContext.cacheDir.path + "/" + Constants.fileName).bufferedReader()
            val inputString = bufferedReader.use { it.readText() }
            Gson().fromJson(inputString, MapListDataModel::class.java)
        } else MapListDataModel()
    }

    fun getCarListingDataGps3(mContext: Context): MapListDataModelGps3 {
        return if (File(mContext.cacheDir.path + "/" + Constants.fileNameGps3).exists()) {

            val bufferedReader: BufferedReader =
                File(mContext.cacheDir.path + "/" + Constants.fileNameGps3).bufferedReader()
            val inputString = bufferedReader.use { it.readText() }
            Gson().fromJson(inputString, MapListDataModelGps3::class.java)
        } else MapListDataModelGps3()
    }


    /**
     * Get current time zone from device
     *
     * @return device's current time zone
     */
    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into
     * pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    fun convertDpToPixel(dp: Float, context: Context): Float {
        return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px      A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    fun convertPixelsToDp(px: Float, context: Context): Float {
        return px / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }


    @SuppressLint("MissingPermission")
            /* Check Internet Connectivity */ fun isOnline(context: Context): Boolean {

        return try {
            val cm = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            cm != null && cm.activeNetworkInfo?.isConnectedOrConnecting!!
        } catch (e: Exception) {
            false
        }

    }

    /**
     * Show Snack bar
     * @param message  message as String
     * @param view  instance of View
     */
    fun showSnackbar(message: String, view: View) {
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        val snackbarView = snackbar.view
        val textView = snackbarView.findViewById<TextView>(R.id.snackbar_text)
        textView.maxLines = 3  // show multiple line
        snackbar.show()
    }


    /**
     * Show Toast
     * @param mContext  Context
     * @param message Message as String
     * @param icon  drawable if want to show
     * @param duration duration either Toast.LENGTH_SHORT or Toast.LENGTH_LONG
     */
    fun showToast(mContext: Context, message: String, icon: Int, duration: Int) {
        try {
            val toastBuilder = StyleableToast.Builder(mContext)
            toastBuilder.text(message)
            toastBuilder.textColor(Color.WHITE)
            toastBuilder.backgroundColor(mContext.resources.getColor(R.color.colorAccent))
            toastBuilder.length(duration)
            if (icon != 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                toastBuilder.iconStart(icon)
            }
            toastBuilder.show()
        } catch (e: Exception) {
            DebugLog.print(e)
        }

    }

    /**
     * Show Toast
     * @param mContext  Context
     * @param message Message as String
     * @param duration duration either Toast.LENGTH_SHORT or Toast.LENGTH_LONG
     */
    fun showToast(mContext: Context, message: String, duration: Int) {
        Toast.makeText(mContext, message, duration).show()
    }

    @SuppressLint("NewApi")
    fun getDeviceWidth(mContext: Context): Int {
        val display = (mContext
            .getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val size = Point()
        display.getSize(size)
        return size.x
    }

    fun getDeviceHeight(mContext: Context): Int {
        val display = (mContext
            .getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val size = Point()
        display.getSize(size)
        return size.y
    }

    /**
     * Convert inputDate in String format to Date Object
     *
     * @param strDate     input date format as String
     * @param parseFormat Desired Date format
     * @return converted Date
     */
    fun convertStringToDate(strDate: String, parseFormat: String): Date? {
        return try {
            SimpleDateFormat(parseFormat, Locale.US).parse(strDate)
        } catch (e: Exception) {
            DebugLog.print(e)
            null
        }

    }

    /**
     * Hide Keyboard
     *
     * @param activity object of activity
     */
    fun hideSoftKeyboard(activity: Activity) {
        try {
            val inputMethodManager = activity
                .getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(
                activity.currentFocus!!
                    .windowToken, 0
            )
        } catch (e: Exception) {
            DebugLog.print(e)
            activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        }

    }

    /**
     * Convert Date formate
     */
    @JvmStatic
    fun mGetFormattedDate(date: String, inputPattern: String, outputPattern: String): String {
        try {
            val inputFormat = SimpleDateFormat(inputPattern, Locale.getDefault())
            val inputDate = inputFormat.parse(date)
            val outputFormat = SimpleDateFormat(outputPattern, Locale.getDefault())
            //outputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            var newStr = outputFormat.format(inputDate!!)
            newStr = newStr.replace("AM", "am").replace("PM", "pm")
            return newStr
        } catch (ignore: Exception) {
            ignore.printStackTrace()
        }

        return "--"
    }

    /**
     * Enlarge Hit Area of TextView
     *
     * @param mTextView object of TextView
     */
    fun increaseTextViewHitArea(mTextView: TextView) {
        try {
            val parent = mTextView.parent as View  // button: the view you want to enlarge hit area
            parent.post {
                val rect = Rect()
                mTextView.getHitRect(rect)
                rect.top -= 50    // increase top hit area
                rect.left -= 150   // increase left hit area
                rect.bottom += 100 // increase bottom hit area
                rect.right += 150  // increase right hit area
                parent.touchDelegate = TouchDelegate(rect, mTextView)
            }
        } catch (e: Exception) {
            DebugLog.print(e)
        }

    }

    /**
     * Enlarge Hit Area of ImageView
     *
     * @param mImageView object of Image
     */
    fun increaseImageHitArea(mImageView: ImageView) {
        try {
            val parent = mImageView.parent as View  // button: the view you want to enlarge hit area
            parent.post {
                val rect = Rect()
                mImageView.getHitRect(rect)
                rect.top -= 100    // increase top hit area
                rect.left -= 100   // increase left hit area
                rect.bottom += 100 // increase bottom hit area
                rect.right += 100  // increase right hit area
                parent.touchDelegate = TouchDelegate(rect, mImageView)
            }
        } catch (e: Exception) {
            DebugLog.print(e)
        }

    }

    /**
     * Convert date into desired format
     *
     * @param dateString       input date format as String
     * @param inputDateString  Input Date format
     * @param outputDateString Desired Date format
     * @return converted Date as String
     */

    fun convertDateFormate(
        dateString: String,
        inputDateString: String,
        outputDateString: String
    ): String {
        val date: Date?
        val dateFormatLocal = SimpleDateFormat(inputDateString, Locale.US)
        return try {
            date = dateFormatLocal.parse(dateString)
            SimpleDateFormat(outputDateString, Locale.US).format(date!!)
        } catch (e: ParseException) {
            DebugLog.print(e)
            ""
        } catch (e: Exception) {
            DebugLog.print(e)
            ""
        }

    }

    /**
     * Hide keyboard if user touch outside editText..
     *
     * @param view:     parent view
     * @param mContext: Context
     */
    @SuppressLint("ClickableViewAccessibility")
    fun setupOutSideTouchHideKeyboard(view: View, mContext: Context) {

        //Set up touch listener for non-text box views to hide keyboard.
        if (view !is EditText) {

            view.setOnTouchListener { v, event ->
                val mgr =
                    mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                mgr.hideSoftInputFromWindow(v.windowToken, 0)
                false
            }
        }

        //If a layout container, iterate over children and seed recursion.
        if (view is ViewGroup) {

            for (i in 0 until view.childCount) {

                val innerView = view.getChildAt(i)

                setupOutSideTouchHideKeyboard(innerView, mContext)
            }
        }
    }

    /**
     * get Current Date
     *
     * @param format desired format for current date
     * @return current date as String
     */
    fun getCurrentDate(format: String): String {
        val c = Calendar.getInstance().time
        DebugLog.print("Current time => $c")

        val df = SimpleDateFormat(format, Locale.US)
        return df.format(c)

    }

    /**
     * Show ProgressBar
     *
     * @param mContext Context
     */
    fun showProgressBar(mContext: Context) {

        dialog = Dialog(mContext/*, android.R.style.Theme_Translucent*/)
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog!!.setCancelable(false)
        val inflater = mContext
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val viewChild: View = inflater.inflate(R.layout.layout_progress_dialog, null)
        val imgLoader = viewChild.findViewById<ImageView>(R.id.loader)
        Glide.with(mContext).load(R.drawable.ic_drawer_image).into(imgLoader)
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog!!.setContentView(viewChild)


        try {
            dialog!!.show()
        } catch (e: Exception) {
            DebugLog.print(e)
        }

    }

    /**
     * Hide Progress bar
     */
    fun hideProgressBar() {
        if (dialog != null && dialog!!.isShowing) {
            dialog!!.dismiss()
        }
    }

    /**
     * Set AdjustPanKeyboard
     *
     * @param activity instance od Activity
     */
    fun setAdjustPanKeyBoard(activity: FragmentActivity) {
        activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    /**
     * Set AdjustResizeKeyboard
     *
     * @param activity instance od Activity
     */
    fun setAdjustResizeKeyBoard(activity: FragmentActivity) {
        activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    /**
     * Compare two dates
     *
     * @param date1 First Date
     * @param date2 Second Date
     * @return // 0- today, // negative- past (if date1  is lower than date2) // positive-future (if
     * date1  is greater than date2)
     */
    fun compareTwoDates(date1: Date, date2: Date): Int {
        return Integer.compare(date1.compareTo(date2), 0)
        //values:
        // 0- today,
        // negative- past (if date1  is lower than date2)
        // positive-future (if date1  is greater than date2)
    }

    /**
     * Load image using Glide as Circle image
     *
     * @param mContext    Context
     * @param url         image url
     * @param imageView   Object of ImageView
     * @param placeHolder placeholder image
     */
    fun loadRoundedImageUsingGlide(
        mContext: Context,
        url: String,
        placeHolder: Int,
        imageView: ImageView
    ) {
        if (Validation.isNotNull(url)) {
            Glide.with(mContext).load(url).apply(RequestOptions.circleCropTransform())
                .apply(RequestOptions().placeholder(placeHolder).error(placeHolder)).into(imageView)
        } else {
            Glide.with(mContext).load(placeHolder).apply(RequestOptions.circleCropTransform())
                .into(imageView)
        }
    }

    /**
     * Load image using Glide as Circle image
     *
     * @param mContext    Context
     * @param url         image url
     * @param imageView   Object of ImageView
     * @param placeHolder placeholder image
     */
    fun loadImageUsingGlide(
        mContext: Context,
        url: String,
        placeHolder: Int,
        imageView: ImageView
    ) {
        if (Validation.isNotNull(url)) {
            Glide.with(mContext)
                .asBitmap()
                .load(url)
                .apply(
                    RequestOptions().placeholder(placeHolder).error(placeHolder)
                        .override(1000, 800).centerCrop()
                )
                .into(imageView)
        } else {
            Glide.with(mContext).load(placeHolder).into(imageView)
        }
    }

    /**
     * Load image using Glide as Circle image
     *
     * @param mContext    Context
     * @param url         image url
     * @param placeHolder placeholder image
     * @param imageView   Object of ImageView
     */
    fun loadRoundedCornerImageUsingGlide(
        mContext: Context,
        url: String,
        placeHolder: Int,
        imageView: ImageView
    ) {
        if (Validation.isNotNull(url)) {
            Glide.with(mContext)
                .load(url)
                .apply(RequestOptions.bitmapTransform(RoundedCorners(20)))
                .apply(
                    RequestOptions().placeholder(placeHolder)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                )
                .into(imageView)
        } else {
            imageView.setImageResource(placeHolder)
        }
    }

    /**
     * set statusbar color to Activity
     *
     * @param appCompatActivity instead of AppCompactActivity
     * @param color             color code as integer
     */
    fun setStatusBarColor(appCompatActivity: AppCompatActivity, color: Int) {
        appCompatActivity.window.statusBarColor = appCompatActivity.resources.getColor(color)
    }

    /**
     * For Marshmallow & Above set Light statusbar color to Activity
     *
     * @param appCompatActivity instead of AppCompactActivity
     * @param view              parent View of Activity
     */
    fun setLightStatusBar(appCompatActivity: AppCompatActivity, view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            var flags = view.systemUiVisibility
            flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            view.systemUiVisibility = flags
            appCompatActivity.window.statusBarColor = Color.WHITE
        }
    }


    /**
     * Remove [] from Error Objects when there are multiple errors
     *
     * @param message as String
     * @return replacedString
     */
    fun removeArrayBrace(message: String): String {
        val replaceString: String = message.replace("[\"", "").replace("\"]", "").replace(".", "")
        return replaceString
    }

    /**
     * Convert special characters' unicode to String
     *
     * @param text html string with unicode
     * @return converted string
     */
    fun convertStringWithHtmlParser(text: String): String {
        return if (Validation.isNotNull(text)) {
            Html.fromHtml(text).toString()
        } else {
            ""
        }
    }

    /**
     * Clear data and perform logout
     */

    /* fun clearDataForLogout(mContext: Context, intent: Intent) {
         try {
             MyPreference.clearAllData()
         } catch (e: Exception) {
             DebugLog.print(e)
         } finally {
             // intent should contains below flags
             //                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
             //                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
             //                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
             goToPreviousActivity(mContext as AppCompatActivity, intent)
         }
     }*/

    /**
     * Asyntc task to check whether app is in foreground or not
     */
    class ForegroundCheckTask : AsyncTask<Context, Void, Boolean>() {

        override fun doInBackground(vararg params: Context): Boolean {
            val context = params[0].applicationContext
            return isAppOnForeground(context)
        }

        private fun isAppOnForeground(context: Context): Boolean {
            val activityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val appProcesses = activityManager.runningAppProcesses ?: return false
            val packageName = context.packageName
            for (appProcess in appProcesses) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName == packageName) {
                    return true
                }
            }
            return false
        }
    }

    /**
     * Get Application Name
     * @param context context
     * @return  application name
     */
    fun getApplicationName(context: Context): String {
        val applicationInfo = context.applicationInfo
        val stringId = applicationInfo.labelRes
        return if (stringId == 0) applicationInfo.nonLocalizedLabel.toString() else context.getString(
            stringId
        )
    }


    fun getOS(): String {
        var osName = "Not_Found"
        try {
            val fields = Build.VERSION_CODES::class.java.fields
            osName = fields[Build.VERSION.SDK_INT].name
            DebugLog.d("OsNAme $osName")
        } catch (e: java.lang.Exception) {
            DebugLog.e(e.message.toString())
        }
        return osName
    }

    fun getToken(): String {
        val tokenType = MyPreference.getValueString(PrefKey.TOKEN_TYPE, "")
        val token = MyPreference.getValueString(PrefKey.ACCESS_TOKEN, "")
        return "$tokenType $token"
    }

    fun getInputFilterOnlyCharAndSpace(): InputFilter {
        return InputFilter { source, start, end, _, _, _ ->
            var newString = ""
            for (i in start until end) {
                if (!Character.isLetter(source[i]) && !Character.isWhitespace(source[i])) {
                    return@InputFilter newString
                } else {
                    newString += source[i]
                }
            }
            null
        }
    }

    fun getInputFilterOnlyChar(): InputFilter {

        return InputFilter { source, start, end, _, _, _ ->
            var newString = ""
            for (i in start until end) {
                if (!Character.isLetter(source[i])) {
                    return@InputFilter newString
                } else {
                    newString += source[i]
                }
            }
            null
        }
    }


    fun getInputFilterUpperCaseAndDigit(): InputFilter {
        return InputFilter { source, start, end, _, _, _ ->
            var newString = ""
            for (i in start until end) {
                if (!Character.isLetter(source[i]) && !Character.isDigit(source[i])) {
                    return@InputFilter newString
                } else {
                    newString += source[i]
                }
            }
            null
        }
    }

    fun getInputFilterNumbers(): InputFilter {
        return InputFilter { source, start, end, _, _, _ ->
            var newString = ""
            for (i in start until end) {

                if (!Character.isDigit(source[i]) && !source.contains(".")) {
                    return@InputFilter newString
                } else {
                    newString += source[i]
                }

                if (newString.isNullOrEmpty() && newString[0].equals(".")) {
                    newString = ""
                }

            }
            null
        }
    }

    fun lunchMap(
        context: Context,
        destinationLatitude: Double,
        destinationLongitude: Double,
        placeName: String
    ) {
        val uri = String.format(
            Locale.ENGLISH,
            "http://maps.google.com/maps?daddr=%f,%f (%s)",
            destinationLatitude,
            destinationLongitude,
            placeName
        )
        launchDirection(context, uri)
    }

    fun lunchMap(
        context: Context,
        sourceLatitude: Double,
        sourceLongitude: Double,
        sourceName: String,
        destinationLatitude: Double,
        destinationLongitude: Double,
        placeName: String
    ) {
        val uri = String.format(
            Locale.ENGLISH,
            "http://maps.google.com/maps?saddr=%f,%f(%s)&daddr=%f,%f (%s)",
            sourceLatitude,
            sourceLongitude,
            sourceName,
            destinationLatitude,
            destinationLongitude,
            placeName
        )
        launchDirection(context, uri)
    }

    fun launchDirection(context: Context, uri: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        intent.setPackage("com.google.android.apps.maps")
        try {
            context.startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            try {
                val unrestrictedIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                context.startActivity(unrestrictedIntent)
            } catch (innerEx: ActivityNotFoundException) {
                showToast(context, context.getString(R.string.error_map_app), Toast.LENGTH_LONG)
            }
        }
    }

    fun commonHeaders(): HashMap<String, String> {
        var headers = HashMap<String, String>()
        headers["Content-Length"] = "1000"
        return headers
    }


    fun getLanguage(): String {
        val language = MyPreference.getValueString(PrefKey.LANGUAGE, "")
        return "$language"
    }

    fun getUniqueList(carId: ArrayList<String>): ArrayList<String> {
        val hashSet = HashSet<String>()
        hashSet.addAll(carId)
        carId.clear()
        carId.addAll(hashSet)

        return carId
    }

    fun convertSecondsToHMmSs(seconds: Long): String {
        var result = ""
        val s = seconds % 60
        val m = seconds / 60 % 60
        val h = seconds / (60 * 60) % 24

        result = String.format("%d:%02d:%02d", h, m, s)
        return result
    }

    fun convertToHoursMin(minutes: Int): String {
        var result = ""
        val hours: Int = minutes / 60
        val minutes: Int = minutes % 60

//        val minutes = (minutes / 60).toInt() % 60
//        val hours = (minutes / (60 * 60)).toInt() % 24
//        var result = String.format("%d h %02d min ", hours, minutes)

        result = String.format("%d h %02d min ", hours, minutes)
        return result
    }

    // Get list of dates
    fun getDates(
        dateString1: String,
        dateString2: Date
    ): ArrayList<Date> {

        /* var date = Date()
 //        val formatter = SimpleDateFormat("MMM dd yyyy HH:mm a")
         val formatter = SimpleDateFormat("dd MMM")
         val answer = formatter.format(date)
         Log.d("answer",answer)*/

        val dates = ArrayList<Date>()
        val df1: DateFormat = SimpleDateFormat("yyyy-MM-dd")

        val str = df1.format(dateString2)

        var ff: Date? = null
        var tt: Date? = null
        try {
            ff = df1.parse(dateString1)
            tt = df1.parse(str)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        val cal1 = Calendar.getInstance()
        cal1.time = ff
        val cal2 = Calendar.getInstance()
        cal2.time = tt
        while (!cal1.after(cal2)) {
            dates.add(cal1.time)
            cal1.add(Calendar.DATE, 1)
        }
        return dates
    }

    fun formatDuration(totalSecs: Long): String {
        val days = totalSecs / (3600 * 24)
        val hours = (totalSecs % (3600 * 24)) / 3600
        val minutes = (totalSecs % 3600) / 60
        val seconds = totalSecs % 60
        when {
            days > 0 -> {
                return "$days d "
            }
            hours > 0 -> {
                return "$hours h "
            }
            minutes > 0 -> {
                return "$minutes min "
            }
            seconds > 0 -> {
                return "$seconds sec "
            }
        }
        return ""
    }

    fun formatDurationFullValues(totalSecs: Long): String {
        val days = totalSecs / (3600 * 24)
        val hours = (totalSecs % (3600 * 24)) / 3600
        var minutes = (totalSecs % 3600) / 60
        val seconds = totalSecs % 60
        /* if (seconds > 10)
             minutes++*/
        val b = StringBuilder()
        b.append(if (days == 0L) "" else "$days d ")
        b.append(if (hours == 0L) "" else "$hours h ")
        b.append(if (minutes == 0L) "" else "$minutes min ")
        if (totalSecs < 60)
            b.append(if (seconds == 0L) "" else "$seconds sec ")
        return b.toString()
    }

    /**
     * Return date in specified format.
     * @param milliSeconds Date in milliseconds
     * @param dateFormat Date format
     * @return String representing date in specified format
     */
    @SuppressLint("SimpleDateFormat")
    fun getDateFromMillis(milliSeconds: Long, dateFormat: String): String? {
        // Create a DateFormatter object for displaying date in specified format.
        val formatter = SimpleDateFormat(dateFormat, Locale.US)

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds
        return formatter.format(calendar.time)
    }

    /**
     * Return date in specified format.
     * @param date Date
     * @param dateFormat Date format
     * @return String representing date in specified format
     */
    @SuppressLint("SimpleDateFormat")
    fun getMillisFromDate(date: String?, dateFormat: String): Long {
        if (date == null || date.isEmpty()) return 0
        // Create a DateFormatter object for displaying date in specified format.
        val formatter = SimpleDateFormat(dateFormat, Locale.US)

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar = Calendar.getInstance()
        calendar.time = formatter.parse(date)
        return calendar.timeInMillis / 1000
    }


    fun getDateToMilliSeconds(dateValue: String, timeValue: String): Long {
        val result: Long
        val sdf = SimpleDateFormat("dd-MM-yyyy hh:mm:ss")
        val comString = "$dateValue $timeValue"
        val d1: Date? = sdf.parse(comString)

        var milli = d1!!.time

        /* val formatter: DateTimeFormatter =
             DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)

         val l1 =  LocalDateTime.MIN
         val l2 =  LocalDateTime.MAX
         val localDate1: LocalDateTime = LocalDateTime.parse(d1.toString(), formatter)
         val timeInMilliseconds1: Long =
             localDate1.atOffset(ZoneOffset.UTC).toInstant().toEpochMilli()*/

        result = TimeUnit.MILLISECONDS.toSeconds(milli)
        return result
    }

    /**
     * Convert date into desired format
     *
     * @param dateString input date format as String
     * @param inputDateString Input Date format
     * @param outputDateString Desired Date format
     * @return converted Date as String
     */

    fun convertDateFormat(
        dateString: String,
        inputDateString: String,
        outputDateString: String
    ): String {
        when {
            dateString != null && dateString.trim().isNotEmpty() -> {
                val date: Date
                val dateFormatLocal = SimpleDateFormat(inputDateString, Locale.US)
                return try {
                    date = dateFormatLocal.parse(dateString)
                    SimpleDateFormat(outputDateString, Locale.US).format(date)
                } catch (e: ParseException) {
                    DebugLog.print(e)
                    ""
                } catch (e: Exception) {
                    DebugLog.print(e)
                    ""
                }
            }
            else -> {
                return ""
            }
        }

    }

    /**
     *  Create folder in Internal Storage for load data
     */
    fun createInternalStorageFolder(context: Context) {
        try {
            var storagePath: String =
                Environment.getExternalStorageDirectory().toString() + "/AAData"
            var folder = File(storagePath)
            var isExists = false
            if (!folder.exists()) {
                isExists = folder.mkdir()
            }

            if (isExists) {
                Toast.makeText(context, "Folder created", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Folder already created", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     *  Create file in internal storage
     */
    private fun createFileInternalStorage(context: Context) {
        try {
            var filePath = context.getExternalFilesDir(null)!!.absolutePath
            var newFile = File(filePath)

            if (!newFile.exists()) {
                if (!newFile.mkdir()) {
                    // Log.d("Error", "occurred while creating file")
                }
            }

//            var logFile = File(newFile)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
