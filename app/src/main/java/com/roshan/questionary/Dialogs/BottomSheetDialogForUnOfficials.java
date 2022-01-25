package com.roshan.questionary.Dialogs;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.util.EntityUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roshan.questionary.Models.PostModel;
import com.roshan.questionary.databinding.BottomSheetHomeUnofficialsBinding;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

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

//            try {
//                URL imageUrl = new URL(url);
//                Bitmap image = BitmapFactory.decodeStream(imageUrl.openConnection().getInputStream());
//                Intent intent = new Intent(Intent.ACTION_SEND);
//                intent.setType("image/png");
//                intent.putExtra(Intent.EXTRA_STREAM, image);
//                startActivity(Intent.createChooser(intent , "Share"));
//            } catch(IOException e) {
//                System.out.println(e);
//            }
            dismiss();

        });


        binding.downloadPostHome.setOnClickListener(v -> {
            downloadFile(url);
            Toast.makeText(context, "Downloading....", Toast.LENGTH_LONG).show();
            dismiss();
        });

        return binding.getRoot();
    }


    private void loadData(){
        database.getReference().child("posts")
                .child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                PostModel model = snapshot.getValue(PostModel.class);
                url = model.getQuestionImage();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Check your internet connection", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Download Image ---------------------------->

    private void downloadFile(String url) {
        try {
            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            Uri uri = Uri.parse(url);

            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI)
                    .setAllowedOverRoaming(false)
                    .setTitle("Questionary")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, File.separator + "Questionary" + ".jpg");

            manager.enqueue(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}