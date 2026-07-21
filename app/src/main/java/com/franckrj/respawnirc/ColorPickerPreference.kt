package com.franckrj.respawnirc

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.preference.DialogPreference
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.preference.PreferenceViewHolder

class ColorPickerPreference(context: Context, attrs: AttributeSet) : DialogPreference(context, attrs) {
    private val listOfColorChoices: IntArray
    private var colorChoosed: Int

    init {
        val attributesOfPref = context.obtainStyledAttributes(attrs, R.styleable.ColorPickerPreference)

        listOfColorChoices = context.resources.getIntArray(
            attributesOfPref.getResourceId(R.styleable.ColorPickerPreference_colorChoices, 0)
        )
        colorChoosed = attributesOfPref.getColor(R.styleable.ColorPickerPreference_defaultColor, 0)
        attributesOfPref.recycle()

        widgetLayoutResource = R.layout.colorpicker_preference_widget
        dialogLayoutResource = R.layout.dialog_colorpicker
    }

    private fun chooseColor(newColorChoosed: Int) {
        if (callChangeListener(newColorChoosed)) {
            colorChoosed = newColorChoosed
            persistInt(newColorChoosed)
            notifyChanged()
        }
    }

    /* La couleur par défaut vient de l'attribut defaultColor, "android:defaultValue" n'est donc pas
     * utilisé et cette méthode n'est appelée que lorsqu'une couleur a déjà été enregistrée. */
    override fun onSetInitialValue(defaultValue: Any?) {
        colorChoosed = getPersistedInt(colorChoosed)
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        (holder.findViewById(R.id.color_colorpicker_preference_widget) as ImageView).imageTintList =
            ColorStateList.valueOf(colorChoosed)
    }

    class ColorPickerDialogFragment : PreferenceDialogFragmentCompat() {
        override fun onBindDialogView(view: View) {
            super.onBindDialogView(view)

            val colorPickerPref = preference as ColorPickerPreference
            val listOfColors = colorPickerPref.listOfColorChoices
            val gridOfColors = view.findViewById<GridView>(R.id.grid_colors_dialog_colorpicker)

            gridOfColors.adapter = ColorChoicesAdapter(listOfColors, colorPickerPref.colorChoosed)
            gridOfColors.setOnItemClickListener { _, _, position, _ ->
                colorPickerPref.chooseColor(listOfColors[position])
                dismiss()
            }
        }

        /* Choisir une couleur l'applique immédiatement, il n'y a donc rien à valider. */
        override fun onPrepareDialogBuilder(builder: AlertDialog.Builder) {
            super.onPrepareDialogBuilder(builder)
            builder.setPositiveButton(null, null)
        }

        override fun onDialogClosed(positiveResult: Boolean) {}

        companion object {
            @JvmStatic
            fun newInstance(keyOfPref: String) = ColorPickerDialogFragment().apply {
                arguments = Bundle().apply { putString(ARG_KEY, keyOfPref) }
            }
        }
    }
}

private class ColorChoicesAdapter(private val listOfColors: IntArray, private val colorChoosed: Int) : BaseAdapter() {
    override fun getCount() = listOfColors.size

    override fun getItem(position: Int) = listOfColors[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val rowView = convertView
            ?: LayoutInflater.from(parent.context).inflate(R.layout.colorpicker_row, parent, false)
        val colorOfThisRow = listOfColors[position]
        val thisColorIsChoosed = (colorOfThisRow == colorChoosed)

        rowView.findViewById<ImageView>(R.id.color_colorpicker_row).imageTintList =
            ColorStateList.valueOf(colorOfThisRow)
        rowView.findViewById<ImageView>(R.id.checkmark_colorpicker_row).visibility =
            if (thisColorIsChoosed) View.VISIBLE else View.GONE
        rowView.contentDescription = rowView.context.getString(
            if (thisColorIsChoosed) R.string.colorNumberSelected else R.string.colorNumber, position + 1
        )

        return rowView
    }
}
