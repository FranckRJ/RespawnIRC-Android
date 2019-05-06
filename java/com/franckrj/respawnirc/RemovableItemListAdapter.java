package com.franckrj.respawnirc;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RemovableItemListAdapter extends RecyclerView.Adapter<RemovableItemListAdapter.ItemViewHolder> {
    private LayoutInflater serviceInflater;
    private ArrayList<String> listOfItemText = new ArrayList<>();
    private OnItemRemovedListener listenerForItemRemoved = null;

    private final View.OnClickListener removeButtonClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getTag() != null && view.getTag() instanceof Integer) {
                int position = (Integer) view.getTag();
                if (position != -1) {
                    removeItem(position);
                }
            }
        }
    };

    public RemovableItemListAdapter(Activity parentActivity) {
        serviceInflater = (LayoutInflater) parentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setOnItemRemovedListener(OnItemRemovedListener newListenerForItemRemoved) {
        listenerForItemRemoved = newListenerForItemRemoved;
    }

    public void setListOfItemText(ArrayList<String> newList) {
        listOfItemText = newList;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position < listOfItemText.size()) {
            String itemTextRemoved = listOfItemText.get(position);

            listOfItemText.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, listOfItemText.size() - position);

            if (listenerForItemRemoved != null) {
                listenerForItemRemoved.onItemRemoved(itemTextRemoved);
            }
        }
    }

    public boolean listIsEmpty() {
        return listOfItemText.isEmpty();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemViewHolder(serviceInflater.inflate(R.layout.removableitemlist_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        holder.setCurrentItemText(listOfItemText.get(position), position);
    }

    @Override
    public int getItemCount() {
        return listOfItemText.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        private TextView mainText;
        private ImageButton actionButton;

        public ItemViewHolder(View mainView) {
            super(mainView);
            mainText = mainView.findViewById(R.id.text_main_removableitemlist_row);
            actionButton = mainView.findViewById(R.id.image_button_action_removableitemlist_row);

            actionButton.setTag(-1);
            actionButton.setOnClickListener(removeButtonClickedListener);
        }

        public void setCurrentItemText(String newItemText, int newPosition) {
            mainText.setText(newItemText);
            actionButton.setTag(newPosition);
        }
    }

    public interface OnItemRemovedListener {
        void onItemRemoved(String itemText);
    }
}
