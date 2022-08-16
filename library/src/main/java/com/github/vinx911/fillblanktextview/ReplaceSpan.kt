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

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.text.TextUtils
import android.text.style.ReplacementSpan
import android.widget.TextView

class ReplaceSpan(val blankText: String) : ReplacementSpan() {

    /**
     * Span的ID
     */
    var index: Int = 0

    /**
     * 保存的文本
     */
    var spanText: String = ""

    /**
     * 文本颜色
     */
    var textColor: Int = Color.BLACK

    /**
     * 文本省略方式
     */
    var ellipsize: TextUtils.TruncateAt = TextUtils.TruncateAt.END

    /**
     * 下划线颜色
     */
    var underlineColor: Int = Color.BLACK

    /**
     * 背景
     */
    var background: Drawable? = null

    /**
     * 背景左偏移
     */
    var backgroundOffsetLeft: Int = 0

    /**
     * 背景上偏移
     */
    var backgroundOffsetTop: Int = 0

    /**
     * 背景右偏移
     */
    var backgroundOffsetRight: Int = 0

    /**
     * 背景下偏移
     */
    var backgroundOffsetBottom: Int = 0

    /**
     * 下划线宽度
     */
    var underlineWidth: Float = 3f

    /**
     * 附加数据
     */
    var extra: Any? = null

    /**
     * 点击监听器
     */
    var onClickListener: OnClickListener? = null

    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        return paint.measureText(String().padEnd(blankText.length, 'A')).toInt()
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        val underlineY = y + paint.fontMetrics.bottom
        val textWidth = getSize(paint, text, start, end, null)

        val ellipsize = TextUtils.ellipsize(
            spanText,
            paint as TextPaint,
            textWidth.toFloat(),
            ellipsize
        )
        var width = paint.measureText(ellipsize, 0, ellipsize.length).toInt()

        width = (textWidth - width) / 2

        if (background == null) {
            val linePaint = Paint()
            linePaint.color = underlineColor
            linePaint.strokeWidth = underlineWidth
            canvas.drawLine(x, underlineY, x + textWidth, underlineY, linePaint)
        } else {
            background?.setBounds(
                x.toInt() - backgroundOffsetLeft,
                top - backgroundOffsetTop,
                (x + textWidth).toInt() + backgroundOffsetRight,
                underlineY.toInt() + backgroundOffsetBottom
            )
            background?.draw(canvas)
        }

        val oldColor = paint.color
        paint.color = textColor
        canvas.drawText(ellipsize, 0, ellipsize.length, x + width, y.toFloat(), paint)
        paint.color = oldColor

    }

    fun onClick(v: TextView) {
        onClickListener?.onClick(v, index, this)
    }

    fun interface OnClickListener {
        fun onClick(v: TextView, index: Int, span: ReplaceSpan)
    }
}