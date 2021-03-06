package com.roshan.questionnaire.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.roshan.questionnaire.Dialogs.ExitAppDialog;
import com.roshan.questionnaire.Fragments.HomeFragment;
import com.roshan.questionnaire.Fragments.MyProfileFragment;
import com.roshan.questionnaire.Fragments.PostFragment;
import com.roshan.questionnaire.Fragments.QuestionSearchFragment;
import com.roshan.questionnaire.R;
import com.roshan.questionnaire.databinding.ActivityMainBinding;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    private static final int CAMERA_PERMISSION_CODE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).hide();

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        android.net.NetworkInfo data = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if ((wifi != null & data != null) && (wifi.isConnected() | data.isConnected())) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                        Manifest.permission.CAMERA
                }, CAMERA_PERMISSION_CODE);
            }

            callingFragments();
        }
        else {
            binding.progressBar2.setVisibility(View.VISIBLE);
            Toast toast = Toast.makeText(MainActivity.this, "No Internet Connection", Toast.LENGTH_LONG);
            toast.show();
        }
    }



    //Calling Fragments ---------------------->

    @SuppressLint("NonConstantResourceId")
    private void callingFragments(){
        HomeFragment homeFragment = new HomeFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.linearLayout, homeFragment);
        transaction.commit();

        binding.bottomNavigationBar.setOnNavigationItemSelectedListener(item -> {
            Fragment fragment = null;
            switch (item.getItemId()){
                case R.id.home:
                    if (binding.bottomNavigationBar.getSelectedItemId()==R.id.home){
                        return false;
                    }
                    fragment = new HomeFragment();
                    break;

                case R.id.search:
                    if (binding.bottomNavigationBar.getSelectedItemId()==R.id.search){
                        return false;
                    }
                    fragment = new QuestionSearchFragment();
                    break;

                case R.id.post:
                    if (binding.bottomNavigationBar.getSelectedItemId()==R.id.post){
                        return false;
                    }
                    fragment = new PostFragment();
                    break;

                case R.id.profile:
                    if (binding.bottomNavigationBar.getSelectedItemId()==R.id.profile){
                        return false;
                    }
                    fragment = new MyProfileFragment();
                    break;
            }

            FragmentTransaction transaction1 = getSupportFragmentManager().beginTransaction();
            assert fragment != null;
            transaction1.replace(R.id.linearLayout, fragment);
            transaction1.commit();

            return true;
        });
    }

    @Override
    public void onBackPressed() {
        if (binding.bottomNavigationBar.getSelectedItemId()==R.id.home ||
                binding.bottomNavigationBar.getSelectedItemId()==R.id.search ||
                binding.bottomNavigationBar.getSelectedItemId()==R.id.post ||
                binding.bottomNavigationBar.getSelectedItemId()==R.id.profile){

            ExitAppDialog dialog = new ExitAppDialog();
            dialog.show(getSupportFragmentManager(), dialog.getTag());
            dialog.setCancelable(false);

        }
        else {
            binding.bottomNavigationBar.setSelectedItemId(R.id.home);
        }
    }
}