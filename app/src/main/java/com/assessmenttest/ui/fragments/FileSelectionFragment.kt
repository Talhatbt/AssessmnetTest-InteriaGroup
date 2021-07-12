package com.assessmenttest.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.assessmenttest.R
import com.assessmenttest.common.LastAdapter
import com.assessmenttest.common.SimpleCallback
import com.assessmenttest.constants.Consts
import com.assessmenttest.databinding.FragmentFileSelectionBinding
import com.assessmenttest.extensions.bindViewModel
import com.assessmenttest.extensions.replaceFragment
import com.assessmenttest.helper.PrefMgr
import com.assessmenttest.interfaces.StringCallBack
import com.assessmenttest.models.TravelDestinations
import com.assessmenttest.models.TravellingData
import com.assessmenttest.models.TravellingViewModel
import com.assessmenttest.ui.MainApp
import com.assessmenttest.utility.Utils

class FileSelectionFragment : Fragment() {

    private val viewModel by bindViewModel<TravellingViewModel>()
    var ACTIVITY_CHOOSE_FILE = 110
    lateinit var binding: FragmentFileSelectionBinding
    lateinit var travelAdapter: LastAdapter<TravellingData>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentFileSelectionBinding.inflate(inflater, container, false).apply {
            binding = this
        }.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        setListeners()
        setRecyclerview()
    }

    // setup recyclerview by plotting all dates fetched from db
    private fun setRecyclerview() {
        val layoutManager = LinearLayoutManager(activity)
        binding.rvTravel.layoutManager = layoutManager
        binding.rvTravel.setHasFixedSize(true)
        travelAdapter = LastAdapter(
            R.layout.layout_row_travel_date,
            object : LastAdapter.OnItemClickListener<TravellingData> {
                override fun onItemClick(model: TravellingData) {
                    binding.btnDetail.visibility = View.GONE
                    replaceFragment(
                        R.id.container,
                        TravellingDetailFragment.newInstance(model.date.toString(),
                            object : SimpleCallback<Any> {
                                override fun onCallBack(list: MutableList<TravelDestinations>) {
                                    binding.btnDetail.visibility = View.VISIBLE
                                }
                            }),
                        true
                    )
                }
            })

        binding.rvTravel.adapter = travelAdapter
    }


    private fun selectCSVFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        startActivityForResult(Intent.createChooser(intent, "Open CSV"), ACTIVITY_CHOOSE_FILE)

    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {
            if (data?.data?.let { Utils.getMimeType(requireActivity(), it) } == Consts.FILE_EXT) {
                Utils.copyFileToInternalStorage(
                    Uri.parse(data?.data.toString()),
                    "",
                    StringCallBack { msg ->
                        if (msg == "Success") {
                            Utils.importCSV()
                            Handler().postDelayed({

                                PrefMgr.get(requireActivity())
                                    .putBoolean(PrefMgr.Keys.FIRST_TIME, true)
                                viewModel.fetchTravellingData()
                                Toast.makeText(
                                    MainApp.getContext(),
                                    "Data Imported SuccessFully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }, 1000)
                        }
                    })
            } else {
                Toast.makeText(
                    activity,
                    "Format not supported. Please select .csv",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setListeners() {
        binding.btnChooseFile.setOnClickListener {
            selectCSVFile()
        }

        binding.btnDetail.setOnClickListener {
            replaceFragment(
                R.id.container,
                ShowDetailFragment.newInstance(viewModel),
                true
            )
        }
    }


    private fun init() {

        binding.viewmodel = viewModel
        binding.btnDetail.visibility = View.GONE
        if (PrefMgr.get(requireActivity()).getBoolean(PrefMgr.Keys.FIRST_TIME)) {
            viewModel.fetchTravellingData()
            binding.btnDetail.visibility = View.VISIBLE
        }
    }
}