package com.example.otpverifcation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil.setContentView
import com.example.otpverifcation.databinding.ActivityOtpBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import java.lang.StringBuilder
import java.util.concurrent.TimeUnit

class OtpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpBinding
    private lateinit var enteredOTP: String
    private lateinit var mAuth: FirebaseAuth
    private var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks? = null
    private var mVerificationId: String? = null

    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = setContentView(this, R.layout.activity_otp)

        mAuth = FirebaseAuth.getInstance()

        val input1 = binding.otpBox1
        val input2 = binding.otpBox2
        val input3 = binding.otpBox3
        val input4 = binding.otpBox4
        val input5 = binding.otpBox5
        val input6 = binding.otpBox6
        val mNumber = binding.number


        progressBar = binding.verifyProgress
        val resendOTP = binding.resendOtp
        val verifyBtn = binding.verifyBtn

        val number = intent.getStringExtra("mNumber")

        mNumber.text = "+91-$number"

        mVerificationId = intent.getStringExtra("VerificationId")


        mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.

                Log.d("OTPSentActivity", "onVerificationCompleted:$credential")
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.

                Log.w("OTPSentActivity", "onVerificationFailed", e)

            }

            override fun onCodeSent(
                resendverificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d("OTPActivity", "onCodeSent:$resendverificationId")

                mVerificationId = resendverificationId

                // Save verification ID and resending token so we can use them later
//                    forcedResendToken = token
            }
        }

        verifyBtn.setOnClickListener {
            if (input1.text.toString().trim().isNotEmpty() &&
                input2.text.toString().trim().isNotEmpty() &&
                input3.text.toString().trim().isNotEmpty() &&
                input4.text.toString().trim().isNotEmpty() &&
                input5.text.toString().trim().isNotEmpty() &&
                input6.text.toString().trim().isNotEmpty()
            ) {
                enteredOTP = input1.text.toString() +
                        input2.text.toString() +
                        input3.text.toString() +
                        input4.text.toString() +
                        input5.text.toString() +
                        input6.text.toString()

                progressBar.visibility = View.VISIBLE
                verifyBtn.visibility = View.GONE

                if (mVerificationId != null) {
                    verifyPhoneNumberWithCode(mVerificationId, enteredOTP)

                }

            } else {
                Log.d("OTPActivity", "failed to read otp")
                Toast.makeText(this, "Please enter all the number", Toast.LENGTH_SHORT).show()
            }
        }


        resendOTP.setOnClickListener {
            resendVerificationCode(number!!)
            Toast.makeText(this, "Resending the OTP", Toast.LENGTH_SHORT).show()

        }

        moveOtpNumber()

    }

    private fun moveOtpNumber() {

        binding.otpBox1.doOnTextChanged { text, start, before, count ->
            if (text.toString().trim().isNotEmpty()) {
                binding.otpBox2.requestFocus()
            }

        }
        binding.otpBox2.doOnTextChanged { text, start, before, count ->
            if (text.toString().trim().isNotEmpty()) {
                binding.otpBox3.requestFocus()
            }

        }
        binding.otpBox3.doOnTextChanged { text, start, before, count ->
            if (text.toString().trim().isNotEmpty()) {
                binding.otpBox4.requestFocus()
            }

        }
        binding.otpBox4.doOnTextChanged { text, start, before, count ->
            if (text.toString().trim().isNotEmpty()) {
                binding.otpBox5.requestFocus()
            }

        }
        binding.otpBox5.doOnTextChanged { text, start, before, count ->
            if (text.toString().trim().isNotEmpty()) {
                binding.otpBox6.requestFocus()
            }

        }


    }


    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        progressBar.visibility = View.VISIBLE

        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneAuthCredential(credential)


    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {

        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->

                if (task.isSuccessful) {

                    progressBar.visibility = View.GONE

                    val intent = Intent(this, Dashboard::class.java)
                    startActivity(intent)
                    finish()

                    // Sign in success, update UI with the signed-in user's information
                    Log.d("OTPActivity", "signInWithCredential:success")

                    Toast.makeText(this, "You are logged in", Toast.LENGTH_SHORT).show()

//                    val user = task.result?.user

                } else {
                    // Sign in failed, display a message and update the UI
                    Log.d("OTPActivity", "signInWithCredential:failure", task.exception)

                    Toast.makeText(this, "Please enter correct OTP", Toast.LENGTH_SHORT).show()

                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid

                        progressBar.visibility = View.GONE
                        binding.verifyBtn.visibility = View.VISIBLE
                        Toast.makeText(this, "Entered code is invalid!", Toast.LENGTH_SHORT).show()
                    }
                    // Update UI
                }
            }
    }

    private fun resendVerificationCode(phoneN: String) {
        progressBar.visibility = View.VISIBLE

        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber("+91$phoneN")       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(mCallbacks!!) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

    }


}