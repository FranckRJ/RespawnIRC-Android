package com.franckrj.respawnirc.dialogs

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.franckrj.respawnirc.R
import com.franckrj.respawnirc.databinding.DialogInsertstuffBinding
import com.franckrj.respawnirc.utils.PrefsManager
import com.franckrj.respawnirc.utils.ThemeManager

class InsertStuffDialogFragment : DialogFragment() {
    private val listOfCategoryButtons = mutableListOf<ImageView>()
    private var oldRowNumber: Int = 1

    private lateinit var bindings: DialogInsertstuffBinding

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
        bindings.listScrollviewInsertstuff.requestChildFocus(
            listOfCategoryButtons[rowToUse],
            listOfCategoryButtons[rowToUse]
        )
        oldRowNumber = rowToUse
        PrefsManager.putInt(PrefsManager.IntPref.Names.LAST_ROW_SELECTED_INSERTSTUFF, oldRowNumber)
        PrefsManager.applyChanges()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())

        bindings = DialogInsertstuffBinding.inflate(requireActivity().layoutInflater)
        bindings.dialog = this

        listOfCategoryButtons.add(bindings.smileyButtonInsertstuff)
        listOfCategoryButtons.add(bindings.textformatButtonInsertstuff)
        listOfCategoryButtons.add(bindings.sticker1ButtonInsertstuff)
        listOfCategoryButtons.add(bindings.sticker2ButtonInsertstuff)
        listOfCategoryButtons.add(bindings.sticker3ButtonInsertstuff)
        listOfCategoryButtons.add(bindings.sticker4ButtonInsertstuff)
        listOfCategoryButtons.add(bindings.sticker5ButtonInsertstuff)
        listOfCategoryButtons.add(bindings.sticker6ButtonInsertstuff)
        listOfCategoryButtons.add(bindings.sticker7ButtonInsertstuff)
        listOfCategoryButtons.add(bindings.sticker8ButtonInsertstuff)
        listOfCategoryButtons.add(bindings.sticker9ButtonInsertstuff)
        listOfCategoryButtons.add(bindings.sticker10ButtonInsertstuff)
        listOfCategoryButtons.add(bindings.sticker11ButtonInsertstuff)
        listOfCategoryButtons.add(bindings.sticker12ButtonInsertstuff)
        listOfCategoryButtons.add(bindings.sticker13ButtonInsertstuff)
        listOfCategoryButtons.add(bindings.sticker14ButtonInsertstuff)
        listOfCategoryButtons.add(bindings.sticker15ButtonInsertstuff)

        for (i in listOfCategoryButtons.indices) {
            listOfCategoryButtons[i].tag = i
            listOfCategoryButtons[i].setOnClickListener(categoryButtonClickedListener)
        }

        if (PrefsManager.getBool(PrefsManager.BoolPref.Names.SAVE_LAST_ROW_USED_INSERTSTUFF)) {
            oldRowNumber = PrefsManager.getInt(PrefsManager.IntPref.Names.LAST_ROW_SELECTED_INSERTSTUFF)
        }

        oldRowNumber = oldRowNumber.coerceIn(0, listOfCategoryButtons.size - 1)

        selectThisRow(oldRowNumber)

        builder.setTitle(R.string.insertStuff).setView(bindings.root)
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }

        return builder.create()
    }

    interface StuffInserted {
        fun getStringInserted(newStringToAdd: String, posOfCenterFromEnd: Int)
    }
}
