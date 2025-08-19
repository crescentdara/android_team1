package bitc.fullstack502.android_studio.ui

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import kotlin.math.max
import kotlin.math.min

/** 한국 휴대폰 번호 3-4-4 자동 하이픈. emoji2/Spannable 충돌 회피용 */
class PhoneHyphenTextWatcher(
    private val editText: EditText
) : TextWatcher {

    private var isFormatting = false
    private var last = ""

    override fun afterTextChanged(s: Editable?) {
        if (isFormatting) return
        isFormatting = true

        val digits = s?.toString()?.filter { it.isDigit() }.orEmpty()
        val formatted = when {
            digits.length <= 3  -> digits
            digits.length <= 7  -> digits.substring(0,3) + "-" + digits.substring(3)
            digits.length <= 11 -> digits.substring(0,3) + "-" + digits.substring(3,7) + "-" + digits.substring(7)
            else                -> digits.substring(0,3) + "-" + digits.substring(3,7) + "-" + digits.substring(7,11)
        }

        val oldCursor = editText.selectionStart
        val delta = formatted.length - last.length

        editText.removeTextChangedListener(this)
        editText.setText(formatted)
        val newCursor = min(max(0, oldCursor + delta), formatted.length)
        editText.setSelection(newCursor)
        editText.addTextChangedListener(this)

        last = formatted
        isFormatting = false
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
}