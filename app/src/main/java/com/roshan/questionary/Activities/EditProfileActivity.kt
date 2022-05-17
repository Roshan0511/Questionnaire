package com.roshan.questionary.Activities

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.roshan.questionary.Models.UserModel
import com.roshan.questionary.R
import com.roshan.questionary.databinding.ActivityEditProfileBinding

class EditProfileActivity : AppCompatActivity() {

    private var binding: ActivityEditProfileBinding? = null
    private var auth: FirebaseAuth? = null
    private var databse: FirebaseDatabase? = null
    private var pd: ProgressDialog? = null
    private var profilePic = ""
    private var password = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        supportActionBar!!.hide()
        initView()

        pd!!.show()

        setData()

        binding!!.btnSave.setOnClickListener {
            saveBtnClick()
        }

        binding!!.backBtn.setOnClickListener {
            back()
        }
    }

    private fun initView() {
        auth = FirebaseAuth.getInstance()
        databse = FirebaseDatabase.getInstance()
        pd = ProgressDialog(this, R.style.AppCompatAlertDialogStyle)
        pd!!.setMessage("Please Wait...")
        pd!!.setCancelable(false)
    }

    private fun setData() {
        auth!!.uid?.let {
            databse!!.reference
                .child("Users")
                .child(it)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (pd!!.isShowing) {
                            pd!!.dismiss()
                        }
                        if (snapshot.exists()){
                            val user: UserModel? = snapshot.getValue(UserModel::class.java)

                            binding!!.etEditName.editText?.setText(user!!.name)
                            binding!!.etEditEmail.editText?.setText(user!!.email)
                            binding!!.etEditDescription.editText?.setText(user!!.summary)

                            profilePic = user!!.profilePic.toString()
                            password = user.password.toString()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        if (pd!!.isShowing) {
                            pd!!.dismiss()
                        }
                        Toast.makeText(this@EditProfileActivity, "Error : ${error.message}", Toast.LENGTH_SHORT).show()
                    }

                })
        }
    }


    private fun saveBtnClick() {

        pd!!.show()

        val userModel = UserModel()
        userModel.profilePic = profilePic
        userModel.password = password
        userModel.name = binding!!.etEditName.editText!!.text.toString()
        userModel.email = binding!!.etEditEmail.editText!!.text.toString()
        userModel.summary = binding!!.etEditDescription.editText!!.text.toString()

        auth!!.uid?.let {
            databse!!.reference
                .child("Users")
                .child(it)
                .setValue(userModel)
                .addOnCompleteListener {
                    Toast.makeText(this, "Details Updated", Toast.LENGTH_LONG).show()

                    if (pd!!.isShowing) {
                        pd!!.dismiss()
                    }

                    finish()
                }
        }
    }

    private fun back() {
        onBackPressed()
    }
}