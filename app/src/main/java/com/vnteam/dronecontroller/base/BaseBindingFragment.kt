package com.vnteam.dronecontroller.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.vnteam.dronecontroller.MainActivity

abstract class BaseBindingFragment<VB : ViewBinding> : Fragment() {

    protected open var binding: VB? = null
    abstract fun getViewBinding(): VB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = getViewBinding()
        return binding?.root
    }



    fun showMessage(message: String, isError: Boolean = true) {
        (activity as? MainActivity)?.showToast(message)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

}