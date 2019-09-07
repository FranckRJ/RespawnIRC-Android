package com.franckrj.respawnirc.dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.request.RequestOptions
import com.franckrj.respawnirc.R
import com.franckrj.respawnirc.utils.GlideProgressImageLoader
import com.franckrj.respawnirc.utils.Undeprecator

class ShowImageDialogFragment : DialogFragment() {
    companion object {
        const val ARG_IMAGE_LINK = "com.franckrj.respawnirc.showimagedialogfragment.ARG_IMAGE_LINK"
    }

    private lateinit var imageLoader: GlideProgressImageLoader

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
        var linkIsValid = false
        val mainView: View = inflater.inflate(R.layout.dialog_showimage, container, false)

        val viewForImage: ImageView = mainView.findViewById(R.id.imageview_image_showimage)!!
        val indeterminateProgressBar: ProgressBar = mainView.findViewById(R.id.dl_indeterminate_image_showimage)!!
        val determinateProgressBar: ProgressBar = mainView.findViewById(R.id.dl_determinate_image_showimage)!!
        val textForSizeOfImage: TextView = mainView.findViewById(R.id.text_size_image_showimage)!!

        viewForImage.visibility = View.INVISIBLE
        Undeprecator.drawableSetColorFilter(
            indeterminateProgressBar.indeterminateDrawable,
            Color.WHITE,
            PorterDuff.Mode.SRC_IN
        )
        Undeprecator.drawableSetColorFilter(
            determinateProgressBar.progressDrawable,
            Color.WHITE,
            PorterDuff.Mode.SRC_IN
        )

        imageLoader = GlideProgressImageLoader(
            this,
            viewForImage,
            indeterminateProgressBar,
            determinateProgressBar,
            textForSizeOfImage
        )

        if (currentArgs != null) {
            val linkOfImage: String = currentArgs.getString(ARG_IMAGE_LINK, "")

            if (linkOfImage.isNotEmpty()) {
                val optionsOfImageLoader = RequestOptions()
                    .error(R.drawable.image_deleted_dark)
                    .fitCenter()

                linkIsValid = true

                imageLoader.startNewLoad(linkOfImage, optionsOfImageLoader)
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
        imageLoader.clearCurrentUrl()
        dismiss()
        super.onPause()
    }
}
