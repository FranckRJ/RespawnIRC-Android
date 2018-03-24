package com.franckrj.respawnirc.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.utils.ImageDownloader;
import com.franckrj.respawnirc.utils.ThemeManager;

public class ShowImageDialogFragment extends DialogFragment {
    public static final String ARG_IMAGE_LINK = "com.franckrj.respawnirc.showimagedialogfragment.ARG_IMAGE_LINK";

    private ImageView viewForImage = null;
    private ProgressBar progressBarIndeterminateForImage = null;
    private ProgressBar progressBarDeterminateForImage = null;
    private TextView textForSizeOfImage = null;
    private ImageDownloader downloaderForImage = new ImageDownloader();
    private String linkOfImage = "";
    private Drawable fullsizeImage = null;

    private final ImageDownloader.DownloadFinished listenerForDownloadFinished = new ImageDownloader.DownloadFinished() {
        @Override
        public void newDownloadFinished(int numberOfDownloadRemaining) {
            updateViewForImage();
        }
    };

    private final ImageDownloader.CurrentProgress listenerForCurrentProgress = new ImageDownloader.CurrentProgress() {
        @Override
        public void newCurrentProgress(int progressInPercent, int sizeOfFile, String fileLink) {
            if (linkOfImage.equals(fileLink)) {
                if (progressBarIndeterminateForImage.getVisibility() == View.VISIBLE) {
                    float formattedSizeOfFile = sizeOfFile / 1024;

                    if (formattedSizeOfFile >= 1000) {
                        formattedSizeOfFile = formattedSizeOfFile / 1024;
                        textForSizeOfImage.setText(getString(R.string.megaByteNumber, formattedSizeOfFile));
                    } else {
                        textForSizeOfImage.setText(getString(R.string.kiloByteNumber, formattedSizeOfFile));
                    }

                    progressBarIndeterminateForImage.setVisibility(View.INVISIBLE);
                    progressBarDeterminateForImage.setVisibility(View.VISIBLE);
                    textForSizeOfImage.setVisibility(View.VISIBLE);
                }
                progressBarDeterminateForImage.setProgress(progressInPercent);
            }
        }
    };

    private void updateViewForImage() {
        viewForImage.setVisibility(View.VISIBLE);
        progressBarIndeterminateForImage.setVisibility(View.INVISIBLE);
        progressBarDeterminateForImage.setVisibility(View.INVISIBLE);
        textForSizeOfImage.setVisibility(View.INVISIBLE);
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
            DisplayMetrics metrics = new DisplayMetrics();

            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

            deletedDrawable = ThemeManager.getDrawable(R.attr.themedDeletedImage, getActivity());
            deletedDrawable.setBounds(0, 0, deletedDrawable.getIntrinsicWidth(), deletedDrawable.getIntrinsicHeight());

            downloaderForImage.setParentActivity(getActivity());
            downloaderForImage.setListenerForDownloadFinished(listenerForDownloadFinished);
            downloaderForImage.setListenerForCurrentProgress(listenerForCurrentProgress);
            downloaderForImage.setImagesCacheDir(getActivity().getCacheDir());
            downloaderForImage.setScaleLargeImages(true);
            downloaderForImage.setDefaultDrawable(deletedDrawable);
            downloaderForImage.setDeletedDrawable(deletedDrawable);
            downloaderForImage.setImagesSize(metrics.widthPixels, metrics.heightPixels, false);
        }
    }

    @NonNull
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View mainView = inflater.inflate(R.layout.dialog_showimage, container, false);
        viewForImage = mainView.findViewById(R.id.imageview_image_showimage);
        progressBarIndeterminateForImage = mainView.findViewById(R.id.dl_indeterminate_image_showimage);
        progressBarDeterminateForImage = mainView.findViewById(R.id.dl_determinate_image_showimage);
        textForSizeOfImage = mainView.findViewById(R.id.text_size_image_showimage);

        /*nécessaire pour un affichage correcte sur les versions récentes d'android.*/
        progressBarIndeterminateForImage.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        progressBarDeterminateForImage.getProgressDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        progressBarDeterminateForImage.setVisibility(View.INVISIBLE);
        textForSizeOfImage.setVisibility(View.INVISIBLE);
        viewForImage.setVisibility(View.INVISIBLE);

        fullsizeImage = downloaderForImage.getDrawableFromLink(linkOfImage);
        if (downloaderForImage.getNumberOfFilesDownloading() == 0) {
            updateViewForImage();
        }

        mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: pas sur que ce soit une bonne idée, ne pas appliquer ailleurs avant plus de tests
                dismissAllowingStateLoss();
            }
        });

        return mainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    public void onPause() {
        downloaderForImage.stopAllCurrentTasks();
        super.onPause();
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        downloaderForImage.stopAllCurrentTasks();
        super.onDismiss(dialogInterface);
    }
}
