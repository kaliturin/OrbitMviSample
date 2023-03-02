package com.example.orbitmvisample.ui.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.orbitmvisample.R
import com.example.orbitmvisample.apierrorhandler.impl.AppErrorHandlerPropagator
import com.example.orbitmvisample.di.IntViewModel
import com.example.orbitmvisample.fetcher.Response
import com.example.orbitmvisample.service.IntFetcherService
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.orbitmvi.orbit.viewmodel.observe
import timber.log.Timber

class MainFragment : Fragment(R.layout.fragment_main) {
    private val viewModel: IntViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.button).setOnClickListener {
            val args = IntFetcherService.Arguments(100)
            viewModel
                .errorHandlerSettings(AppErrorHandlerPropagator.settings(suppressAlert = true))
                .ignorePendingRequests()
                .request(args)
        }

        viewModel.observe(viewLifecycleOwner, state = ::render)
    }

    private fun render(response: Response<Int>) {
        val id = response.info.requestId
        val origin = response.info.origin
        when (response) {
            is Response.Loading -> {
                Timber.d("Id=$id loading started")
            }
            is Response.Data -> {
                Timber.w("Id=$id loading finished from $origin")
                Timber.w("Data: ${response.value}")
            }
            is Response.Error -> {
                Timber.e("Id=$id error on loading from $origin")
            }
            else -> {
            }
        }
    }
}