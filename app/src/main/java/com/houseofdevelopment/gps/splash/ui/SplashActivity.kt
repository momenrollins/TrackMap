package com.houseofdevelopment.gps.splash.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.FirebaseApp
import com.houseofdevelopment.gps.R
import com.houseofdevelopment.gps.base.BaseActivity
import com.houseofdevelopment.gps.databinding.ActivitySplashBinding
import com.houseofdevelopment.gps.login.ui.LoginActivity
import com.houseofdevelopment.gps.splash.viewmodel.SplashViewModel
import com.houseofdevelopment.gps.utils.Utils


class SplashActivity : BaseActivity() {
    lateinit var splashViewModel: SplashViewModel
    lateinit var binding: ActivitySplashBinding
    private lateinit var handler: Handler
    var timeDelay : Long = 1000


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(applicationContext)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        splashViewModel =
            ViewModelProviders.of(this@SplashActivity).get(SplashViewModel::class.java)

       /* val animFadein: Animation = AnimationUtils.loadAnimation(applicationContext, R.anim.left_to_right)

        // start the animation

        binding.txtName.startAnimation(animFadein)*/

        //Check For Permission
        //checkPermision()
        handler = Handler()
        handler.postDelayed({
            val intent = Intent(this@SplashActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }, timeDelay)

        Utils.deleteFile(this)
    }
}
