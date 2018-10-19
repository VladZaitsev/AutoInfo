package com.baikaleg.v3.autoinfo.ui.stations.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.DialogFragment
import android.widget.Toast
import com.baikaleg.v3.autoinfo.R
import com.baikaleg.v3.autoinfo.data.exportimport.DATA_PATH
import java.io.File
import java.io.FilenameFilter


class ImportRouteDialog : DialogFragment() {
    private var navigator: ImportRouteNavigator? = null
    private var fileList: Array<String>? = null

    companion object {
        fun getInstance(): ImportRouteDialog {
            return ImportRouteDialog()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        fileList = loadFileList()
        activity?.let { it ->
            val dialog = AlertDialog.Builder(it)
            dialog.setTitle(getString(R.string.chose_file_imported_route))
            dialog.setItems(fileList) { _, which ->
                fileList?.let { navigator?.onImportFileNameGranted(it[which]) }
                dismiss()
            }
            return dialog.create()
        }
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is ImportRouteNavigator) {
            navigator = context
        } else {
            throw RuntimeException(context.toString() + " must implement ImportRouteNavigator")
        }
    }

    override fun onDetach() {
        super.onDetach()
        navigator = null
    }

    interface ImportRouteNavigator {
        fun onImportFileNameGranted(fileName: String)
    }

    private fun loadFileList(): Array<String>? {
        val path = File(Environment.getExternalStorageDirectory(), DATA_PATH)
        try {
            path.mkdirs()
        } catch (e: SecurityException) {
            Toast.makeText(activity, getString(R.string.unale_to_wrote_file), Toast.LENGTH_SHORT).show()
        }
        if (path.exists()) {
            val filter = FilenameFilter { dir, filename ->
                val sel = File(dir, filename)
                filename.contains(".txt") || sel.isDirectory
            }
            return path.list(filter)
        } else {
            return null
        }
    }
}


