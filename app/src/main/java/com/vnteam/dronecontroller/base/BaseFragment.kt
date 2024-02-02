package com.vnteam.dronecontroller.base

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.vnteam.dronecontroller.utils.Extensions.safeSingleObserve
import com.vnteam.dronecontroller.MainActivity

abstract class BaseFragment<B : ViewBinding, VM : BaseViewModel> : BaseBindingFragment<B>() {

    abstract val viewModelClass: Class<VM>
    abstract fun observeLiveData()

    open val viewModel: VM by lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProvider(this)[viewModelClass]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setExceptionMessageDisplaying()
        observeLiveData()
        setProgressVisibility()
    }

    private fun setExceptionMessageDisplaying() {
        viewModel.exceptionLiveData.safeSingleObserve(viewLifecycleOwner) { exception ->
            showMessage(exception, true)
        }
    }

    private fun setProgressVisibility() {
        viewModel.isProgressProcessLiveData.safeSingleObserve(viewLifecycleOwner) { isVisible ->
            (activity as? MainActivity)?.setProgressVisibility(isVisible)
        }
    }
}