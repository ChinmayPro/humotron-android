package com.humotron.app.ui.connect.dialog


import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.humotron.app.R
import com.humotron.app.databinding.LayoutBottomsheetDeviceSelectionBinding
import com.humotron.app.ui.connect.adapter.DeviceInfo
import com.humotron.app.ui.connect.adapter.DeviceListAdapter

class DeviceSelectionBottomSheet :
    BottomSheetDialogFragment(R.layout.layout_bottomsheet_device_selection) {

    private lateinit var binding: LayoutBottomsheetDeviceSelectionBinding
    private lateinit var myAdapter: DeviceListAdapter // Your adapter instance
    private var onItemClick: ((DeviceInfo) -> Unit)? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = LayoutBottomsheetDeviceSelectionBinding.bind(view)

        binding.imageButtonClose.setOnClickListener {
            dismiss() // Close the bottom sheet
        }

        // Setup RecyclerView
        myAdapter = DeviceListAdapter(
            arrayListOf(
                DeviceInfo(
                    R.drawable.ic_bg_ring,
                    "Humotron Smart Ring",
                    "Connect to sync sleep & recovery metrics"
                ), DeviceInfo(
                    R.drawable.ic_bg_ring,
                    "Humotron Wrist Band",
                    "Health tracking smart band"
                )
            ), onItemClicked = {
                onItemClick?.invoke(it)
            })
        binding.recyclerViewItems.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = myAdapter
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState)


    }

    fun setDeviceSelectionListener(listener: (DeviceInfo) -> Unit) {
        onItemClick = listener
    }

    // Optional: To make the bottom sheet expand to full height initially
    // override fun onStart() {
    //     super.onStart()
    //     val dialog = dialog
    //     if (dialog != null) {
    //         val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
    //         bottomSheet?.let {
    //             val behavior = BottomSheetBehavior.from(it)
    //             behavior.state = BottomSheetBehavior.STATE_EXPANDED
    //         }
    //     }
    // }
}