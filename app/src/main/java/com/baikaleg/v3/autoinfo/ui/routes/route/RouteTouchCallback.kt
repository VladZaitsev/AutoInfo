package com.baikaleg.v3.autoinfo.ui.routes.route

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import com.baikaleg.v3.autoinfo.R
import com.baikaleg.v3.autoinfo.ui.routes.RouteViewModel


class RouteTouchCallback constructor(context: Context, private val viewmodel: RouteViewModel) : ItemTouchHelper.Callback() {
    private val background: Drawable
    private val deleteIcon: Drawable?
    private val iconMargin: Int
    private val itemBackgroundColor: Int

    init {
        background = ColorDrawable()
        deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete)
        deleteIcon?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        iconMargin = context.resources.getDimension(R.dimen.general_margin).toInt()
        itemBackgroundColor = context.resources.getColor(R.color.colorAccent)
    }
    override fun isItemViewSwipeEnabled(): Boolean {
        return true
    }

    override fun getMovementFlags(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?): Int {
        val swipeFlags = ItemTouchHelper.START
        return ItemTouchHelper.Callback.makeMovementFlags(0, swipeFlags)
    }

    override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        viewmodel.onRemove(viewHolder.layoutPosition)
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
}