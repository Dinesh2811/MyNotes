package com.dinesh.mynotes.app

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.dinesh.mynotes.R


class FancyDialog(context: Context) : Dialog(context) {

    private var title: String? = null
    private var message: String? = null
    private var positiveButtonText: String? = null
    private var negativeButtonText: String? = null
    private var positiveButtonClickListener: DialogInterface.OnClickListener? = null
    private var negativeButtonClickListener: DialogInterface.OnClickListener? = null

    init {
        setContentView(R.layout.fancy_dialog)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }

    fun setTitle(title: String): FancyDialog {
        this.title = title
        return this
    }

    fun setMessage(message: String): FancyDialog {
        this.message = message
        return this
    }

    fun setPositiveButton(positiveButtonText: String, listener: DialogInterface.OnClickListener): FancyDialog {
        this.positiveButtonText = positiveButtonText
        this.positiveButtonClickListener = listener
        return this
    }

    fun setNegativeButton(negativeButtonText: String, listener: DialogInterface.OnClickListener): FancyDialog {
        this.negativeButtonText = negativeButtonText
        this.negativeButtonClickListener = listener
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val titleView = findViewById<TextView>(R.id.title)
        val messageView = findViewById<TextView>(R.id.message)
        val positiveButton = findViewById<Button>(R.id.positive_button)
        val negativeButton = findViewById<Button>(R.id.negative_button)

        titleView.text = title
        messageView.text = message

        if (positiveButtonText != null) {
            positiveButton.text = positiveButtonText
            if (positiveButtonClickListener != null) {
                positiveButton.setOnClickListener { positiveButtonClickListener!!.onClick(this@FancyDialog, DialogInterface.BUTTON_POSITIVE) }
            }
        } else {
            positiveButton.visibility = View.GONE
        }

        if (negativeButtonText != null) {
            negativeButton.text = negativeButtonText
            if (negativeButtonClickListener != null) {
                negativeButton.setOnClickListener { negativeButtonClickListener!!.onClick(this@FancyDialog, DialogInterface.BUTTON_NEGATIVE) }
            }
        } else {
            negativeButton.visibility = View.GONE
        }
    }
}

/*

        val dialog = FancyDialog(this)
            .setTitle("Title")
            .setMessage("Message")
            .setPositiveButton("OK", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    // Do something when OK button is clicked
                }
            })
            .setNegativeButton("Cancel", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    // Do something when Cancel button is clicked
                }
            })
        dialog.show()

 */