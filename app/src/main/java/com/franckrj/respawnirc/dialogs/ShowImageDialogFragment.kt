package com.franckrj.respawnirc.dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.franckrj.respawnirc.R

class ShowImageDialogFragment : DialogFragment() {
    companion object {
        const val ARG_IMAGE_LINK = "com.franckrj.respawnirc.showimagedialogfragment.ARG_IMAGE_LINK"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog: Dialog = super.onCreateDialog(savedInstanceState)
        val dialogWindow: Window? = dialog.window

        if (dialogWindow != null) {
            dialogWindow.requestFeature(Window.FEATURE_NO_TITLE)
            dialogWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val currentArgs: Bundle? = arguments
        val mainView = inflater.inflate(R.layout.dialog_showimage, container, false)
        val viewForImage: ImageView = mainView.findViewById(R.id.imageview_image_showimage)!!
        var linkIsValid = false

        if (currentArgs != null) {
            val linkOfImage: String = currentArgs.getString(ARG_IMAGE_LINK, "")

            if (linkOfImage.isNotEmpty()) {
                linkIsValid = true

                Glide.with(this)
                    .load(linkOfImage)
                    .placeholder(R.drawable.image_download_dark)
                    .error(R.drawable.image_deleted_dark)
                    .fitCenter()
                    .into(viewForImage)
            }
        }

        if (!linkIsValid) {
            dismiss()
        }

        mainView.setOnClickListener {
            if (!isStateSaved) {
                dismiss()
            }
        }

        return mainView
    }

    override fun onStart() {
        super.onStart()

        /* Bizarrement MATCH_PARENT n'est nécessaire que pour le width, pour le height pas besoin et en plus si le height
         * est set à MATCH_PARENT la statusbar bug est devient toute noire. */
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onPause() {
        dismiss()
        super.onPause()
    }
}
