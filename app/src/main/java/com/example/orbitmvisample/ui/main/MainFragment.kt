package com.example.orbitmvisample.ui.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.orbitmvisample.R
import com.example.orbitmvisample.databinding.FragmentMainBinding
import com.example.orbitmvisample.di.IntViewModel
import com.example.orbitmvisample.fetcher.Response
import com.example.orbitmvisample.service.IntFetcherService
import com.example.orbitmvisample.ui.helpers.logger.RecyclerLoggerHelper
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainFragment : Fragment(R.layout.fragment_main) {
    private val binding by viewBinding(FragmentMainBinding::bind)
    private val viewModel: IntViewModel by viewModel()
    private lateinit var log: RecyclerLoggerHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        log = RecyclerLoggerHelper(binding.logRecyclerView)

        binding.button.setOnClickListener {
            val args = IntFetcherService.Arguments(100)
            lifecycleScope.launch {
                viewModel
                    .ignorePendingRequests()
                    .request(args, context)
            }
        }

        viewModel.observe(viewLifecycleOwner, state = ::render)
    }

    private fun render(response: Response<Int>) {
        val id = response.info.requestId
        val origin = response.info.origin
        when (response) {
            is Response.Loading -> {
                log.d("Request id=$id loading started")
            }
            is Response.Data -> {
                log.i("Request id=$id loading finished from $origin")
                log.i("Data: ${response.value}")
            }
            is Response.Error -> {
                log.e("Request id=$id error on loading from $origin")
            }
            is Response.Cancelled -> {
                log.e("Request id=$id cancelled from $origin")
            }
            else -> {
            }
        }
    }
}