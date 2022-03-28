package com.trackmap.gps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.trackmap.gps.base.BaseActivity
import com.trackmap.gps.databinding.ActivityMainBinding
import com.trackmap.gps.homemap.ui.DrawerItemAdapter
import com.trackmap.gps.homemap.ui.HomeFragment
import com.trackmap.gps.homemap.viewmodel.HomeMapViewModel
import com.trackmap.gps.login.ui.LoginActivity
import com.trackmap.gps.preference.MyPreference
import com.trackmap.gps.preference.PrefKey
import com.trackmap.gps.utils.*
import java.util.*

class MainActivity : BaseActivity() {

    lateinit var navController: NavController
    lateinit var homeMapViewModel: HomeMapViewModel
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration

    lateinit var binding: ActivityMainBinding
    lateinit var toolbar: Toolbar
    lateinit var adapter: DrawerItemAdapter
    var isFakeLocation: Boolean = false
    private  val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeMapViewModel = ViewModelProvider(this)[HomeMapViewModel::class.java]
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        drawerLayout = binding.drawerLayout
        setSupportActionBar(binding.actionBar.toolbar)
        toolbar = binding.actionBar.toolbar

        navController = this.findNavController(R.id.myNavHostFragment)
        NavigationUI.setupWithNavController(binding.actionBar.toolbar, navController, drawerLayout)
        appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.actionBar.toolbar.setNavigationIcon(R.drawable.ic_drawer_icon)

        navController.addOnDestinationChangedListener { nc: NavController, nd: NavDestination, bundle: Bundle? ->
            if (nd.id == nc.graph.startDestination) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                binding.actionBar.toolbar.setNavigationIcon(R.drawable.ic_drawer_icon)
            }/*else if(nd.id == R.id.tracksFragment){
                binding.actionBar.toolbar.setNavigationIcon(R.drawable.ic_drawer_icon)
            } */ else {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
        }
        // navHeaderBinding = LayoutNavHeaderBinding.bind(binding.navView.getHeaderView(0))
        NavigationUI.setupWithNavController(binding.navView, navController)

        toolbar.setNavigationOnClickListener {
            hideKeyboard()
            val navHost = supportFragmentManager.findFragmentById(R.id.myNavHostFragment)
            navHost?.let { navFragment ->
                navFragment.childFragmentManager.primaryNavigationFragment?.let { fragment ->
                    if (fragment is HomeFragment) {
                        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                            drawerLayout.closeDrawer(GravityCompat.START)
                        } else {
                            drawerLayout.openDrawer(GravityCompat.START)
                        }
                        fragment.hideBottomSheetValues()

                    } else {
//                        navController.navigateUp()
                        onBackPressed()
                    }

                }
            }
        }


        val handler = object : DrawerItemAdapter.DrawerItemClick {
            override fun drawerItemClick(position: Int) {
                try {
                    when (position) {
                        0 -> {
                            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                            navController.navigate(R.id.homemapFragment)
//                        navController.navigate(R.id.action_homemapFragment_to_vehicalsListFragment)
                        }
                        1 -> {
                            navController.navigate(R.id.action_homemapFragment_to_tracksFragment)
                            return
                        }
                        2 -> {
                            navController.navigate(R.id.action_homemapFragment_to_notificationHome)
                            return
                        }
                        3 -> {
//                        navController.navigate(HomeMapFragmentDirections.actionHomemapFragmentToGeoZoneFragment("map"))
                            navController.navigate(R.id.action_homemapFragment_to_geoZoneFragment)
                            return
                        }

                        4 -> {
                            navController.navigate(R.id.action_homemapFragment_to_userSettingFragment)
                            return
                        }
                        5 -> {
                            navController.navigate(R.id.action_homemapFragment_to_reportFragment)
                            return
                        }
                        6 -> {
                            val checkFlag = MyPreference.getValueBoolean(PrefKey.IS_CEO_DATA, false)
                            val subscriptionDyas =
                                MyPreference.getValueBoolean(PrefKey.SUBSCRIPTION_DAYS, false)
                            val isActiveUser =
                                MyPreference.getValueString(PrefKey.IS_USER_ACTIVE, "active")!!
                                    .lowercase(Locale.getDefault()).equals("inactive", true)
                            if (checkFlag && !isActiveUser && subscriptionDyas) {
                                if (!isFakeLocation) {
                                    navController.navigate(R.id.action_homemapFragment_to_bindVehicleFragment)
                                } else {
                                    CommonAlertDialog.showAlertWithMessage(
                                        this@MainActivity, "You are using Fake GPS"
                                    )
                                    onBackPressed()
                                }
                            } else {
                                Toast.makeText(
                                    applicationContext,
                                    getString(R.string.you_can_not_access_this_option),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            return
                        }
                        7 -> {
                            navController.navigate(R.id.action_homemapFragment_to_contactUsFragment)
                            return
                        }
                        8 -> {
                            navController.navigate(R.id.action_homemapFragment_to_aboutUsFragment)
                            return
                        }
                        9 -> {
                            navController.navigate(R.id.action_homemapFragment_to_privacyPolicyFragment)
                            return
                        }
                    }
                } catch (e: Exception) {
                    DebugLog.e(e.message!!)
                }
            }
        }

        adapter = DrawerItemAdapter(this, handler)
        binding.layoutSideMenuDrawer.rvDrawerItem.layoutManager = GridLayoutManager(this, 3)
        binding.layoutSideMenuDrawer.rvDrawerItem.adapter = adapter
        if (Locale.getDefault().displayLanguage.equals("العربية")
        ) binding.layoutSideMenuDrawer.imgBack.rotation = 180F
        binding.layoutSideMenuDrawer.imgBack.setOnClickListener {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        }
        observeData()

        Log.d("TAG", "onCreate: nnf ${intent.getBooleanExtra("IS_FROM_LOGIN", false)}")
        if (intent.getBooleanExtra("IS_FROM_LOGIN", false)) {
            MyPreference.getValueString(Constants.USER_NAME, "")?.let {
                val isFirstTimeSignIn = MyPreference.getValueBoolean(PrefKey.IS_SIGNIN, false)
                if (!isFirstTimeSignIn) {
                    Log.d(TAG, "onCreate: TOKEN::1 $it")
                    homeMapViewModel.handler.removeCallbacksAndMessages(null)
                    homeMapViewModel.callSignInMethod(it)
                } else {
                    DebugLog.e("intent00")
                    callApi()
                }
            }
        } else {
            DebugLog.e("intent 199")
            callApi()
        }
    }

    fun callApi() {
        DebugLog.e("callApi")
        homeMapViewModel.handler.removeCallbacksAndMessages(null)

//        if (MyPreference.getValueString(PrefKey.SELECTED_SERVER_DATA,"")!!.contains("s3")){
//            homeMapViewModel.getCarList()
//        }else{
//
//        }
        homeMapViewModel.callsetToken()
    }

    private fun observeData() {
        homeMapViewModel.tokenExpiredCall.observe(this) {
            if (it) {
                if (!homeMapViewModel.isTimeOut) {
                    homeMapViewModel.isTimeOut = it
                    MyPreference.clearAllData()
                    val myIntent = Intent(this, LoginActivity::class.java)
                    myIntent.putExtra("logoutFromApp", true)
                    startActivity(myIntent)
                    finishAffinity()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // homeMapViewModel.handler.removeCallbacksAndMessages(null);

    }

    override fun onDestroy() {
        super.onDestroy()
        DebugLog.e("onDestroy")
        homeMapViewModel.handler.removeCallbacks(homeMapViewModel.updateTextTask)
    }


    /**
     * This method is used to hide the keyboard.
     */
    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    /**
     * Back press management
     */
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }


    @SuppressLint("MissingPermission")
    fun mapSettings(googleMap: GoogleMap) {
        googleMap.mapType = MyPreference.getValueInt(PrefKey.MAP_TYPE, GoogleMap.MAP_TYPE_NORMAL)
        googleMap.uiSettings.isCompassEnabled = false
        googleMap.uiSettings.isMapToolbarEnabled = false
        val requiredPermission = Manifest.permission.ACCESS_FINE_LOCATION
        val checkVal: Int = checkCallingOrSelfPermission(requiredPermission)
        if (checkVal == PackageManager.PERMISSION_GRANTED) googleMap.isMyLocationEnabled =
            MyPreference.getValueBoolean(PrefKey.IS_CURRENT_LOCATION, false)
        googleMap.uiSettings.isMyLocationButtonEnabled =
            MyPreference.getValueBoolean(PrefKey.IS_CURRENT_LOCATION, false)
        googleMap.uiSettings.isTiltGesturesEnabled = false
        googleMap.uiSettings.isRotateGesturesEnabled = true
        googleMap.uiSettings.isZoomControlsEnabled = true
    }
}