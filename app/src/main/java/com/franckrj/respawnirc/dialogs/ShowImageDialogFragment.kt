package com.franckrj.respawnirc.dialogs

import android.app.Dialog
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.franckrj.respawnirc.R

class ShowImageDialogFragment : DialogFragment() {
    companion object {
        const val ARG_IMAGE_LINK = "com.franckrj.respawnirc.showimagedialogfragment.ARG_IMAGE_LINK"
    }

    lateinit var viewForImage: ImageView
    lateinit var indeterminateProgressBar: ProgressBar

    val imageDownloadRequestListener = object : RequestListener<Drawable> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean {
            indeterminateProgressBar.visibility = View.GONE
            viewForImage.visibility = View.VISIBLE
            return false
        }

        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: Target<Drawable>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            indeterminateProgressBar.visibility = View.GONE
            viewForImage.visibility = View.VISIBLE
            return false
        }
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
        var linkIsValid = false
        val mainView = inflater.inflate(R.layout.dialog_showimage, container, false)

        viewForImage = mainView.findViewById(R.id.imageview_image_showimage)!!
        indeterminateProgressBar = mainView.findViewById(R.id.dl_indeterminate_image_showimage)!!

        viewForImage.visibility = View.INVISIBLE
        indeterminateProgressBar.indeterminateDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)

        if (currentArgs != null) {
            val linkOfImage: String = currentArgs.getString(ARG_IMAGE_LINK, "")

            if (linkOfImage.isNotEmpty()) {
                linkIsValid = true

                Glide.with(this)
                    .load(linkOfImage)
                    .listener(imageDownloadRequestListener)
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
