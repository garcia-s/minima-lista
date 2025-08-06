package com.symmetrytechec.listaminima

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.EditText
import android.widget.TextView
import android.view.inputmethod.InputMethodManager
import android.content.Context
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.widget.Toast
import java.text.Normalizer
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var searchEditText: EditText
    private lateinit var appsRecyclerView: RecyclerView
    private lateinit var appAdapter: AppAdapter
    private var allApps = mutableListOf<AppInfo>()
    
    companion object {
        private const val REQUEST_QUERY_ALL_PACKAGES = 1001
    }
    
    private fun normalizeText(text: String): String {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .lowercase()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        WindowCompat.setDecorFitsSystemWindows(window, true)

        searchEditText = findViewById(R.id.searchEditText)
        appsRecyclerView = findViewById(R.id.appsRecyclerView)

        setupRecyclerView()
        setupSearch()
        setupAutoFocus()
        checkAndRequestPermission()
    }

    private fun setupRecyclerView() {
        appAdapter = AppAdapter { appInfo ->
            launchApp(appInfo)
        }
        appsRecyclerView.layoutManager = LinearLayoutManager(this)
        appsRecyclerView.adapter = appAdapter
    }

    private fun loadApps() {
        val packageManager = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        val resolveInfoList = packageManager.queryIntentActivities(intent, 0)
        allApps.clear()

        for (resolveInfo in resolveInfoList) {
            val appInfo = AppInfo(
                name = resolveInfo.loadLabel(packageManager).toString(),
                packageName = resolveInfo.activityInfo.packageName,
                className = resolveInfo.activityInfo.name
            )
            allApps.add(appInfo)
        }

        allApps.sortBy { it.name.lowercase() }
        appAdapter.updateApps(allApps)
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterApps(s.toString())
            }
        })
    }

    private fun setupAutoFocus() {
        searchEditText.requestFocus()
        searchEditText.post {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    override fun onResume() {
        super.onResume()
        setupAutoFocus()
    }

    private fun checkAndRequestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.QUERY_ALL_PACKAGES) 
            == PackageManager.PERMISSION_GRANTED) {
            loadApps()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.QUERY_ALL_PACKAGES),
                REQUEST_QUERY_ALL_PACKAGES
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            REQUEST_QUERY_ALL_PACKAGES -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadApps()
                } else {
                    Toast.makeText(
                        this,
                        "Permission needed to show installed apps",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun filterApps(query: String) {
        val filteredApps = if (query.isEmpty()) {
            allApps
        } else {
            val normalizedQuery = normalizeText(query)
            allApps.filter { normalizeText(it.name).contains(normalizedQuery) }
        }
        appAdapter.updateApps(filteredApps)
    }

    private fun launchApp(appInfo: AppInfo) {
        try {
            val intent = Intent()
            intent.component = android.content.ComponentName(appInfo.packageName, appInfo.className)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            searchEditText.text.clear()
        } catch (e: Exception) {
        }
    }

    data class AppInfo(
        val name: String,
        val packageName: String,
        val className: String
    )
} 