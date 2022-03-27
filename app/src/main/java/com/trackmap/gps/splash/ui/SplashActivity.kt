package com.trackmap.gps.splash.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.FirebaseApp
import com.trackmap.gps.R
import com.trackmap.gps.base.BaseActivity
import com.trackmap.gps.databinding.ActivitySplashBinding
import com.trackmap.gps.login.ui.LoginActivity
import com.trackmap.gps.splash.viewmodel.SplashViewModel
import com.trackmap.gps.utils.Utils


class SplashActivity : BaseActivity() {
    lateinit var splashViewModel: SplashViewModel
    lateinit var binding: ActivitySplashBinding
    private lateinit var handler: Handler
    var timeDelay : Long = 1100


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(applicationContext)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        splashViewModel =
            ViewModelProviders.of(this@SplashActivity).get(SplashViewModel::class.java)

      /*  val animFadein: Animation = AnimationUtils.loadAnimation(applicationContext, R.anim.right_to_left)

        // start the animation

        binding.txtName.startAnimation(animFadein)
*/
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
