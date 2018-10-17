package com.baikaleg.v3.autoinfo.ui.stations.station

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Vibrator
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.View
import com.baikaleg.v3.autoinfo.R
import com.baikaleg.v3.autoinfo.ui.stations.AddEditStationModel

open class StationTouchCallback constructor(context: Context, private val callback: AddEditStationModel) : ItemTouchHelper.Callback() {
    private val background: Drawable
    private val deleteIcon: Drawable?
    private val iconMargin: Int
    private val itemBackgroundColor: Int
    private val backgroundColor: Int

    private var orderChanged = false
    private var positionFrom = 0
    private var positionTo = 0

    init {
        background = ColorDrawable()
        deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete)
        deleteIcon?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        iconMargin = context.resources.getDimension(R.dimen.general_margin).toInt()
        itemBackgroundColor = context.resources.getColor(R.color.colorAccent)
        backgroundColor = context.resources.getColor(R.color.colorBackground)
    }

    override fun getMovementFlags(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        val swipeFlags = ItemTouchHelper.START
        return ItemTouchHelper.Callback.makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun isLongPressDragEnabled(): Boolean {
        return true
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return true
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        orderChanged = true
        positionFrom = viewHolder.adapterPosition
        positionTo = target.adapterPosition
        return true
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        if (actionState == ItemTouchHelper.ACTION_STATE_IDLE ) {
            callback.onMoved(positionFrom, positionTo)
            orderChanged = false
            positionFrom = 0
            positionTo = 0
            deleteIcon?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        } else if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            deleteIcon?.setColorFilter(backgroundColor, PorterDuff.Mode.SRC_ATOP)
        }
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        callback.onRemoved(viewHolder.layoutPosition)
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        val view = viewHolder.itemView
        val itemHeight = view.bottom - view.top

        (background as ColorDrawable).color = itemBackgroundColor
        background.setBounds(view.right + dX.toInt(),
                view.top,
                view.right,
                view.bottom)
        background.draw(c)


        val iconTop = view.top + iconMargin
        val iconLeft = view.right - (itemHeight - 2 * iconMargin) - iconMargin
        val iconRight = view.right - iconMargin
        val iconBottom = view.bottom - iconMargin

        deleteIcon!!.setBounds(iconLeft, iconTop, iconRight, iconBottom)
        deleteIcon.draw(c)

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    interface ItemTouchHelperContract {
        fun onMoved(fromPosition: Int, toPosition: Int)
        fun onRemoved(position: Int)
    }
}


