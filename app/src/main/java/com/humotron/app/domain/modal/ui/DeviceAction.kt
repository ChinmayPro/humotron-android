package com.humotron.app.domain.modal.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.humotron.app.R

enum class DeviceAction(
    @StringRes val titleRes: Int,
    @DrawableRes val iconRes: Int,
) {
    RESTART(R.string.restart_device_label, R.drawable.ic_restart_alt_24px),
    RESET_FACTORY(R.string.reset_to_factory_settings_label, R.drawable.ic_warning_24px),
    REMOVE_FROM_ACCOUNT(R.string.remove_from_account_label, R.drawable.ic_delete_24px),
    RE_SETUP(R.string.resetup_device_label, R.drawable.ic_autorenew_24px)
}
