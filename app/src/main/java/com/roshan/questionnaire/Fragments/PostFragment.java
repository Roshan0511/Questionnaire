package com.roshan.questionnaire.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.roshan.questionnaire.Activities.MainActivity;
import com.roshan.questionnaire.Dialogs.ShowingProfileDialog;
import com.roshan.questionnaire.Models.PostModel;
import com.roshan.questionnaire.Models.UserModel;
import com.roshan.questionnaire.R;
import com.roshan.questionnaire.databinding.FragmentPostBinding;

import java.util.Date;
import java.util.Objects;

public class PostFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    FragmentPostBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    PostModel postModel;
    Uri uri;
    String subjectTxt;
    ArrayAdapter<String> arrayAdapter;
    String[] subjects = {"Select", "Maths ","English","Science","Hindi","Computer Science","General Knowledge", "Political Science",
            "Geography", "Economics", "History",};

    public PostFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPostBinding.inflate(inflater, container, false);

        // Initialization ------------------>

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        postModel = new PostModel();

        binding.progressBar.setVisibility(View.VISIBLE);


        //Retrieve data from database and set profile pic and name ------------------------>

        database.getReference().child("Users").child(Objects.requireNonNull(auth.getUid())).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    UserModel userModel = snapshot.getValue(UserModel.class);
                    assert userModel != null;

                    if (getContext() != null){
                        Glide.with(requireActivity())
                                .load(userModel.getProfilePic())
                                .placeholder(R.drawable.placeholder)
                                .into(binding.profilePicPost);
                    }

                    binding.userNamePost.setText(userModel.getName());
                    binding.progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE);
            }
        });


        //Click on Upload Image ------------------>

        binding.uploadImage.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 11);
        });


        binding.profilePicPost.setOnClickListener(v -> {
            ShowingProfileDialog dialog = new ShowingProfileDialog(auth.getUid());
            dialog.show(((FragmentActivity)requireContext()).getSupportFragmentManager(), dialog.getTag());
            dialog.setCancelable(false);
        });


        //Click on Post Button --------------------->

        arrayAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_1, subjects);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.subjectSpinner.setAdapter(arrayAdapter);

        binding.subjectSpinner.setOnItemSelectedListener(this);

        binding.postBtn.setOnClickListener(v -> postBtnClicked());

        return binding.getRoot();
    }




    //Activity Result for Select Image ------------------>

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==11 && resultCode == Activity.RESULT_OK){
            assert data != null;
            if (data.getData() != null) {
                uri = data.getData();
                binding.questionImage.setVisibility(View.VISIBLE);
                binding.questionImage.setImageURI(uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    //Post Button Clicked ----------------->

    private void postBtnClicked(){
        binding.progressBar.setVisibility(View.VISIBLE);
        requireActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        if (binding.askQuestionET.getText().toString().trim().equals("")){
            binding.askQuestionET.setError("Not Empty");
            binding.progressBar.setVisibility(View.GONE);
            requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
        else if (uri != null){
            final StorageReference reference = storage.getReference().child("posts")
                    .child(Objects.requireNonNull(auth.getUid()))
                    .child(new Date().getTime() + "");
            reference.putFile(uri).addOnSuccessListener(taskSnapshot -> reference.getDownloadUrl().addOnSuccessListener(uri -> {

                postModel.setQuestionImage(uri.toString());
                postModel.setUserId(auth.getUid());
                postModel.setTime(new Date().getTime());
                postModel.setQuestionTxt(binding.askQuestionET.getText().toString());
                postModel.setSubject(subjectTxt);

                database.getReference().child("posts")
                        .push()
                        .setValue(postModel).addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Posted Successfully..", Toast.LENGTH_SHORT).show();
                    binding.progressBar.setVisibility(View.GONE);
                    requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                    binding.askQuestionET.setText(null);
                    binding.questionImage.setVisibility(View.GONE);

                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    startActivity(intent);
                    requireActivity().finish();
                });
            }));
        }
        else {
            postModel.setQuestionImage("");
            postModel.setUserId(auth.getUid());
            postModel.setTime(new Date().getTime());
            postModel.setQuestionTxt(binding.askQuestionET.getText().toString());
            postModel.setSubject(subjectTxt);

            database.getReference().child("posts")
                    .push()
                    .setValue(postModel).addOnSuccessListener(unused -> {
                Toast.makeText(getContext(), "Posted Successfully..", Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE);
                requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                binding.askQuestionET.setText(null);

                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
                requireActivity().finish();
            });
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position==0){
            subjectTxt = "";
        } else {
            subjectTxt = subjects[position];
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}