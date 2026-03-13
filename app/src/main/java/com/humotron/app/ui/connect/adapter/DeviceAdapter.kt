package com.humotron.app.ui.connect.adapter

//class DeviceAdapter(val onDeviceConnect: (DeviceModal) -> Unit) :
//    RecyclerView.Adapter<DeviceAdapter.ViewHolder>() {
//
//    private val list = arrayListOf<DeviceModal>()
//
//    class ViewHolder(val binding: ItemDeviceBinding) : RecyclerView.ViewHolder(binding.root)
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        return ViewHolder(
//            ItemDeviceBinding.inflate(
//                LayoutInflater.from(parent.context),
//                parent,
//                false
//            )
//        )
//    }
//
//    override fun getItemCount(): Int {
//        return list.size
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val device = list[position]
//        holder.binding.apply {
//            tvName.text = device.name
//            tvAddress.text = device.address
//
//            root.setOnClickListener {
//                onDeviceConnect(device)
//            }
//        }
//
//    }
//
//    fun addItem(device: DeviceModal) {
//        if (!list.map { it.address }.contains(device.address)) {
//            list.add(device)
//            notifyDataSetChanged()
//        }
//
//    }
//}