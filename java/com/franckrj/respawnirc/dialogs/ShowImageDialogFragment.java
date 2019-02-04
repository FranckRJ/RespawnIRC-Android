package com.franckrj.respawnirc.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
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
import com.franckrj.respawnirc.utils.Undeprecator;

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
        public void newCurrentProgress(long progressInPercent, long sizeOfFile, String fileLink) {
            if (linkOfImage.equals(fileLink)) {
                if (progressBarIndeterminateForImage.getVisibility() == View.VISIBLE) {
                    double formattedSizeOfFile = sizeOfFile / 1024.;

                    if (formattedSizeOfFile >= 1000) {
                        formattedSizeOfFile = formattedSizeOfFile / 1024.;
                        textForSizeOfImage.setText(getString(R.string.megaByteNumber, formattedSizeOfFile));
                    } else {
                        textForSizeOfImage.setText(getString(R.string.kiloByteNumber, formattedSizeOfFile));
                    }

                    progressBarIndeterminateForImage.setVisibility(View.INVISIBLE);
                    progressBarDeterminateForImage.setVisibility(View.VISIBLE);
                    textForSizeOfImage.setVisibility(View.VISIBLE);
                }
                progressBarDeterminateForImage.setProgress((int)progressInPercent);
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

            requireActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

            deletedDrawable = Undeprecator.resourcesGetDrawable(requireActivity().getResources(), R.drawable.image_deleted_dark);

            downloaderForImage.setParentActivity(requireActivity());
            downloaderForImage.setListenerForDownloadFinished(listenerForDownloadFinished);
            downloaderForImage.setListenerForCurrentProgress(listenerForCurrentProgress);
            downloaderForImage.setImagesCacheDir(requireActivity().getCacheDir());
            downloaderForImage.setOptimisedScale(true);
            downloaderForImage.setUpdateProgress(true);
            downloaderForImage.setDefaultDrawableResized(deletedDrawable);
            downloaderForImage.setDeletedDrawableResized(deletedDrawable);
            downloaderForImage.setImagesSize(metrics.widthPixels, metrics.heightPixels);
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
        progressBarIndeterminateForImage.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        progressBarDeterminateForImage.getProgressDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        progressBarDeterminateForImage.setVisibility(View.INVISIBLE);
        textForSizeOfImage.setVisibility(View.INVISIBLE);
        viewForImage.setVisibility(View.INVISIBLE);

        fullsizeImage = downloaderForImage.getDrawableFromLink(linkOfImage, true, true, false);
        if (downloaderForImage.getNumberOfFilesDownloading() == 0) {
            updateViewForImage();
        }

        mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isStateSaved()) {
                    dismiss();
                }
            }
        });

        return mainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            /* Bizarrement MATCH_PARENT n'est nécessaire que pour le width, pour le height pas besoin et en plus si le height
             * est set à MATCH_PARENT la statusbar bug est devient toute noire. */
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onPause() {
        downloaderForImage.stopAllCurrentTasks();
        downloaderForImage.clearMemoryCache();
        super.onPause();
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        /* La fonction onPause est censé toujours être appelé avant onDismiss donc ça sert a rien,
         * mais dans le doute... */
        downloaderForImage.stopAllCurrentTasks();
        downloaderForImage.clearMemoryCache();
        super.onDismiss(dialogInterface);
    }
}
