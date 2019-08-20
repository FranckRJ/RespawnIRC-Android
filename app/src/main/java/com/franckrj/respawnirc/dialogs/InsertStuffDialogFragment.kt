package com.franckrj.respawnirc.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ScrollView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.franckrj.respawnirc.R
import com.franckrj.respawnirc.utils.PrefsManager
import com.franckrj.respawnirc.utils.ThemeManager

class InsertStuffDialogFragment : DialogFragment() {
    private val listOfCategoryButtons = mutableListOf<ImageView>()
    private var oldRowNumber: Int = 1

    private lateinit var scrollViewOfButtons: ScrollView

    private val categoryButtonClickedListener = View.OnClickListener { view ->
        var newRowNumber: Int = (view.tag as? Int) ?: 0 // est-ce réellement une bonne idée de cacher l'exception ?

        newRowNumber = newRowNumber.coerceIn(0, listOfCategoryButtons.size - 1)

        selectThisRow(newRowNumber)
    }

    private fun selectThisRow(rowToUse: Int) {
        listOfCategoryButtons[oldRowNumber].setBackgroundColor(Color.TRANSPARENT)
        listOfCategoryButtons[rowToUse].setBackgroundColor(
            ThemeManager.getColorInt(
                R.attr.themedDarkerPopupBackgroundColor,
                requireActivity()
            )
        )
        /*initializeSpanForTextViewIfNeeded(jvcImageGetter, rowToUse)
        mainTextView.setText(replaceUrlSpans(listOfSpanForTextView[rowToUse]))*/
        scrollViewOfButtons.requestChildFocus(listOfCategoryButtons[rowToUse], listOfCategoryButtons[rowToUse])
        oldRowNumber = rowToUse
        PrefsManager.putInt(PrefsManager.IntPref.Names.LAST_ROW_SELECTED_INSERTSTUFF, oldRowNumber)
        PrefsManager.applyChanges()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        @SuppressLint("InflateParams")
        val mainView = requireActivity().layoutInflater.inflate(R.layout.dialog_insertstuff, null)

        scrollViewOfButtons = mainView.findViewById(R.id.list_scrollview_insertstuff)

        listOfCategoryButtons.add(mainView.findViewById(R.id.smiley_button_insertstuff))
        listOfCategoryButtons.add(mainView.findViewById(R.id.textformat_button_insertstuff))
        listOfCategoryButtons.add(mainView.findViewById(R.id.sticker_1_button_insertstuff))
        listOfCategoryButtons.add(mainView.findViewById(R.id.sticker_2_button_insertstuff))
        listOfCategoryButtons.add(mainView.findViewById(R.id.sticker_3_button_insertstuff))
        listOfCategoryButtons.add(mainView.findViewById(R.id.sticker_4_button_insertstuff))
        listOfCategoryButtons.add(mainView.findViewById(R.id.sticker_5_button_insertstuff))
        listOfCategoryButtons.add(mainView.findViewById(R.id.sticker_6_button_insertstuff))
        listOfCategoryButtons.add(mainView.findViewById(R.id.sticker_7_button_insertstuff))
        listOfCategoryButtons.add(mainView.findViewById(R.id.sticker_8_button_insertstuff))
        listOfCategoryButtons.add(mainView.findViewById(R.id.sticker_9_button_insertstuff))
        listOfCategoryButtons.add(mainView.findViewById(R.id.sticker_10_button_insertstuff))
        listOfCategoryButtons.add(mainView.findViewById(R.id.sticker_11_button_insertstuff))
        listOfCategoryButtons.add(mainView.findViewById(R.id.sticker_12_button_insertstuff))
        listOfCategoryButtons.add(mainView.findViewById(R.id.sticker_13_button_insertstuff))
        listOfCategoryButtons.add(mainView.findViewById(R.id.sticker_14_button_insertstuff))
        listOfCategoryButtons.add(mainView.findViewById(R.id.sticker_15_button_insertstuff))

        for (i in listOfCategoryButtons.indices) {
            listOfCategoryButtons[i].tag = i
            listOfCategoryButtons[i].setOnClickListener(categoryButtonClickedListener)
        }

        if (PrefsManager.getBool(PrefsManager.BoolPref.Names.SAVE_LAST_ROW_USED_INSERTSTUFF)) {
            oldRowNumber = PrefsManager.getInt(PrefsManager.IntPref.Names.LAST_ROW_SELECTED_INSERTSTUFF)
        }

        oldRowNumber = oldRowNumber.coerceIn(0, listOfCategoryButtons.size - 1)

        selectThisRow(oldRowNumber)

        builder.setTitle(R.string.insertStuff).setView(mainView)
            .setNegativeButton(R.string.cancel) { dialog, id -> dialog.dismiss() }

        return builder.create()
    }

    interface StuffInserted {
        fun getStringInserted(newStringToAdd: String, posOfCenterFromEnd: Int)
    }
}
