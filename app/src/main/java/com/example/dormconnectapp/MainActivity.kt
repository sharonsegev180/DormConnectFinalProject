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
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.view.View
import com.google.firebase.FirebaseApp




class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(this)


        // טיפול ב-insets לשמירה על שוליים במסכים שונים
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // --- Toolbar ---
        val toolbar: Toolbar = findViewById(R.id.main_toolbar)
        toolbar.setBackgroundColor(Color.parseColor("#b1e5d7"))
        setSupportActionBar(toolbar)

        // ⬇ שימוש נכון ב-Navigation עם FragmentContainerView
        // Menu
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
            if (destination.id == R.id.logInFragment) {
                bottomNav.visibility = View.GONE
            } else {
                bottomNav.visibility = View.VISIBLE
            }
        }
        NavigationUI.setupWithNavController(bottomNav, navController)

        setupActionBarWithNavController(navController)

        // הוספת לוגו וטקסט מותאם אישית לטולבר
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val logoSizePx = (80 * resources.displayMetrics.density).toInt()
        val logo = ImageView(this).apply {
            setImageResource(R.drawable.dormconnect_icon_clean)
            layoutParams = LinearLayout.LayoutParams(logoSizePx, logoSizePx)
        }
        container.addView(logo)

        val title = TextView(this).apply {
            text = "DormConnect"
            textSize = 24f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.BLACK)
            setPadding((8 * resources.displayMetrics.density).toInt(), 0, 0, 0)
        }
        container.addView(title)

        toolbar.removeAllViews()
        toolbar.addView(container)
    }

    // תומך בחץ Back אוטומטי בעת ניווט אחורה בין פרגמנטים
    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
