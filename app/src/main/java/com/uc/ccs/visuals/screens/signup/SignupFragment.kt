import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.uc.ccs.visuals.R
import com.uc.ccs.visuals.databinding.FragmentSignupBinding
import com.uc.ccs.visuals.screens.signup.DIALOG_DURATION
import com.uc.ccs.visuals.screens.signup.SignupViewModel

data class User(
    val email: String,
    val firstName: String,
    val lastName: String,
    val roles: Int = 2
)

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
                    Toast.makeText(requireContext(), getString(R.string.login_error_message), Toast.LENGTH_SHORT).show()
                }
                is SignupViewModel.SignupState.ErrorSavingData -> {
                    loadingIndicator.visibility = View.GONE
                    Toast.makeText(requireContext(), "Failed to save user data", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
