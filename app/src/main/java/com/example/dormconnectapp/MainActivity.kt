package com.example.dormconnectapp

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // טיפול ב-insets (לא קשור לטולבר)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // --- Toolbar ---
        val toolbar: Toolbar = findViewById(R.id.main_toolbar)
        toolbar.setBackgroundColor(Color.parseColor("#b1e5d7"))
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)   // ללא כותרת אוטומטית

        // מכולה אופקית ללוגו + טקסט
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        // לוגו (גדול • 80 dp)
        val logoSizePx = (80 * resources.displayMetrics.density).toInt()
        val logo = ImageView(this).apply {
            setImageResource(R.drawable.dormconnect_icon_clean)   // השם החדש
            layoutParams = LinearLayout.LayoutParams(logoSizePx, logoSizePx)
        }
        container.addView(logo)

        // טקסט “DormConnect”
        val title = TextView(this).apply {
            text = "DormConnect"
            textSize = 24f                      // 24 sp – בולט
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.BLACK)
            setPadding((8 * resources.displayMetrics.density).toInt(), 0, 0, 0) // רווח מהלוגו
        }
        container.addView(title)

        // מוסיפים את המכולה לטולבר (קודם מנקים כל View קיים)
        toolbar.removeAllViews()
        toolbar.addView(container)
    }
}

