/*
 * Copyright 2022 Vinx911
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.vinx911.fillblanktextview

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