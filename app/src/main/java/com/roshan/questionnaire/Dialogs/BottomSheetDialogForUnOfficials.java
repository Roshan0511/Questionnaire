package com.roshan.questionnaire.Dialogs;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roshan.questionnaire.Models.PostModel;
import com.roshan.questionnaire.databinding.BottomSheetHomeUnofficialsBinding;
import java.io.File;

public class BottomSheetDialogForUnOfficials extends BottomSheetDialogFragment {

    BottomSheetHomeUnofficialsBinding binding;
    Context context;
    String postId;
    String userId;
    FirebaseDatabase database;
    FirebaseAuth auth;
    String url;

    public BottomSheetDialogForUnOfficials(Context context, String postId, String userId) {
        this.postId = postId;
        this.userId = userId;
        this.context = context;
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @SuppressLint("WrongConstant")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = BottomSheetHomeUnofficialsBinding.inflate(inflater, container, false);
        loadData();

        binding.sharePostHome.setOnClickListener(v -> {
            Intent intent2 = new Intent();
            intent2.setAction(Intent.ACTION_SEND);
            intent2.setType("text/plain");
            intent2.putExtra(Intent.EXTRA_TEXT, "Here your Question " + url);
            startActivity(Intent.createChooser(intent2, "Share via"));

            dismiss();

        });


        binding.downloadPostHome.setOnClickListener(v -> {

            if (url.isEmpty()){
                Toast.makeText(context, "Image Not Available", Toast.LENGTH_SHORT).show();
            }
            else{
                downloadFile(url);
            }

            dismiss();
        });

        return binding.getRoot();
    }


    private void loadData(){
        database.getReference().child("posts")
                .child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    PostModel model = snapshot.getValue(PostModel.class);
                    assert model != null;
                    url = model.getQuestionImage();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Check your internet connection", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Download Image ---------------------------->

    private void downloadFile(String url) {

        ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        if(ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED){
            try {
                DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                Uri uri = Uri.parse(url);

                DownloadManager.Request request = new DownloadManager.Request(uri);
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI)
                        .setAllowedOverRoaming(false)
                        .setTitle("Questionnaire")
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, File.separator + "Questionnaire" + ".jpg");

                manager.enqueue(request);

                Toast.makeText(context, "Downloading....", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }
}