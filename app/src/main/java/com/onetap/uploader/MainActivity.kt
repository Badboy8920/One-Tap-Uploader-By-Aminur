
package com.onetap.uploader

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.*
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import java.util.Calendar

class MainActivity : Activity() {

    private var darkMode = true
    private var selectedVideo: Uri? = null
    private var scheduledDate = ""
    private var scheduledTime = ""
    private val scheduledVideos = mutableListOf<String>()

    private lateinit var root: LinearLayout
    private lateinit var videoStatus: TextView
    private lateinit var titleInput: EditText
    private lateinit var captionInput: EditText
    private lateinit var dateStatus: TextView
    private lateinit var timeStatus: TextView

    private var youtube = true
    private var instagram = false
    private var facebook = false

    private val blue = Color.rgb(65, 105, 225)

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private val bg: Int
        get() = if (darkMode) Color.rgb(14, 22, 38)
        else Color.rgb(245, 247, 252)

    private val card: Int
        get() = if (darkMode) Color.rgb(28, 40, 61)
        else Color.WHITE

    private val fg: Int
        get() = if (darkMode) Color.WHITE
        else Color.rgb(28, 38, 55)

    private val muted: Int
        get() = if (darkMode) Color.rgb(175, 190, 210)
        else Color.rgb(105, 118, 138)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showHome()
    }

    private fun rounded(color: Int, radius: Int = 18): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
        }
    }

    private fun text(
        value: String,
        size: Float,
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

    private fun addSpace(parent: LinearLayout, height: Int) {
        parent.addView(View(this), LinearLayout.LayoutParams(1, dp(height)))
    }

    private fun section(value: String) {
        root.addView(text(value, 18f, fg, true))
        addSpace(root, 10)
    }

    private fun makeCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            background = rounded(card, 20)
        }
    }

    private fun addCard(view: View) {
        val p = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        p.bottomMargin = dp(16)
        root.addView(view, p)
    }

    private fun styledButton(label: String): Button {
        return Button(this).apply {
            text = label
            textSize = 15f
            isAllCaps = false
            setTextColor(Color.WHITE)
            background = rounded(blue, 14)
        }
    }

    private fun field(hintText: String, multiline: Boolean): EditText {
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
                singleLine = true
            }
        }
    }

    private fun showHome() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(18), dp(20), dp(24))
            setBackgroundColor(bg)
        }

        val scroll = ScrollView(this)
        scroll.addView(root)

        val header = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            orientation = LinearLayout.HORIZONTAL
        }

        val brand = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        brand.addView(text("OneTap Uploader", 23f, fg, true))
        brand.addView(text("Create • Schedule • Publish", 12f, muted))

        header.addView(
            brand,
            LinearLayout.LayoutParams(0, -2, 1f)
        )

        val themeButton = styledButton(
            if (darkMode) "☀ Light" else "☾ Dark"
        )

        themeButton.setOnClickListener {
            darkMode = !darkMode
            showHome()
        }

        header.addView(themeButton)
        root.addView(header)
        addSpace(root, 22)

        val welcome = makeCard()

        welcome.addView(
            text("Ready to share your next video?", 20f, fg, true)
        )

        addSpace(welcome, 8)

        welcome.addView(
            text(
                "Select one video and prepare it for your social platforms.",
                13f,
                muted
            )
        )

        addSpace(welcome, 18)

        val selectButton = styledButton("＋ Select Your Video")

        selectButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "video/*"
                addCategory(Intent.CATEGORY_OPENABLE)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivityForResult(intent, 1001)
        }

        welcome.addView(selectButton)
        addSpace(welcome, 12)

        videoStatus = text(
            if (selectedVideo == null) "No video selected"
            else "✓ Video selected",
            13f,
            muted
        )

        welcome.addView(videoStatus)
        addCard(welcome)

        section("Choose Platforms")

        val platforms = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        fun platform(
            label: String,
            emoji: String,
            checked: Boolean,
            callback: (Boolean) -> Unit
        ): CheckBox {
            return CheckBox(this).apply {
                text = "$emoji\n$label"
                textSize = 13f
                gravity = Gravity.CENTER
                isChecked = checked
                setTextColor(fg)
                buttonTintList =
                    android.content.res.ColorStateList.valueOf(blue)

                setOnCheckedChangeListener { _, value ->
                    callback(value)
                }
            }
        }

        val yt = platform("YouTube", "▶️", youtube) {
            youtube = it
        }

        val ig = platform("Instagram", "📸", instagram) {
            instagram = it
        }

        val fb = platform("Facebook", "f", facebook) {
            facebook = it
        }

        platforms.addView(yt, LinearLayout.LayoutParams(0, dp(85), 1f))
        platforms.addView(ig, LinearLayout.LayoutParams(0, dp(85), 1f))
        platforms.addView(fb, LinearLayout.LayoutParams(0, dp(85), 1f))

        val platformCard = makeCard()
        platformCard.addView(platforms)
        addCard(platformCard)

        section("Video Details")

        val details = makeCard()

        titleInput = field("Enter video title", false)
        captionInput = field("Write caption and hashtags", true)

        details.addView(titleInput)
        addSpace(details, 12)
        details.addView(captionInput)
        addCard(details)

        section("Publishing Options")

        val options = makeCard()

        val uploadNow = styledButton("↗ Upload Now")

        uploadNow.setOnClickListener {
            showMessage("Upload feature is not connected yet.")
        }

        options.addView(uploadNow)
        addSpace(options, 12)

        dateStatus = text(
            if (scheduledDate.isEmpty()) "Select upload date"
            else "Date: $scheduledDate",
            14f,
            fg,
            true
        )

        options.addView(dateStatus)

        val dateButton = styledButton("📅 Choose Date")

        dateButton.setOnClickListener {
            chooseDate()
        }

        addSpace(options, 8)
        options.addView(dateButton)
        addSpace(options, 14)

        timeStatus = text(
            if (scheduledTime.isEmpty()) "Select upload time"
            else "Time: $scheduledTime",
            14f,
            fg,
            true
        )

        options.addView(timeStatus)

        val timeButton = styledButton("🕒 Choose Time")

        timeButton.setOnClickListener {
            chooseTime()
        }

        addSpace(options, 8)
        options.addView(timeButton)
        addSpace(options, 16)

        val scheduleButton = styledButton("＋ Schedule Upload")

        scheduleButton.setOnClickListener {
            saveSchedule()
        }

        options.addView(scheduleButton)
        addCard(options)

        section("My Scheduled Videos")

        val history = makeCard()

        if (scheduledVideos.isEmpty()) {
            history.addView(
                text("No scheduled videos yet.", 14f, muted)
            )
        } else {
            scheduledVideos.forEachIndexed { index, item ->
                history.addView(text("${index + 1}. $item", 14f, fg))
                addSpace(history, 8)
            }
        }

        addCard(history)

        root.addView(
            text(
                "Home • Scheduled • History • Settings",
                12f,
                muted
            ).apply {
                gravity = Gravity.CENTER
            }
        )

        setContentView(scroll)
    }

    private fun chooseDate() {
        val c = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, day ->
                scheduledDate = String.format(
                    "%04d-%02d-%02d",
                    year,
                    month + 1,
                    day
                )

                dateStatus.text = "Date: $scheduledDate"
            },
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun chooseTime() {
        val c = Calendar.getInstance()

        TimePickerDialog(
            this,
            { _, hour, minute ->
                scheduledTime = String.format(
                    "%02d:%02d",
                    hour,
                    minute
                )

                timeStatus.text = "Time: $scheduledTime"
            },
            c.get(Calendar.HOUR_OF_DAY),
            c.get(Calendar.MINUTE),
            false
        ).show()
    }

    private fun saveSchedule() {
        val title = titleInput.text.toString().trim()

        if (selectedVideo == null) {
            showMessage("Please select a video first.")
            return
        }

        if (title.isEmpty()) {
            showMessage("Please enter a video title.")
            return
        }

        if (scheduledDate.isEmpty() || scheduledTime.isEmpty()) {
            showMessage("Please select date and time.")
            return
        }

        if (!youtube && !instagram && !facebook) {
            showMessage("Please choose at least one platform.")
            return
        }

        val platforms = listOfNotNull(
            if (youtube) "YouTube" else null,
            if (instagram) "Instagram" else null,
            if (facebook) "Facebook" else null
        ).joinToString(", ")

        scheduledVideos.add(
            "$title • $scheduledDate $scheduledTime • $platforms"
        )

        showMessage(
            "Schedule saved in this session. Automatic publishing is not connected."
        )

        showHome()
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    @Deprecated("Deprecated in Android")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001 && resultCode == RESULT_OK) {
            selectedVideo = data?.data

            videoStatus.text =
                if (selectedVideo != null)
                    "✓ Video selected successfully"
                else
                    "No video selected"
        }
    }
}
