package com.example.finalproject.page;

/**
 * Created by Gerrys on 18/04/2018.
 */

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.widget.TextView;

import com.example.finalproject.Common.Common;
import com.example.finalproject.MainActivity;
import com.example.finalproject.R;
import com.example.finalproject.base.BaseActivity;
import com.example.finalproject.util.PreferenceUtil;


public class Splash extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);

        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            if(PreferenceUtil.isUserExist()){
                if(PreferenceUtil.getUser().getRole().equalsIgnoreCase(Common.ROLE_USER)){
                    startActivity(new Intent(getApplicationContext(), MainMenuUser.class));
                }else{
                    startActivity(new Intent(getApplicationContext(), MainMenu.class));
                }
            }else{
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
            finish();
        }, 1000L);

    }
}