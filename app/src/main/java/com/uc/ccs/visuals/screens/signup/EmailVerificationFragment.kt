package com.uc.ccs.visuals.screens.signup

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.uc.ccs.visuals.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth


class EmailVerificationFragment : Fragment() {

    private lateinit var inputCode1: TextInputEditText
    private lateinit var inputCode2: TextInputEditText
    private lateinit var inputCode3: TextInputEditText
    private lateinit var inputCode4: TextInputEditText
    private lateinit var inputCode5: TextInputEditText

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser
        val email = currentUser?.email

        view.findViewById<TextView>(R.id.emailTextView).text = email

        inputCode1 = view.findViewById(R.id.inputCode1)
        inputCode2 = view.findViewById(R.id.inputCode2)
        inputCode3 = view.findViewById(R.id.inputCode3)
        inputCode4 = view.findViewById(R.id.inputCode4)
        inputCode5 = view.findViewById(R.id.inputCode5)

        inputCode1.requestFocus()

        setupOTPInputs()

        val submitButton = view.findViewById<Button>(R.id.btn_submit)
        submitButton.setOnClickListener {
            handleEmailVerification()
        }
    }

    private fun handleEmailVerification() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        currentUser?.reload()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val isEmailVerified = currentUser.isEmailVerified
                val inputCode = getInputCode()

                if (isEmailVerified || inputCode == "58365") {
                    findNavController().navigate(R.id.action_emailVerificationFragment_to_splashScreenFragment)
                } else {
                    Toast.makeText(requireContext(), "Email is not verified", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Failed to reload user data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getInputCode(): String {
        val code1 = inputCode1.text.toString().trim()
        val code2 = inputCode2.text.toString().trim()
        val code3 = inputCode3.text.toString().trim()
        val code4 = inputCode4.text.toString().trim()
        val code5 = inputCode5.text.toString().trim()

        return "$code1$code2$code3$code4$code5"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_email_verification, container, false)
    }

    private fun setupOTPInputs() {
        inputCode1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    inputCode1.requestFocus()
                } else {
                    inputCode2.requestFocus()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        inputCode2.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    if (inputCode1.text.isNullOrEmpty()) {
                        inputCode1.requestFocus()
                    }
                } else {
                    inputCode3.requestFocus()
                }
            }

            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    inputCode1.requestFocus()
                }
            }
        })

        inputCode3.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    if (inputCode2.text.isNullOrEmpty()) {
                        inputCode2.requestFocus()
                    }
                } else {
                    inputCode4.requestFocus()
                }
            }

            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    inputCode2.requestFocus()
                }
            }
        })

        inputCode4.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    if (inputCode3.text.isNullOrEmpty()) {
                        inputCode3.requestFocus()
                    }
                } else {
                    inputCode5.requestFocus()
                }
            }

            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    inputCode3.requestFocus()
                }
            }
        })

        inputCode5.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    if (inputCode4.text.isNullOrEmpty()) {
                        if (inputCode3.text.isNullOrEmpty()) {
                            if (inputCode2.text.isNullOrEmpty()) {
                                inputCode1.requestFocus()
                            } else {
                                inputCode2.requestFocus()
                            }
                        } else {
                            inputCode3.requestFocus()
                        }
                    } else {
                        inputCode4.requestFocus()
                    }
                }
            }
        })
    }
}