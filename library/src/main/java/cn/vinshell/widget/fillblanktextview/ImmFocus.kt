package cn.vinshell.widget.fillblanktextview

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView

/**
 * 处理焦点
 */
class ImmFocus {
    companion object {
        /**
         * 显示/隐藏软键盘
         */
        fun show(focus: View, on: Boolean): Boolean {
            val imm = focus.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            return if (on) {
                focus.requestFocus()
                imm.showSoftInput(focus, 0)
            } else {
                imm.hideSoftInputFromWindow(focus.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }
        }
    }

    /**
     * 焦点控件
     */
    var lastFocus: View? = null

    /**
     * 保存焦点
     */
    fun save(focus: View) {
        lastFocus = focus
        if (lastFocus != null) {
            if (!show(lastFocus!!, true) || lastFocus is TextView) {
                lastFocus = null
            }
        }
    }

    /**
     * 恢复焦点
     */
    fun restore() {
        if (lastFocus != null) {
            show(lastFocus!!, true)
            lastFocus = null
        }
    }

}