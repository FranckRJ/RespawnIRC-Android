package com.franckrj.respawnirc.jvcforum.jvcforumtools;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.franckrj.respawnirc.R;

import java.util.ArrayList;

public class SurveyReplysAdapter extends RecyclerView.Adapter<SurveyReplysAdapter.ReplyViewHolder> {
    private ArrayList<String> listOfReplyContent = new ArrayList<>();
    private LayoutInflater serviceInflater;
    /* Ces deux variables sont une tentative d'optimisation probablement inutile. */
    private String replyTitleModel;
    private String replyContentHintModel;

    private final View.OnClickListener removeButtonClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getTag() != null && view.getTag() instanceof Integer) {
                int position = (Integer) view.getTag();
                if (position >= 0 && position < listOfReplyContent.size()) {
                    if (listOfReplyContent.size() > 2) {
                        listOfReplyContent.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, listOfReplyContent.size() - position);
                    } else {
                        listOfReplyContent.set(position, "");
                        notifyItemChanged(position);
                    }
                }
            }
        }
    };

    public SurveyReplysAdapter(Activity parentActivity) {
        serviceInflater = (LayoutInflater) parentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        replyTitleModel = parentActivity.getString(R.string.replyTitleInSurveyManager);
        replyContentHintModel = parentActivity.getString(R.string.replyTitleInSurveyHint);
    }

    public void setReplys(ArrayList<String> newListOfReplyContent) {
        listOfReplyContent = newListOfReplyContent;
        notifyDataSetChanged();
    }

    public void addReply(String replyContent) {
        listOfReplyContent.add(replyContent);
        notifyItemInserted(listOfReplyContent.size() - 1);
    }

    public ArrayList<String> getReplyContentList() {
        return listOfReplyContent;
    }

    @NonNull
    @Override
    public ReplyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ReplyViewHolder(serviceInflater.inflate(R.layout.reply_survey_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ReplyViewHolder holder, int position) {
        holder.setCurrentReply(listOfReplyContent.get(position), position);
    }

    @Override
    public int getItemCount() {
        return listOfReplyContent.size();
    }

    public class ReplyViewHolder extends RecyclerView.ViewHolder {
        private TextView replyTitle;
        private EditText replyContent;
        private ImageButton actionButton;
        private int replyPos = -1;
        private boolean saveReplyChanges = false;

        private final TextWatcher replyContentChanged = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //rien
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //rien
            }

            @Override
            public void afterTextChanged(Editable newText) {
                if (saveReplyChanges && replyPos >= 0 && replyPos < listOfReplyContent.size()) {
                    listOfReplyContent.set(replyPos, newText.toString());
                }
            }
        };

        public ReplyViewHolder(View mainView) {
            super(mainView);
            replyTitle = mainView.findViewById(R.id.reply_title_replysurveyrow);
            replyContent = mainView.findViewById(R.id.reply_content_replysurveyrow);
            actionButton = mainView.findViewById(R.id.image_button_action_replysurveyrow);

            replyContent.addTextChangedListener(replyContentChanged);
            actionButton.setTag(-1);
            actionButton.setOnClickListener(removeButtonClickedListener);
        }

        public void setCurrentReply(String content, int newPos) {
            replyPos = newPos;
            replyTitle.setText(replyTitleModel.replace("%n%", String.valueOf(replyPos + 1)));
            actionButton.setTag(newPos);

            saveReplyChanges = false;
            replyContent.setHint(replyContentHintModel.replace("%n%", String.valueOf(replyPos + 1)));
            replyContent.setText(content);
            saveReplyChanges = true;
        }
    }
}
