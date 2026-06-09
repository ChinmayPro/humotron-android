package com.humotron.app.ui.device.dialog

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.humotron.app.R
import com.humotron.app.databinding.LayoutBottomsheetMeasurementTypeBinding
import com.humotron.app.ui.device.adapter.MeasurementInfo
import com.humotron.app.ui.device.adapter.MeasurementTypeAdapter

class MeasurementTypeBottomSheet :
    BottomSheetDialogFragment(R.layout.layout_bottomsheet_measurement_type) {

    private lateinit var binding: LayoutBottomsheetMeasurementTypeBinding
    private lateinit var myAdapter: MeasurementTypeAdapter
    private var onItemClick: ((MeasurementInfo) -> Unit)? = null

    companion object {
        const val TAG = "MeasurementTypeBottomSheet"
        private const val ARG_MEASUREMENT_LIST = "arg_measurement_list"

        fun newInstance(measurementList: ArrayList<MeasurementInfo>): MeasurementTypeBottomSheet {
            val fragment = MeasurementTypeBottomSheet()
            val args = Bundle()
            args.putParcelableArrayList(ARG_MEASUREMENT_LIST, measurementList)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = LayoutBottomsheetMeasurementTypeBinding.bind(view)

        binding.imageButtonClose.setOnClickListener {
            dismiss()
        }

        val measurementList =
            arguments?.getParcelableArrayList<MeasurementInfo>(ARG_MEASUREMENT_LIST) ?: arrayListOf()

        myAdapter = MeasurementTypeAdapter(
            measurementList, onItemClicked = {
                onItemClick?.invoke(it)
            })
        binding.recyclerViewItems.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = myAdapter
        }
    }

    fun setMeasurementSelectionListener(listener: (MeasurementInfo) -> Unit) {
        onItemClick = listener
    }
}