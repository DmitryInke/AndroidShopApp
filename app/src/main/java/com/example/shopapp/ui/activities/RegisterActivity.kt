package com.example.shopapp.ui.activities

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import com.example.shopapp.R
import com.example.shopapp.firestore.FirestoreClass
import com.example.shopapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_register.et_email
import kotlinx.android.synthetic.main.activity_register.et_password

class RegisterActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        setupActionBar()

        btn_register.setOnClickListener {
            registerUser()
        }

        tv_login.setOnClickListener {
            // Here when the user click on login text we can either call the login activity or call the onBackPressed function.
            // We will call the onBackPressed function.
            onBackPressed()
        }
    }

    private fun setupActionBar() {

        setSupportActionBar(toolbar_register_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }

        toolbar_register_activity.setNavigationOnClickListener { onBackPressed() }
    }

    /**
     * A function to validate the entries of a new user.
     */
    private fun validateRegisterDetails(): Boolean {
        return when {
            TextUtils.isEmpty(et_first_name.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_first_name), true)
                false
            }

            TextUtils.isEmpty(et_last_name.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_last_name), true)
                false
            }

            TextUtils.isEmpty(
                et_email.text.toString()
                    .trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(et_email.text.toString())
                .matches() -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_valid_email), true)
                false
            }

            TextUtils.isEmpty(et_password.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                false
            }

            TextUtils.isEmpty(et_confirm_password.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(
                    resources.getString(R.string.err_msg_enter_confirm_password),
                    true
                )
                false
            }

            et_password.text.toString().trim { it <= ' ' } != et_confirm_password.text.toString()
                .trim { it <= ' ' } -> {
                showErrorSnackBar(
                    resources.getString(R.string.err_msg_password_and_confirm_password_mismatch),
                    true
                )
                false
            }
            !cb_terms_and_condition.isChecked -> {
                showErrorSnackBar(
                    resources.getString(R.string.err_msg_agree_terms_and_condition),
                    true
                )
                false
            }
            else -> {
                true
            }
        }
    }

    /**
     * A function to register the user with email and password using FirebaseAuth.
     */
    private fun registerUser() {

        // Check with validate function if the entries are valid or not.
        if (validateRegisterDetails()) {

            // Show the progress dialog.
            showProgressDialog(resources.getString(R.string.please_wait))

            val email: String = et_email.text.toString().trim { it <= ' ' }
            val password: String = et_password.text.toString().trim { it <= ' ' }

            // Create an instance and create a register a user with email and password.
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    // If the registration is successfully done
                    if (task.isSuccessful) {

                        // Firebase registered user
                        val firebaseUser: FirebaseUser = task.result!!.user!!


                        // Instance of User data model class.
                        val user = User(
                            firebaseUser.uid,
                            et_first_name.text.toString().trim { it <= ' ' },
                            et_last_name.text.toString().trim { it <= ' ' },
                            et_email.text.toString().trim { it <= ' ' }
                        )

                        // Pass the required values in the constructor.
                        FirestoreClass().registerUser(this@RegisterActivity, user)
                    } else {
                        hideProgressDialog()
                        // If the registering is not successful then show error message.
                        showErrorSnackBar(task.exception!!.message.toString(), true)
                    }
                }
        }
    }

    /**
     * A function to notify the success result of Firestore entry when the user is registered successfully.
     */
    fun userRegistrationSuccess() {
        hideProgressDialog()
        Toast.makeText(
            this@RegisterActivity,
            resources.getString(R.string.val_msg_registery_successfull),
            Toast.LENGTH_SHORT
        ).show()


        /**
         * Here the new user registered is automatically signed-in so we just sign-out the user from firebase
         * and send him to Intro Screen for Sign-In
         */
        FirebaseAuth.getInstance().signOut()
        finish()
    }

}