package com.example.otpverifcation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.otpverifcation.databinding.ActivityMainBinding
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import java.io.Serializable
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mAuth: FirebaseAuth
    private var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks? = null

    // progress bar
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        val mNumber = binding.etMobileNumber
        progressBar = binding.getProgress

        mAuth = FirebaseAuth.getInstance()

        mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                binding.btnGetOtp.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
                Log.d("OTPSentActivity", "onVerificationCompleted:$credential")
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                binding.btnGetOtp.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
                Log.w("OTPSentActivity", "onVerificationFailed", e)

            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d("OTPSentActivity", "onCodeSent:$verificationId")

                binding.btnGetOtp.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
                Toast.makeText(this@MainActivity, "Otp has been sent", Toast.LENGTH_SHORT).show()
                // Save verification ID and resending token so we can use them later


                val intent = Intent(this@MainActivity, OtpActivity::class.java)
                intent.putExtra("mNumber", mNumber.text.toString())
                intent.putExtra("VerificationId", verificationId)
                startActivity(intent)
                finish()

                Log.d("OTPSent", token.toString())
//
            }
        }

        binding.btnGetOtp.setOnClickListener {
            if (mNumber.text.toString().trim().isNotEmpty()) {
                if (mNumber.text.toString().length <= 10) {

                    binding.btnGetOtp.visibility = View.GONE
                    progressBar.visibility = View.VISIBLE
                    startPhoneNumberVerification(mNumber.text.toString().trim())


                } else {
                    Toast.makeText(this, "Please enter correct number", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Enter mobile number", Toast.LENGTH_SHORT).show()
            }

        }


    }

    private fun startPhoneNumberVerification(phoneN: String) {
        progressBar.visibility = View.VISIBLE

        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber("+91$phoneN")       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(mCallbacks!!)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)


    }


}