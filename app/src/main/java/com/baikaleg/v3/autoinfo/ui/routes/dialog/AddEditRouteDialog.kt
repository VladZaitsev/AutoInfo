package com.baikaleg.v3.autoinfo.ui.routes.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.app.DialogFragment
import com.baikaleg.v3.autoinfo.R
import com.baikaleg.v3.autoinfo.data.model.Route
import com.baikaleg.v3.autoinfo.databinding.DialogAddEditRouteBinding

const val ARG_ROUTE = "route"

class AddEditRouteDialog : DialogFragment() {
    private lateinit var viewmodel: AddEditRouteViewModel

    companion object {
        fun getInstance(route: Route?): AddEditRouteDialog {
            return AddEditRouteDialog().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_ROUTE, route)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewmodel = ViewModelProviders.of(this).get(AddEditRouteViewModel::class.java)
        val route: Route? = arguments?.getParcelable(ARG_ROUTE)
        if (route != null) {
            viewmodel.setRoute(route)
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DataBindingUtil.inflate<DialogAddEditRouteBinding>(layoutInflater, R.layout.dialog_add_edit_route, null, false)
        binding.viewmodel = viewmodel
        binding.setLifecycleOwner(this)

        return AlertDialog.Builder(activity)
                .setView(binding.root)
                .setPositiveButton(getString(R.string.save)) { _, _ ->
                    viewmodel.saveRoute()
                    dismiss()
                }
                .setNegativeButton(getString(R.string.cancel)) { _, _ -> dismiss() }
                .create()
    }
}
