package com.vnteam.dronecontroller.login

import android.os.Bundle
import android.view.View
import com.vnteam.dronecontroller.base.BaseFragment
import com.vnteam.dronecontroller.databinding.FragmentLoginBinding

class LoginFragment : BaseFragment<FragmentLoginBinding, LoginViewModel>() {

    override val viewModelClass = LoginViewModel::class.java
    override fun getViewBinding() = FragmentLoginBinding.inflate(layoutInflater)

    override fun observeLiveData() {
        viewModel.resultLiveData.observe(viewLifecycleOwner) { result ->
            binding?.tvLoginStateInfo?.text = result
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setClickListeners()
    }

    private fun setClickListeners() {
        binding?.btnLogin?.setOnClickListener { loginAccount() }
        binding?.btnLogout?.setOnClickListener { viewModel.logoutAccount() }
    }

    private fun loginAccount() {
        /*context?.let {
            UserAccountManager.getInstance().logIntoDJIUserAccount(
                it,
                object : CommonCallbacks.CompletionCallbackWith<UserAccountState> {
                    override fun onSuccess(userAccountState: UserAccountState) {
                        binding?.tvLoginStateInfo?.text = "Logged in " + userAccountState.name
                        viewModel.getProductInfo()
                    }

                    override fun onFailure(error: DJIError) {
                        showMessage(
                            "Login Error:"
                                    + error.description, true
                        )
                    }
                })
        }*/
    }

    private fun logoutAccount() {
        /*UserAccountManager.getInstance().logoutOfDJIUserAccount { error: DJIError? ->
            if (null == error) {
                binding?.tvLoginStateInfo?.text = "Logout "
            } else {
                showMessage(
                    "Logout Error:"
                            + error.description, true
                )
            }
        }*/
    }
}