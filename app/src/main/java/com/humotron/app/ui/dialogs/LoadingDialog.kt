package com.humotron.app.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.content.res.AppCompatResources
import com.humotron.app.R
import com.humotron.app.databinding.DialogLoadingBinding

class LoadingDialog(context: Context) : Dialog(context, R.style.Theme_Dialog) {

    private lateinit var binding: DialogLoadingBinding
    var loadingMessage: String? = null
        set(value) {
            field = value
            binding.loadingMessage.text = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(false)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        window?.setBackgroundDrawable(
            AppCompatResources.getDrawable(
                context,
                android.R.color.transparent
            )
        )
        binding = DialogLoadingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadingMessage?.let {
            binding.loadingMessage.text = loadingMessage
        }
    }


}
