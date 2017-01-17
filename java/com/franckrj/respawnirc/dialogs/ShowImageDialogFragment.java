package com.franckrj.respawnirc.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.utils.ImageDownloader;
import com.franckrj.respawnirc.utils.Undeprecator;

public class ShowImageDialogFragment extends DialogFragment {
    public static final String ARG_IMAGE_LINK = "com.franckrj.respawnirc.showimagedialogfragment.ARG_IMAGE_LINK";

    private ImageView viewForImage = null;
    private ProgressBar progressBarForImage = null;
    private ImageDownloader downloaderForImage = new ImageDownloader();
    private String linkOfImage = "";
    private Drawable fullsizeImage = null;

    private ImageDownloader.DownloadFinished listenerForDownloadFinished = new ImageDownloader.DownloadFinished() {
        @Override
        public void newDownloadFinished(int numberOfDownloadRemaining) {
            updateViewForImage();
        }
    };

    private void updateViewForImage() {
        viewForImage.setVisibility(View.VISIBLE);
        progressBarForImage.setVisibility(View.GONE);
        viewForImage.setImageDrawable(fullsizeImage);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle currentArgs = getArguments();

        if (currentArgs != null) {
            linkOfImage = currentArgs.getString(ARG_IMAGE_LINK, "");
        }

        if (linkOfImage.isEmpty()) {
            dismiss();
        } else {
            Drawable deletedDrawable;
            Resources res = getActivity().getResources();
            DisplayMetrics metrics = new DisplayMetrics();

            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

            deletedDrawable = Undeprecator.resourcesGetDrawable(res, R.drawable.image_deleted);
            deletedDrawable.setBounds(0, 0, deletedDrawable.getIntrinsicWidth(), deletedDrawable.getIntrinsicHeight());

            downloaderForImage.setParentActivity(getActivity());
            downloaderForImage.setListenerForDownloadFinished(listenerForDownloadFinished);
            downloaderForImage.setImagesCacheDir(getActivity().getCacheDir());
            downloaderForImage.setScaleLargeImages(true);
            downloaderForImage.setImagesSize(metrics.widthPixels, metrics.heightPixels);
            downloaderForImage.setDefaultDrawable(deletedDrawable);
            downloaderForImage.setDeletedDrawable(deletedDrawable);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View mainView = inflater.inflate(R.layout.dialog_showimage, container, false);
        viewForImage = (ImageView) mainView.findViewById(R.id.imageview_image_showimage);
        progressBarForImage = (ProgressBar) mainView.findViewById(R.id.downloading_image_showimage);

        progressBarForImage.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        viewForImage.setVisibility(View.GONE);

        fullsizeImage = downloaderForImage.getDrawableFromLink(linkOfImage);
        if (downloaderForImage.getNumberOfFilesDownloading() == 0) {
            updateViewForImage();
        }

        mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return mainView;
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        super.onDismiss(dialogInterface);
        downloaderForImage.stopAllCurrentTasks();
    }
}
