package com.franckrj.respawnirc.dialogs

import android.app.Activity
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
    private val stuffAdapter = StuffInsertableAdapter(::stuffClicked)
    private val listOfCategoryButtons = mutableListOf<ImageView>()
    private var oldRowNumber: Int = 1

    private lateinit var bindings: DialogInsertstuffBinding

    private fun stuffClicked(stuffPosition: Int, isLongClick: Boolean) {
        val parentActivity: Activity = requireActivity()

        if (stuffPosition in stuffAdapter.listOfStuffId.indices) {
            if (parentActivity is StuffInserted) {
                parentActivity.insertThisString(
                    stuffAdapter.listOfStuffId[stuffPosition].imageString,
                    stuffAdapter.listOfStuffId[stuffPosition].posOfCenterOfString
                )
            }
        }

        if (!isLongClick) {
            dismiss()
        }
    }

    private fun fillAdapterWithStuff(rowNum: Int) {
        stuffAdapter.listOfStuffId = when (rowNum) {
            0 -> {
                //smiley
                listOf(
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_1, ":)", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_2, ":question:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_3, ":g)", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_4, ":d)", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_5, ":cd:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_6, ":globe:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_7, ":p)", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_8, ":malade:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_9, ":pacg:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_10, ":pacd:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_11, ":noel:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_12, ":o))", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_13, ":snif2:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_14, ":-(", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_15, ":-((", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_16, ":mac:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_17, ":gba:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_18, ":hap:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_19, ":nah:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_20, ":snif:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_21, ":mort:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_22, ":ouch:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_23, ":-)))", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_24, ":content:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_25, ":nonnon:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_26, ":cool:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_27, ":sleep:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_28, ":doute:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_29, ":hello:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_30, ":honte:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_31, ":-p", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_32, ":lol:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_33, ":non2:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_34, ":monoeil:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_35, ":non:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_36, ":ok:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_37, ":oui:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_38, ":rechercher:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_39, ":rire:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_40, ":-D", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_41, ":rire2:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_42, ":salut:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_43, ":sarcastic:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_44, ":up:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_45, ":(", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_46, ":-)", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_47, ":peur:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_48, ":bye:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_49, ":dpdr:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_50, ":fou:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_51, ":gne:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_52, ":dehors:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_53, ":fier:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_54, ":coeur:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_55, ":rouge:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_56, ":sors:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_57, ":ouch2:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_58, ":merci:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_59, ":svp:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_60, ":ange:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_61, ":diable:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_62, ":gni:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_63, ":spoiler:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_64, ":hs:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_65, ":desole:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_66, ":fete:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_67, ":sournois:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_68, ":hum:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_69, ":bravo:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_70, ":banzai:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_71, ":bave:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_cimer, ":cimer:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_ddb, ":ddb:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_fish, ":fish:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_hapoelparty, ":hapoelparty:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_loveyou, ":loveyou:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_nyu, ":cute:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_objection, ":objection:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_pave, ":pave:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_pf, ":pf:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_play, ":play:", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.smiley_siffle, ":siffle:", -1)
                )
            }
            1 -> {
                //textformat
                if (ThemeManager.currentThemeUseDarkColors()) {
                    listOf(
                        StuffInsertableAdapter.StuffInfos(R.drawable.textformat_bold_dark, "''''''", 3),
                        StuffInsertableAdapter.StuffInfos(R.drawable.textformat_italic_dark, "''''", 2),
                        StuffInsertableAdapter.StuffInfos(R.drawable.textformat_underline_dark, "<u></u>", 3),
                        StuffInsertableAdapter.StuffInfos(R.drawable.textformat_strike_dark, "<s></s>", 3),
                        StuffInsertableAdapter.StuffInfos(R.drawable.textformat_ulist_dark, "* ", -1),
                        StuffInsertableAdapter.StuffInfos(R.drawable.textformat_olist_dark, "# ", -1),
                        StuffInsertableAdapter.StuffInfos(R.drawable.textformat_quote_dark, "> ", -1),
                        StuffInsertableAdapter.StuffInfos(R.drawable.textformat_code_dark, "<code></code>", 6),
                        StuffInsertableAdapter.StuffInfos(R.drawable.textformat_spoil_dark, "<spoil></spoil>", 7)
                    )
                } else {
                    listOf(
                        StuffInsertableAdapter.StuffInfos(R.drawable.textformat_bold_light, "''''''", 3),
                        StuffInsertableAdapter.StuffInfos(R.drawable.textformat_italic_light, "''''", 2),
                        StuffInsertableAdapter.StuffInfos(R.drawable.textformat_underline_light, "<u></u>", 3),
                        StuffInsertableAdapter.StuffInfos(R.drawable.textformat_strike_light, "<s></s>", 3),
                        StuffInsertableAdapter.StuffInfos(R.drawable.textformat_ulist_light, "* ", -1),
                        StuffInsertableAdapter.StuffInfos(R.drawable.textformat_olist_light, "# ", -1),
                        StuffInsertableAdapter.StuffInfos(R.drawable.textformat_quote_light, "> ", -1),
                        StuffInsertableAdapter.StuffInfos(R.drawable.textformat_code_light, "<code></code>", 6),
                        StuffInsertableAdapter.StuffInfos(R.drawable.textformat_spoil_light, "<spoil></spoil>", 7)
                    )
                }
            }
            2 -> {
                //ours
                listOf(
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1f88, "[[sticker:p/1f88]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1f89, "[[sticker:p/1f89]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1f8a, "[[sticker:p/1f8a]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1f8b, "[[sticker:p/1f8b]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1f8c, "[[sticker:p/1f8c]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1f8d, "[[sticker:p/1f8d]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1f8e, "[[sticker:p/1f8e]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1f8f, "[[sticker:p/1f8f]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_zu2, "[[sticker:p/zu2]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_zu6, "[[sticker:p/zu6]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_zu7, "[[sticker:p/zu7]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_zu8, "[[sticker:p/zu8]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_zu9, "[[sticker:p/zu9]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_zua, "[[sticker:p/zua]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_zub, "[[sticker:p/zub]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_zuc, "[[sticker:p/zuc]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_zuc_en, "[[sticker:p/zuc-en]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_zuc_es, "[[sticker:p/zuc-es]]", -1)
                )
            }
            3 -> {
                //bourge
                listOf(
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1jnc, "[[sticker:p/1jnc]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1jnd, "[[sticker:p/1jnd]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1jne, "[[sticker:p/1jne]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1jnf, "[[sticker:p/1jnf]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1jng, "[[sticker:p/1jng]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1jnh, "[[sticker:p/1jnh]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1jni, "[[sticker:p/1jni]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1jnj, "[[sticker:p/1jnj]]", -1)
                )
            }
            4 -> {
                //lama
                listOf(
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1kgu, "[[sticker:p/1kgu]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1kgv, "[[sticker:p/1kgv]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1kgw, "[[sticker:p/1kgw]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1kgx, "[[sticker:p/1kgx]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1kgy, "[[sticker:p/1kgy]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1kgz, "[[sticker:p/1kgz]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1kh0, "[[sticker:p/1kh0]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1kh1, "[[sticker:p/1kh1]]", -1)
                )
            }
            5 -> {
                //hap
                listOf(
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1kkg, "[[sticker:p/1kkg]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1kkh, "[[sticker:p/1kkh]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1kki, "[[sticker:p/1kki]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1kkj, "[[sticker:p/1kkj]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1kkk, "[[sticker:p/1kkk]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1kkl, "[[sticker:p/1kkl]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1kkm, "[[sticker:p/1kkm]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1kkn, "[[sticker:p/1kkn]]", -1)
                )
            }
            6 -> {
                //noel
                listOf(
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1kko, "[[sticker:p/1kko]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1kkp, "[[sticker:p/1kkp]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1kkq, "[[sticker:p/1kkq]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1kkr, "[[sticker:p/1kkr]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1kks, "[[sticker:p/1kks]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1kkt, "[[sticker:p/1kkt]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1kku, "[[sticker:p/1kku]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1kkv, "[[sticker:p/1kkv]]", -1)
                )
            }
            7 -> {
                //chat
                listOf(
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1kky, "[[sticker:p/1kky]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1kkz, "[[sticker:p/1kkz]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1kl0, "[[sticker:p/1kl0]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1kl1, "[[sticker:p/1kl1]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1kl2, "[[sticker:p/1kl2]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1kl3, "[[sticker:p/1kl3]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1kl4, "[[sticker:p/1kl4]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1kl5, "[[sticker:p/1kl5]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1kl6, "[[sticker:p/1kl6]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1kl7, "[[sticker:p/1kl7]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1kl8, "[[sticker:p/1kl8]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1kl9, "[[sticker:p/1kl9]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1kla, "[[sticker:p/1kla]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1klb, "[[sticker:p/1klb]]", -1)
                )
            }
            8 -> {
                //orc
                listOf(
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1lga, "[[sticker:p/1lga]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1lgb, "[[sticker:p/1lgb]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1lgc, "[[sticker:p/1lgc]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1lgd, "[[sticker:p/1lgd]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1lge, "[[sticker:p/1lge]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1lgf, "[[sticker:p/1lgf]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1lgg, "[[sticker:p/1lgg]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1lgh, "[[sticker:p/1lgh]]", -1)
                )
            }
            9 -> {
                //dom
                listOf(
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1ljj, "[[sticker:p/1ljj]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1ljl, "[[sticker:p/1ljl]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1ljm, "[[sticker:p/1ljm]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1ljn, "[[sticker:p/1ljn]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1ljo, "[[sticker:p/1ljo]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1ljp, "[[sticker:p/1ljp]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1ljq, "[[sticker:p/1ljq]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1ljr, "[[sticker:p/1ljr]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1rzs, "[[sticker:p/1rzs]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1rzt, "[[sticker:p/1rzt]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1rzu, "[[sticker:p/1rzu]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1rzv, "[[sticker:p/1rzv]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1rzw, "[[sticker:p/1rzw]]", -1)
                )
            }
            10 -> {
                //aventurier
                listOf(
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1lm9, "[[sticker:p/1lm9]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1lma, "[[sticker:p/1lma]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1lmb, "[[sticker:p/1lmb]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1lmc, "[[sticker:p/1lmc]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1lmd, "[[sticker:p/1lmd]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1lme, "[[sticker:p/1lme]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1lmf, "[[sticker:p/1lmf]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1lmg, "[[sticker:p/1lmg]]", -1)
                )
            }
            11 -> {
                //saumon
                listOf(
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1lmh, "[[sticker:p/1lmh]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1lmi, "[[sticker:p/1lmi]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1lmj, "[[sticker:p/1lmj]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1lmk, "[[sticker:p/1lmk]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1lml, "[[sticker:p/1lml]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1lmm, "[[sticker:p/1lmm]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1lmn, "[[sticker:p/1lmn]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1lmo, "[[sticker:p/1lmo]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1lmp, "[[sticker:p/1lmp]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1mqv, "[[sticker:p/1mqv]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1mqw, "[[sticker:p/1mqw]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1mqx, "[[sticker:p/1mqx]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1mqy, "[[sticker:p/1mqy]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1mqz, "[[sticker:p/1mqz]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1mr0, "[[sticker:p/1mr0]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1mr1, "[[sticker:p/1mr1]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1nu6, "[[sticker:p/1nu6]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1nu7, "[[sticker:p/1nu7]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1nu8, "[[sticker:p/1nu8]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1nu9, "[[sticker:p/1nu9]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1nua, "[[sticker:p/1nua]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1nub, "[[sticker:p/1nub]]", -1)
                )
            }
            12 -> {
                //bureau
                listOf(
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1lt7, "[[sticker:p/1lt7]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1lt8, "[[sticker:p/1lt8]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1lt9, "[[sticker:p/1lt9]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1lta, "[[sticker:p/1lta]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1ltb, "[[sticker:p/1ltb]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1ltc, "[[sticker:p/1ltc]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1ltd, "[[sticker:p/1ltd]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1lte, "[[sticker:p/1lte]]", -1)
                )
            }
            13 -> {
                //foot
                listOf(
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1n1m_de, "[[sticker:p/1n1m-de]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1n1m_es, "[[sticker:p/1n1m-es]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1n1m, "[[sticker:p/1n1m]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1n1m_it, "[[sticker:p/1n1m-it]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1n1n_de, "[[sticker:p/1n1n-de]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1n1n_es, "[[sticker:p/1n1n-es]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1n1n, "[[sticker:p/1n1n]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1n1n_it, "[[sticker:p/1n1n-it]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1n1o_de, "[[sticker:p/1n1o-de]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1n1o_es, "[[sticker:p/1n1o-es]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1n1o, "[[sticker:p/1n1o]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1n1o_it, "[[sticker:p/1n1o-it]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1n1p_de, "[[sticker:p/1n1p-de]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1n1p_es, "[[sticker:p/1n1p-es]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1n1p, "[[sticker:p/1n1p]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1n1p_it, "[[sticker:p/1n1p-it]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1n1q_de, "[[sticker:p/1n1q-de]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1n1q_es, "[[sticker:p/1n1q-es]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1n1q, "[[sticker:p/1n1q]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1n1q_it, "[[sticker:p/1n1q-it]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1n1r_de, "[[sticker:p/1n1r-de]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1n1r_es, "[[sticker:p/1n1r-es]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1n1r, "[[sticker:p/1n1r]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1n1r_it, "[[sticker:p/1n1r-it]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1n1s, "[[sticker:p/1n1s]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1n1t_de, "[[sticker:p/1n1t-de]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1n1t_es, "[[sticker:p/1n1t-es]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1n1t, "[[sticker:p/1n1t]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1n1t_it, "[[sticker:p/1n1t-it]]", -1)
                )
            }
            14 -> {
                //store
                listOf(
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1n2c, "[[sticker:p/1n2c]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1n2d, "[[sticker:p/1n2d]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1n2g, "[[sticker:p/1n2g]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1n2h, "[[sticker:p/1n2h]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1n2i, "[[sticker:p/1n2i]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1n2j, "[[sticker:p/1n2j]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1n2k, "[[sticker:p/1n2k]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1n2l, "[[sticker:p/1n2l]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1n2m, "[[sticker:p/1n2m]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1n2n, "[[sticker:p/1n2n]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1n2o, "[[sticker:p/1n2o]]", -1)
                )
            }
            15 -> {
                //pixel
                listOf(
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1o2k, "[[sticker:p/1o2k]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1o33, "[[sticker:p/1o33]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1o3f, "[[sticker:p/1o3f]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1o3g, "[[sticker:p/1o3g]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1o3i, "[[sticker:p/1o3i]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1o3k, "[[sticker:p/1o3k]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1o66, "[[sticker:p/1o66]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1o67, "[[sticker:p/1o67]]", -1)
                )
            }
            16 -> {
                //gym
                listOf(
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1ptd, "[[sticker:p/1ptd]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1rob, "[[sticker:p/1rob]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1ron, "[[sticker:p/1ron]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1rpa, "[[sticker:p/1rpa]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1rpp, "[[sticker:p/1rpp]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1rpp_fr, "[[sticker:p/1rpp_fr]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1rpt, "[[sticker:p/1rpt]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1rpw, "[[sticker:p/1rpw]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1rpw_fr, "[[sticker:p/1rpw_fr]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1rpy, "[[sticker:p/1rpy]]", -1),
                    StuffInsertableAdapter.StuffInfos(R.drawable.sticker_1rpy_fr, "[[sticker:p/1rpy_fr]]", -1)
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
        fun insertThisString(stringToInsert: String, posOfCenterOfString: Int)
    }
}

private class StuffInsertableAdapter(val clickCallback: (stuffPositionClicked: Int, isLongClick: Boolean) -> Any?) :
    RecyclerView.Adapter<StuffInsertableAdapter.StuffViewHolder>() {
    var listOfStuffId: List<StuffInfos> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StuffViewHolder {
        return StuffViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.stuffinsertable_row, parent, false),
            clickCallback
        )
    }

    override fun getItemCount(): Int {
        return listOfStuffId.size
    }

    override fun onBindViewHolder(holder: StuffViewHolder, position: Int) {
        if (position in listOfStuffId.indices) {
            holder.bind(position, listOfStuffId[position].imageId)
        }
    }

    class StuffViewHolder(
        val mainView: View,
        clickCallback: (stuffPositionClicked: Int, isLongClick: Boolean) -> Any?
    ) : RecyclerView.ViewHolder(mainView) {
        var stuffPosition: Int = -1

        init {
            mainView.setOnClickListener { clickCallback(stuffPosition, false) }
            mainView.setOnLongClickListener { clickCallback(stuffPosition, true); true }
        }

        fun bind(newStuffPosition: Int, @DrawableRes resId: Int) {
            stuffPosition = newStuffPosition
            (mainView as? ImageView)?.setImageResource(resId)
        }
    }

    class StuffInfos(@DrawableRes val imageId: Int, val imageString: String, val posOfCenterOfString: Int)
}
