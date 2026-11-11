
package com.onetap.uploader

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.*
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : Activity() {

    private val prefs by lazy {
        getSharedPreferences("OneTapUploaderPrefs", MODE_PRIVATE)
    }

    private val scheduledVideos = mutableListOf<String>()

    private var darkMode = true
    private var selectedVideo: Uri? = null
    private var scheduledDate = ""
    private var scheduledTime = ""

    private lateinit var root: LinearLayout
    private lateinit var content: LinearLayout
    private lateinit var titleInput: EditText
    private lateinit var captionInput: EditText
    private lateinit var videoLabel: TextView

    private var youtubeSelected = true
    private var instagramSelected = false
    private var facebookSelected = false

    private val bg: Int
        get() = if (darkMode) Color.rgb(13, 20, 38)
        else Color.rgb(245, 247, 252)

    private val cardBg: Int
        get() = if (darkMode) Color.rgb(25, 36, 59)
        else Color.WHITE

    private val fg: Int
        get() = if (darkMode) Color.WHITE
        else Color.rgb(25, 35, 55)

    private val muted: Int
        get() = if (darkMode) Color.LTGRAY
        else Color.DKGRAY

    private val blue = Color.rgb(55, 115, 255)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        darkMode = prefs.getBoolean("dark_mode", true)
        loadSchedules()
        showHome()
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun rounded(color: Int, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
        }
    }

    private fun text(
        value: String,
        size: Float = 16f,
        color: Int = fg,
        bold: Boolean = false
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(null, Typeface.BOLD)
        }
    }

    private fun addSpace(parent: LinearLayout, height: Int = 12) {
        parent.addView(View(this), LinearLayout.LayoutParams(1, dp(height)))
    }

    private fun section(parent: LinearLayout, heading: String) {
        parent.addView(text(heading, 17f, fg, true))
        addSpace(parent, 8)
    }

    private fun makeCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            background = rounded(cardBg, 18)
        }
    }

    private fun addCard(parent: LinearLayout, card: LinearLayout) {
        parent.addView(
            card,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(14)
            }
        )
    }

    private fun styledButton(
        label: String,
        color: Int = blue,
        onClick: () -> Unit
    ): Button {
        return Button(this).apply {
            text = label
            textSize = 14f
            setTextColor(Color.WHITE)
            background = rounded(color, 14)
            setOnClickListener { onClick() }
        }
    }

    private fun field(
        hintText: String,
        multiline: Boolean
    ): EditText {
        return EditText(this).apply {
            hint = hintText
            textSize = 15f
            setTextColor(fg)
            setHintTextColor(muted)
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = rounded(bg, 12)

            if (multiline) {
                minLines = 3
                gravity = Gravity.TOP
            } else {
                setSingleLine(true)
            }
        }
    }

    private fun showHome() {

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
            setPadding(dp(16), dp(12), dp(16), dp(12))
        }

        val header = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            orientation = LinearLayout.HORIZONTAL
        }

        val heading = text("OneTap Uploader", 23f, fg, true)

        header.addView(
            heading,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        val themeButton = styledButton(
            if (darkMode) "☀ Light" else "☾ Dark",
            Color.rgb(90, 100, 125)
        ) {
            darkMode = !darkMode
            prefs.edit().putBoolean("dark_mode", darkMode).apply()
            showHome()
        }

        header.addView(themeButton)
        root.addView(header)

        addSpace(root, 8)
        root.addView(text("Upload your videos in one place", 13f, muted))
        addSpace(root, 18)

        val scroll = ScrollView(this)

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        scroll.addView(content)

        // FIXED HOME SCROLLVIEW
        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        val videoCard = makeCard()
        section(videoCard, "1. Select Video")

        videoLabel = text(
            selectedVideo?.lastPathSegment ?: "No video selected",
            14f,
            muted
        )

        videoCard.addView(videoLabel)
        addSpace(videoCard)

        videoCard.addView(
            styledButton("Choose Video") {
                chooseVideo()
            }
        )

        addCard(content, videoCard)

        val detailsCard = makeCard()
        section(detailsCard, "2. Video Details")

        titleInput = field("Enter video title", false)
        detailsCard.addView(titleInput)
        addSpace(detailsCard)

        captionInput = field("Enter caption and hashtags", true)
        detailsCard.addView(captionInput)

        addCard(content, detailsCard)

        val platformCard = makeCard()
        section(platformCard, "3. Select Platforms")

        val youtube = CheckBox(this).apply {
            text = "▶ YouTube"
            setTextColor(fg)
            isChecked = youtubeSelected
            buttonTintList =
                android.content.res.ColorStateList.valueOf(blue)

            setOnCheckedChangeListener { _, checked ->
                youtubeSelected = checked
            }
        }

        val instagram = CheckBox(this).apply {
            text = "◎ Instagram"
            setTextColor(fg)
            isChecked = instagramSelected
            buttonTintList =
                android.content.res.ColorStateList.valueOf(blue)

            setOnCheckedChangeListener { _, checked ->
                instagramSelected = checked
            }
        }

        val facebook = CheckBox(this).apply {
            text = "f Facebook"
            setTextColor(fg)
            isChecked = facebookSelected
            buttonTintList =
                android.content.res.ColorStateList.valueOf(blue)

            setOnCheckedChangeListener { _, checked ->
                facebookSelected = checked
            }
        }

        platformCard.addView(youtube)
        platformCard.addView(instagram)
        platformCard.addView(facebook)

        addCard(content, platformCard)

        val actionCard = makeCard()
        section(actionCard, "4. Upload Options")

        actionCard.addView(
            styledButton("Upload Now") {
                if (selectedVideo == null) {
                    showMessage("Please select a video first")
                } else {
                    showMessage(
                        "Publishing is not connected yet. Platform API setup is required."
                    )
                }
            }
        )

        addSpace(actionCard)

        actionCard.addView(
            styledButton("Choose Schedule Date") {
                chooseDate()
            }
        )

        addSpace(actionCard)

        actionCard.addView(
            styledButton("Choose Schedule Time") {
                chooseTime()
            }
        )

        addSpace(actionCard)

        val scheduleButton = styledButton(
            "Save Scheduled Video",
            Color.rgb(25, 160, 110)
        ) {
            saveSchedule()
        }

        actionCard.addView(scheduleButton)
        addCard(content, actionCard)

        val listCard = makeCard()
        section(listCard, "5. My Scheduled Videos")

        listCard.addView(
            styledButton("View Scheduled Videos") {
                showScheduled()
            }
        )

        addCard(content, listCard)

        val footer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        footer.addView(
            styledButton("Home") { showHome() },
            LinearLayout.LayoutParams(0, dp(48), 1f)
        )

        footer.addView(
            styledButton("Scheduled") { showScheduled() },
            LinearLayout.LayoutParams(0, dp(48), 1f)
        )

        root.addView(footer)
        setContentView(root)
    }

    private fun chooseVideo() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "video/*"
        }

        startActivityForResult(intent, 1001)
    }

    @Deprecated("Deprecated in Android API")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {

            data?.data?.let { uri ->
                selectedVideo = uri

                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {
                }

                if (::videoLabel.isInitialized) {
                    videoLabel.text =
                        uri.lastPathSegment ?: "Video selected"
                }
            }
        }
    }

    private fun chooseDate() {

        val calendar = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, day ->

                scheduledDate = String.format(
                    Locale.getDefault(),
                    "%04d-%02d-%02d",
                    year,
                    month + 1,
                    day
                )

                showMessage("Date selected: $scheduledDate")
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun chooseTime() {

        val calendar = Calendar.getInstance()

        TimePickerDialog(
            this,
            { _, hour, minute ->

                scheduledTime = String.format(
                    Locale.getDefault(),
                    "%02d:%02d",
                    hour,
                    minute
                )

                showMessage("Time selected: $scheduledTime")
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun saveSchedule() {

        if (selectedVideo == null) {
            showMessage("Please select a video first")
            return
        }

        val title = titleInput.text.toString().trim()

        if (title.isEmpty()) {
            showMessage("Please enter video title")
            return
        }

        if (scheduledDate.isEmpty() || scheduledTime.isEmpty()) {
            showMessage("Please select date and time")
            return
        }

        val platforms = mutableListOf<String>()

        if (youtubeSelected) platforms.add("YouTube")
        if (instagramSelected) platforms.add("Instagram")
        if (facebookSelected) platforms.add("Facebook")

        if (platforms.isEmpty()) {
            showMessage("Please select at least one platform")
            return
        }

        val caption = captionInput.text.toString().trim()

        val item = JSONArray().apply {
            put(title)
            put(scheduledDate)
            put(scheduledTime)
            put(platforms.joinToString(", "))
            put(selectedVideo.toString())
            put(caption)
        }

        scheduledVideos.add(item.toString())
        persistSchedules()

        showMessage("Scheduled video saved successfully!")
        showScheduled()
    }

    private fun persistSchedules() {

        val array = JSONArray()

        scheduledVideos.forEach {
            array.put(it)
        }

        prefs.edit()
            .putString("scheduled_videos", array.toString())
            .apply()
    }

    private fun loadSchedules() {

        scheduledVideos.clear()

        val saved = prefs.getString("scheduled_videos", null) ?: return

        try {
            val array = JSONArray(saved)

            for (i in 0 until array.length()) {
                scheduledVideos.add(array.getString(i))
            }

        } catch (_: Exception) {
            scheduledVideos.clear()
        }
    }

    private fun showScheduled() {

        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            setBackgroundColor(bg)
        }

        page.addView(text("My Scheduled Videos", 23f, fg, true))
        addSpace(page, 16)

        val scroll = ScrollView(this)

        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        if (scheduledVideos.isEmpty()) {

            list.addView(text("No scheduled videos yet.", 15f, muted))

        } else {

            scheduledVideos.forEachIndexed { index, savedItem ->

                try {
                    val data = JSONArray(savedItem)

                    val title = data.optString(0)
                    val date = data.optString(1)
                    val time = data.optString(2)
                    val platforms = data.optString(3)

                    val card = makeCard()

                    card.addView(text(title, 17f, fg, true))
                    addSpace(card, 6)

                    card.addView(text("Date: $date", 14f, muted))
                    card.addView(text("Time: $time", 14f, muted))
                    card.addView(text("Platforms: $platforms", 14f, muted))

                    addSpace(card)

                    card.addView(
                        styledButton(
                            "Delete Schedule",
                            Color.rgb(190, 55, 65)
                        ) {
                            scheduledVideos.removeAt(index)
                            persistSchedules()
                            showScheduled()
                        }
                    )

                    addCard(list, card)

                } catch (_: Exception) {
                }
            }
        }

        scroll.addView(list)

        // FIXED SCHEDULE SCROLLVIEW
        page.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        page.addView(
            styledButton("Back to Home") {
                showHome()
            }
        )

        setContentView(page)
    }

    private fun showMessage(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }
}
