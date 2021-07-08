package com.assessmenttest.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.assessmenttest.R
import com.assessmenttest.common.LastAdapter
import com.assessmenttest.common.SimpleCallback
import com.assessmenttest.constants.Consts
import com.assessmenttest.databinding.ActivityMainBinding
import com.assessmenttest.extensions.bindViewModel
import com.assessmenttest.extensions.replaceFragment
import com.assessmenttest.helper.PrefMgr
import com.assessmenttest.interfaces.StringCallBack
import com.assessmenttest.models.TravelDestinations
import com.assessmenttest.models.TravellingData
import com.assessmenttest.models.TravellingViewModel
import com.assessmenttest.ui.fragments.ShowDetailFragment
import com.assessmenttest.ui.fragments.TravellingDetailFragment
import com.assessmenttest.utility.Utils


class MainActivity : AppCompatActivity() {

    private val viewModel by bindViewModel<TravellingViewModel>()

    var ACTIVITY_CHOOSE_FILE = 110
    lateinit var binding: ActivityMainBinding
    lateinit var textView: AppCompatTextView
    lateinit var activity: Activity
    lateinit var travelAdapter: LastAdapter<TravellingData>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.toolbar))

        init()
        setListeners()
        checkPermission()
        setRecyclerview()
    }


    private fun setListeners() {
        binding.btnChooseFile.setOnClickListener {
            selectCSVFile()
        }

        binding.btnDetail.setOnClickListener {
            binding.btnDetail.visibility = View.GONE
            replaceFragment(
                R.id.container,
                ShowDetailFragment.newInstance(viewModel, 1, object : SimpleCallback<Any> {
                    override fun onCallBack(list: MutableList<TravelDestinations>) {
                        binding.btnDetail.visibility = View.VISIBLE
                    }
                }),
                true
            )
        }
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
                        TravellingDetailFragment.newInstance(model.date.toString(),object : SimpleCallback<Any> {
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

    private fun init() {
        activity = this
        binding.viewmodel = viewModel
        binding.btnDetail.visibility = View.GONE
        if (PrefMgr.get(activity).getBoolean(PrefMgr.Keys.FIRST_TIME)) {
            viewModel.fetchTravellingData()
            binding.btnDetail.visibility = View.VISIBLE
        }
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
        if (resultCode == RESULT_OK) {
            if (data?.data?.let { Utils.getMimeType(activity, it) } == Consts.FILE_EXT) {
                Utils.copyFileToInternalStorage(
                    Uri.parse(data?.data.toString()),
                    "",
                    StringCallBack { msg ->
                        if (msg == "Success") {
                            Utils.importCSVFromFile()
                            Handler().postDelayed({
                                PrefMgr.get(activity).putBoolean(PrefMgr.Keys.FIRST_TIME, true)
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


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            && grantResults[1] == PackageManager.PERMISSION_GRANTED
        ) {
        } else {
        }
    }

    /**
     * check storage permission is required
     */
    private fun checkPermission() {
        if ((ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED) ||
            (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED)

        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ), 1000
            )
        }
    }
}
