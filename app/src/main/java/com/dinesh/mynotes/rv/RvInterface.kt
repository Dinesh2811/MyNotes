package com.dinesh.mynotes.rv

import android.view.View

interface RvInterface {
    fun onItemClick(view: View?, position: Int)
    fun onLongClick(view: View?, position: Int, rvSelectedItemCount: Int)
}
