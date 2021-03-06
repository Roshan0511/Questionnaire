package com.roshan.questionnaire.Fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roshan.questionnaire.Adapters.MyQuestionsAdapter;
import com.roshan.questionnaire.Models.PostModel;
import com.roshan.questionnaire.R;
import com.roshan.questionnaire.databinding.FragmentMyQuestionsBinding;

import java.util.ArrayList;
import java.util.List;

public class MyQuestionsFragment extends Fragment {

    FragmentMyQuestionsBinding binding;
    List<PostModel> list;
    MyQuestionsAdapter adapter;
    FirebaseAuth auth;
    FirebaseDatabase database;
    ProgressDialog pd;

    public MyQuestionsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMyQuestionsBinding.inflate(inflater, container, false);

        initView();

        binding.backBtn.setOnClickListener(v -> pressBackButton());

        return binding.getRoot();
    }

    private void initView(){
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        list = new ArrayList<>();

        pd = new ProgressDialog(requireContext(), R.style.AppCompatAlertDialogStyle);
        pd.setMessage("Please Wait...");
        pd.setCancelable(false);
    }

    private void setDataForPostAdapter() {
        pd.show();

        database.getReference().child("posts").addValueEventListener(new ValueEventListener() {
            @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (pd.isShowing()){
                    pd.dismiss();
                }

                list.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        PostModel post = dataSnapshot.getValue(PostModel.class);
                        assert post != null;
                        post.setPostId(dataSnapshot.getKey());
                        if (post.getUserId().equals(auth.getUid())) {
                            list.add(post);
                        }
                    }

                    adapter = new MyQuestionsAdapter(getContext(), list);
                    binding.recyclerView.setAdapter(adapter);
                    binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (pd.isShowing()){
                    pd.dismiss();
                }
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pressBackButton() {
        MyProfileFragment fragment = new MyProfileFragment();
        assert getFragmentManager() != null;
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.linearLayout, fragment);
        transaction.commit();
    }


    @Override
    public void onResume() {
        super.onResume();

        setDataForPostAdapter();

        requireView().setFocusableInTouchMode(true);
        requireView().requestFocus();
        requireView().setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                pressBackButton();
                return true;
            }
            return false;
        });
    }
}