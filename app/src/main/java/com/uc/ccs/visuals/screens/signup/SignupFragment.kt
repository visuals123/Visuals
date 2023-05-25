import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.jakewharton.rxbinding2.widget.RxTextView
import com.uc.ccs.visuals.R
import com.uc.ccs.visuals.databinding.FragmentSignupBinding
import com.uc.ccs.visuals.screens.signup.DIALOG_DURATION
import com.uc.ccs.visuals.screens.signup.SignupViewModel
import io.reactivex.Observable
import java.util.regex.Pattern

data class User(
    val email: String,
    val firstName: String,
    val lastName: String,
    val roles: Int = 2
)
@SuppressLint("CheckResult")
class SignupFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var viewModel: SignupViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSignupBinding.inflate(inflater, container, false)

        firebaseAuth = FirebaseAuth.getInstance()

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(SignupViewModel::class.java)

        binding.setupViews()
        binding.observeSignupState()

    //Firstname Validation
        val fnameStream = RxTextView.textChanges(binding.tietFirstname)
            .skipInitialValue()
            .map { fname ->
                fname.isEmpty()
            }
        fnameStream.subscribe{
            showFirstNameExistAlert(it)
        }

    //Lastname Validation
        val lnameStream = RxTextView.textChanges(binding.tietLastname)
            .skipInitialValue()
            .map { lname ->
                lname.isEmpty()
            }
        lnameStream.subscribe{
            showLastNameExistAlert(it)
        }

    //Email Validation
        val emailStream  = RxTextView.textChanges(binding.emailEt)
            .skipInitialValue()
            .map { email ->
                !Patterns.EMAIL_ADDRESS.matcher(email).matches()
            }
        emailStream.subscribe{
            showEmailValidAlert(it)
        }

    //Password Validation
        val passwordStream = RxTextView.textChanges(binding.passET)
            .skipInitialValue()
            .map { password ->
                password.length < 8
            }
        passwordStream.subscribe {
            showTextMinimalAlert(it, "Password")
        }

    //Confirm Password Validation
        val confirmPasswordStream = Observable.merge(
            RxTextView.textChanges(binding.passET)
                .skipInitialValue()
                .map{ password ->
                    password.toString() != binding.confirmPassEt.text.toString()
                },
            RxTextView.textChanges(binding.confirmPassEt)
                .skipInitialValue()
                .map { confirmPassword ->
                    confirmPassword.toString() != binding.passET.text.toString()
                }
        )
        confirmPasswordStream.subscribe{
            showPasswordConfirmAlert(it)
        }

    //Button Enable
        val invalidFieldStream = Observable.combineLatest(
            fnameStream,
            lnameStream,
            emailStream,
            passwordStream,
            confirmPasswordStream,
            { fnameInvalid: Boolean, lnameInvalid: Boolean, emailInvalid: Boolean, passwordInvalid: Boolean, confirmPasswordInvalid: Boolean ->
                !fnameInvalid && !lnameInvalid && !emailInvalid && !passwordInvalid && !confirmPasswordInvalid
            })
        invalidFieldStream.subscribe{isValid ->
            if (isValid) {
                binding.btnSignup.isEnabled = true
                binding.btnSignup.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.colorPrimary)
            }
            else {
                binding.btnSignup.isEnabled = false
                binding.btnSignup.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.md_blue_grey_600)
            }
        }
    }

    private fun FragmentSignupBinding.setupViews() {
        btnSignup.setOnClickListener {
            val email = emailEt.text.toString().trim()
            val pass = passET.text.toString()
            val confirmPass = confirmPassEt.text.toString()
            val firstName = tietFirstname.text.toString().trim()
            val lastName = tietLastname.text.toString().trim()

            if (email.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty() && firstName.isNotEmpty() && lastName.isNotEmpty()) {
                if (pass == confirmPass) {
                    loadingIndicator.visibility = View.VISIBLE
                    enableFields(false)

                    val user = User(email, firstName, lastName)
                    viewModel.signupUser(user, pass)
                } else {
                    enableFields(true)
                    Toast.makeText(requireContext(), getString(R.string.password_do_not_match), Toast.LENGTH_SHORT).show()
                }
            } else {
                enableFields(true)
                Toast.makeText(requireContext(), getString(R.string.please_fill_in_all_required_fields), Toast.LENGTH_SHORT).show()
            }
        }

        tvToLogin.setOnClickListener {
            findNavController().navigate(R.id.action_signupFragment_to_loginFragment)
        }
    }

    private fun FragmentSignupBinding.enableFields(bool: Boolean) {
        emailEt.isEnabled = bool
        passET.isEnabled = bool
        confirmPassEt.isEnabled = bool
        tietFirstname.isEnabled = bool
        tietLastname.isEnabled = bool
    }

    private fun FragmentSignupBinding.observeSignupState() {
        viewModel.signupState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SignupViewModel.SignupState.Success -> {
                    loadingIndicator.visibility = View.GONE
                    Handler().postDelayed({
                        findNavController().navigate(R.id.action_signupFragment_to_loginFragment)
                    }, DIALOG_DURATION)
                }
                is SignupViewModel.SignupState.Failure -> {
                    loadingIndicator.visibility = View.GONE
                    Toast.makeText(requireContext(), state.error.localizedMessage, Toast.LENGTH_SHORT).show()
                }
                is SignupViewModel.SignupState.ErrorSavingData -> {
                    loadingIndicator.visibility = View.GONE
                    Toast.makeText(requireContext(), "Failed to save user data", Toast.LENGTH_SHORT).show()
                }
            }
            enableFields(true)
        }
    }

    private fun showFirstNameExistAlert(isNotValid: Boolean){
        binding.tietFirstname.error = if (isNotValid) "Please enter your firstname" else null
    }

    private fun showLastNameExistAlert(isNotValid: Boolean){
        binding.tietLastname.error = if (isNotValid) "Please enter your lastname" else null
    }

    private fun showTextMinimalAlert(isNotValid: Boolean, text: String) {
        if(text == "Password")
            binding.passET.error = if(isNotValid) "Password must be more than 8 characters" else null
    }

    private fun showEmailValidAlert(isNotValid: Boolean) {
        binding.emailEt.error = if(isNotValid) "Must enter a valid email" else null
    }

    private fun showPasswordConfirmAlert(isNotValid: Boolean) {
        binding.confirmPassEt.error = if(isNotValid) "Password must be the same!" else null
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
