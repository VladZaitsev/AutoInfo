package com.baikaleg.v3.autoinfo.ui.routes.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.DialogFragment
import com.baikaleg.v3.autoinfo.R
import com.baikaleg.v3.autoinfo.data.model.Route
import com.baikaleg.v3.autoinfo.databinding.DialogAddEditRouteBinding

const val ARG_ROUTE = "route"

class AddEditRouteDialog : DialogFragment() {

    companion object {
        fun getInstance(route: Route?): AddEditRouteDialog {
            return AddEditRouteDialog().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_ROUTE, route)
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val viewModel = ViewModelProviders.of(this).get(AddEditRouteViewModel::class.java)
        val route: Route? = arguments?.getParcelable(ARG_ROUTE)

        activity?.let {
            val dialog = AlertDialog.Builder(it)
            val binding = DataBindingUtil.inflate<DialogAddEditRouteBinding>(it.layoutInflater, R.layout.dialog_add_edit_route, null, false)
            binding.setLifecycleOwner(this@AddEditRouteDialog)
            binding.viewmodel = viewModel
            if (route != null) {
                viewModel.setRoute(route)
            }

            dialog.setView(binding.root)
                    .setPositiveButton(getString(R.string.save)) { _, _ ->
                        viewModel.saveRoute()
                        dismiss()
                    }
                    .setNegativeButton(getString(R.string.cancel)) { _, _ -> dismiss() }
                    .create()
            return dialog.create()
        }
        return super.onCreateDialog(savedInstanceState)
    }
}
