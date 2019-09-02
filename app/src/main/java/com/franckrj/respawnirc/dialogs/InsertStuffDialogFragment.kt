package com.franckrj.respawnirc.dialogs

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.franckrj.respawnirc.R
import com.franckrj.respawnirc.databinding.DialogInsertstuffBinding
import com.franckrj.respawnirc.utils.PrefsManager
import com.franckrj.respawnirc.utils.ThemeManager

class InsertStuffDialogFragment : DialogFragment() {
    private val stuffAdapter = StuffInsertableAdapter()
    private val listOfCategoryButtons = mutableListOf<ImageView>()
    private var oldRowNumber: Int = 1

    private lateinit var bindings: DialogInsertstuffBinding

    private fun fillAdapterWithStuff(rowNum: Int) {
        stuffAdapter.listOfStuffId = when(rowNum) {
            0 -> {
                listOf(
                    R.drawable.smiley_1,
                    R.drawable.smiley_2,
                    R.drawable.smiley_3,
                    R.drawable.smiley_4,
                    R.drawable.smiley_5,
                    R.drawable.smiley_6,
                    R.drawable.smiley_7,
                    R.drawable.smiley_8,
                    R.drawable.smiley_9,
                    R.drawable.smiley_10,
                    R.drawable.smiley_11,
                    R.drawable.smiley_12,
                    R.drawable.smiley_13,
                    R.drawable.smiley_14,
                    R.drawable.smiley_15,
                    R.drawable.smiley_16,
                    R.drawable.smiley_17,
                    R.drawable.smiley_18,
                    R.drawable.smiley_19,
                    R.drawable.smiley_20,
                    R.drawable.smiley_21,
                    R.drawable.smiley_22,
                    R.drawable.smiley_23,
                    R.drawable.smiley_24,
                    R.drawable.smiley_25,
                    R.drawable.smiley_26,
                    R.drawable.smiley_27,
                    R.drawable.smiley_28,
                    R.drawable.smiley_29,
                    R.drawable.smiley_30,
                    R.drawable.smiley_31,
                    R.drawable.smiley_32,
                    R.drawable.smiley_33,
                    R.drawable.smiley_34,
                    R.drawable.smiley_35,
                    R.drawable.smiley_36,
                    R.drawable.smiley_37,
                    R.drawable.smiley_38,
                    R.drawable.smiley_39,
                    R.drawable.smiley_40,
                    R.drawable.smiley_41,
                    R.drawable.smiley_42,
                    R.drawable.smiley_43,
                    R.drawable.smiley_44,
                    R.drawable.smiley_45,
                    R.drawable.smiley_46,
                    R.drawable.smiley_47,
                    R.drawable.smiley_48,
                    R.drawable.smiley_49,
                    R.drawable.smiley_50,
                    R.drawable.smiley_51,
                    R.drawable.smiley_52,
                    R.drawable.smiley_53,
                    R.drawable.smiley_54,
                    R.drawable.smiley_55,
                    R.drawable.smiley_56,
                    R.drawable.smiley_57,
                    R.drawable.smiley_58,
                    R.drawable.smiley_59,
                    R.drawable.smiley_60,
                    R.drawable.smiley_61,
                    R.drawable.smiley_62,
                    R.drawable.smiley_63,
                    R.drawable.smiley_64,
                    R.drawable.smiley_65,
                    R.drawable.smiley_66,
                    R.drawable.smiley_67,
                    R.drawable.smiley_68,
                    R.drawable.smiley_69,
                    R.drawable.smiley_70,
                    R.drawable.smiley_71,
                    R.drawable.smiley_cimer,
                    R.drawable.smiley_ddb,
                    R.drawable.smiley_fish,
                    R.drawable.smiley_hapoelparty,
                    R.drawable.smiley_loveyou,
                    R.drawable.smiley_nyu,
                    R.drawable.smiley_objection,
                    R.drawable.smiley_pf,
                    R.drawable.smiley_play,
                    R.drawable.smiley_siffle
                )
            }
            else -> listOf()
        }

        stuffAdapter.notifyDataSetChanged()
    }

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
        fillAdapterWithStuff(rowToUse)
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

        bindings.listContentInsertstuff.layoutManager = GridLayoutManager(requireContext(), 4)
        bindings.listContentInsertstuff.adapter = stuffAdapter

        selectThisRow(oldRowNumber)

        builder.setTitle(R.string.insertStuff).setView(bindings.root)
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }

        return builder.create()
    }

    interface StuffInserted {
        fun getStringInserted(newStringToAdd: String, posOfCenterFromEnd: Int)
    }
}

private class StuffInsertableAdapter : RecyclerView.Adapter<StuffInsertableAdapter.StuffViewHolder>() {
    var listOfStuffId: List<Int> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StuffViewHolder {
        return StuffViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.stuffinsertable_row, parent, false))
    }

    override fun getItemCount(): Int {
        return listOfStuffId.size
    }

    override fun onBindViewHolder(holder: StuffViewHolder, position: Int) {
        if (position in listOfStuffId.indices) {
            holder.bind(listOfStuffId[position])
        }
    }

    class StuffViewHolder(val mainView: View) : RecyclerView.ViewHolder(mainView) {
        fun bind(@DrawableRes resId: Int) {
            mainView.setOnClickListener { /*TODO*/ }
            (mainView as? ImageView)?.setImageResource(resId)
        }
    }
}
