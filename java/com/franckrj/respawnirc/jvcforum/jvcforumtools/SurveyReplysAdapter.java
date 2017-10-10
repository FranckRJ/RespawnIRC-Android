package com.franckrj.respawnirc.jvcforum.jvcforumtools;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.franckrj.respawnirc.R;

import java.util.ArrayList;

public class SurveyReplysAdapter extends RecyclerView.Adapter<SurveyReplysAdapter.ReplyViewHolder> {
    private Activity parentActivity = null;
    private ArrayList<String> listOfReplyContent = new ArrayList<>();
    private LayoutInflater serviceInflater = null;

    public SurveyReplysAdapter(Activity newParentActivity) {
        parentActivity = newParentActivity;
        serviceInflater = (LayoutInflater) parentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

    @Override
    public ReplyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ReplyViewHolder(serviceInflater.inflate(R.layout.reply_survey_row, parent, false));
    }

    @Override
    public void onBindViewHolder(ReplyViewHolder holder, int position) {
        holder.setCurrentReply(listOfReplyContent.get(position), position);
    }

    @Override
    public int getItemCount() {
        return listOfReplyContent.size();
    }

    public class ReplyViewHolder extends RecyclerView.ViewHolder {
        private TextView replyTitle = null;
        private EditText replyContent = null;
        private int replyPos = -1;

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
                listOfReplyContent.set(replyPos, newText.toString());
            }
        };

        public ReplyViewHolder(View mainView) {
            super(mainView);
            replyTitle = mainView.findViewById(R.id.reply_title_replysurveyrow);
            replyContent = mainView.findViewById(R.id.reply_content_replysurveyrow);
            replyContent.addTextChangedListener(replyContentChanged);
        }

        public void setCurrentReply(String content, int newPos) {
            replyPos = newPos;
            replyTitle.setText(parentActivity.getString(R.string.replyTitleInSurveyManager, String.valueOf(replyPos + 1)));
            replyContent.setText(content);
        }
    }
}
