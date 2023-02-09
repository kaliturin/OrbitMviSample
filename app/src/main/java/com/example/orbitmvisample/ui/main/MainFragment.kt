package com.example.orbitmvisample.ui.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.orbitmvisample.R
import com.example.orbitmvisample.fetcher.Response
import com.example.orbitmvisample.fetcher.ResponseOrigin
import com.example.orbitmvisample.service.IntFetcherService
import com.example.orbitmvisample.vm.IntViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.orbitmvi.orbit.viewmodel.observe
import timber.log.Timber

class MainFragment : Fragment(R.layout.fragment_main) {
    private val viewModel: IntViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.button).setOnClickListener {
            val args = IntFetcherService.Arguments(100)
            viewModel.request(args)
        }

        viewModel.observe(viewLifecycleOwner, state = ::render)
    }

    private fun render(response: Response<Int>) {
        when (response) {
            is Response.Loading -> {
                Timber.d("Loading started")
            }
            is Response.Data -> {
                if (response.info.origin == ResponseOrigin.Fetcher) {
                    Timber.d("Loading stopped")
                } else if (response.info.origin == ResponseOrigin.Cache) {
                    Timber.d("From cache")
                }
                Timber.d("Data: ${response.value}")
            }
            is Response.Error -> {
                if (response.info.origin == ResponseOrigin.Fetcher) {
                    Timber.d("Loading stopped")
                }
                Timber.e("Error on loading")
            }
            else -> {
                Timber.e("Unknown response")
            }
        }
    }
}