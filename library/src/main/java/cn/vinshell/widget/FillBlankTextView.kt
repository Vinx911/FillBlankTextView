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

package cn.vinshell.widget

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.*
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import cn.vinshell.widget.fillblanktextview.ImmFocus
import cn.vinshell.widget.fillblanktextview.ReplaceSpan


@Suppress("MemberVisibilityCanBePrivate", "unused")
class FillBlankTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val SPILT_TAG_REGEX = "_{4,}"

        private const val ELLIPSIZE_NOT_SET = -1
        private const val ELLIPSIZE_DEFAULT = 0
        private const val ELLIPSIZE_START = 1
        private const val ELLIPSIZE_MIDDLE = 2
        private const val ELLIPSIZE_END = 3
        private const val ELLIPSIZE_MARQUEE = 4
    }

    private var spannableStringBuilder = SpannableStringBuilder("")
    private val spanList: MutableList<ReplaceSpan> = ArrayList()
    private var textSplit: List<String> = emptyList()
    private var oldSpanId = -1
    private var spanRect: RectF? = null
    private var fontT = 0 // ??????top
    private var fontB = 0 // ??????bottom
    private var checkedSpan: ReplaceSpan? = null//??????????????????

    /**
     * ???????????????
     */
    lateinit var editText: EditText
        private set

    /**
     * ??????????????????
     */
    lateinit var textView: TextView
        private set

    /**
     * ??????
     */
    var text: CharSequence = ""
        set(value) {
            field = value

            doParseFillBlank()
        }

    /**
     * ????????????
     */
    val answerList: List<String>
        get() {
            // ???????????????????????????????????????Span
            setLastCheckedSpanText()

            val answerList: MutableList<String> = ArrayList()
            for (i in spanList.indices) {
                answerList.add(spanList[i].spanText)
            }
            return answerList
        }

    /**
     * ???????????????
     */
    var blankTextColor: Int = Color.BLACK
        set(value) {
            field = value

            setSpanChecked(oldSpanId)
        }

    /**
     * ???????????????
     */
    var blankTextSelectColor: Int = Color.BLACK
        set(value) {
            field = value

            editText.setTextColor(value)
            setSpanChecked(oldSpanId)
        }

    /**
     * ?????????????????????
     */
    var blankEllipsize: TextUtils.TruncateAt = TextUtils.TruncateAt.END
        set(value) {
            field = value

            spanList.forEach {
                it.ellipsize = value
            }
        }

    /**
     * ??????????????????
     */
    var blankUnderlineColor: Int = Color.BLACK
        set(value) {
            field = value

            setSpanChecked(oldSpanId)
        }

    /**
     * ??????????????????
     */
    var blankUnderlineSelectColor: Int = Color.BLACK
        set(value) {
            field = value

            setSpanChecked(oldSpanId)
        }

    /**
     * ??????????????????
     */
    var blankUnderlineWidth: Float = 3f
        set(value) {
            field = value

            spanList.forEach {
                it.underlineWidth = value
            }
        }

    /**
     * ?????????
     */
    var blankBackground: Drawable? = null
        set(value) {
            field = value

            spanList.forEach {
                it.background = value
            }
        }

    /**
     * ??????????????????, ???????????????????????????
     */
    var blankBackgroundOffsetLeft: Int = 0
        set(value) {
            field = value

            spanList.forEach {
                it.backgroundOffsetLeft = value
            }
        }

    /**
     * ??????????????????, ???????????????????????????
     */
    var blankBackgroundOffsetTop: Int = 0
        set(value) {
            field = value

            spanList.forEach {
                it.backgroundOffsetTop = value
            }
        }

    /**
     * ??????????????????, ???????????????????????????
     */
    var blankBackgroundOffsetRight: Int = 0
        set(value) {
            field = value

            spanList.forEach {
                it.backgroundOffsetRight = value
            }
        }

    /**
     * ??????????????????, ???????????????????????????
     */
    var blankBackgroundOffsetBottom: Int = 0
        set(value) {
            field = value

            spanList.forEach {
                it.backgroundOffsetBottom = value
            }
        }

    /**
     * ??????????????????
     */
    private val linkMovementMethod = object : LinkMovementMethod() {
        override fun onTouchEvent(
            widget: TextView,
            buffer: Spannable,
            event: MotionEvent
        ): Boolean {
            val action = event.action

            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
                var x = event.x.toInt()
                var y = event.y.toInt()

                x -= widget.totalPaddingLeft
                y -= widget.totalPaddingTop

                x += widget.scrollX
                y += widget.scrollY

                val layout = widget.layout
                val line = layout.getLineForVertical(y)
                val off = layout.getOffsetForHorizontal(line, x.toFloat())

                val spans = buffer.getSpans(
                    off, off,
                    ReplaceSpan::class.java
                )

                if (spans.isNotEmpty()) {
                    val span = spans[0]
                    if (action == MotionEvent.ACTION_UP) {
                        span.onClick(widget)
                    }
                    return true
                }
            }

            return super.onTouchEvent(widget, buffer, event)
        }
    }

    /**
     * span???????????????
     */
    private val spanOnClickListener = ReplaceSpan.OnClickListener { _, index, span ->
        val spanRect = getSpanRect(span) ?: return@OnClickListener
        val answer = editText.text.toString()
        setAnswer(oldSpanId, answer)
        oldSpanId = index
        editText.setText(span.spanText)
        editText.setSelection(span.spanText.length)
        span.spanText = ""
        setEditTextPosition(spanRect)
        setSpanChecked(index)
    }

    init {
        initView(context, attrs)
        initAttrs(context, attrs)

        blankTextColor = textView.currentTextColor
        blankTextSelectColor = textView.currentTextColor

        text = textView.text
    }

    private fun initView(context: Context, attrs: AttributeSet?) {
        textView = TextView(context, attrs)
        editText = EditText(context, attrs)

        this.addView(textView)
        this.addView(editText)

        // textView
        val textViewLayoutParams = LayoutParams(textView.layoutParams)
        textViewLayoutParams.width = LayoutParams.MATCH_PARENT
        textViewLayoutParams.height = LayoutParams.MATCH_PARENT
        textViewLayoutParams.setMargins(0, 0, 0, 0)
        textView.layoutParams = textViewLayoutParams

        // editText
        val editTextLayoutParams = LayoutParams(editText.layoutParams)
        editTextLayoutParams.width = 0
        editTextLayoutParams.height = 0
        editTextLayoutParams.setMargins(0, 0, 0, 0)
        editText.layoutParams = editTextLayoutParams
        editText.background = ColorDrawable(Color.TRANSPARENT)
        editText.setPadding(0, 0, 0, 0)
        editText.maxLines = 1
        editText.minWidth = 4
        editText.isSingleLine = true
        editText.gravity = Gravity.CENTER
        editText.setTextColor(textView.textColors)

        editText.addTextChangedListener {
            updateTextReplacedBySpan(oldSpanId, it.toString())
        }

        editText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                setAnswer(oldSpanId, editText.text.toString())
                editText.visibility = View.GONE
                showEditTextImm(false)
            }
        }
    }


    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.FillBlankTextView)
        try {
            blankTextColor = ta.getColor(
                R.styleable.FillBlankTextView_fbtvBlankTextColor,
                blankTextColor
            )

            blankTextSelectColor = ta.getColor(
                R.styleable.FillBlankTextView_fbtvBlankTextSelectColor,
                blankTextSelectColor
            )

            blankUnderlineColor = ta.getColor(
                R.styleable.FillBlankTextView_fbtvBlankUnderlineColor,
                blankUnderlineColor
            )

            blankUnderlineSelectColor = ta.getColor(
                R.styleable.FillBlankTextView_fbtvBlankTextSelectColor,
                blankUnderlineSelectColor
            )

            when (ta.getInt(R.styleable.FillBlankTextView_fbtvBlankEllipsize, ELLIPSIZE_NOT_SET)) {
                ELLIPSIZE_DEFAULT -> blankEllipsize = TextUtils.TruncateAt.END
                ELLIPSIZE_START -> blankEllipsize = TextUtils.TruncateAt.START
                ELLIPSIZE_MIDDLE -> blankEllipsize = TextUtils.TruncateAt.MIDDLE
                ELLIPSIZE_END -> blankEllipsize = TextUtils.TruncateAt.END
                ELLIPSIZE_MARQUEE -> blankEllipsize = TextUtils.TruncateAt.MARQUEE
            }

            blankUnderlineWidth = ta.getDimension(
                R.styleable.FillBlankTextView_fbtvBlankEllipsize,
                blankUnderlineWidth
            )

            blankBackground = ta.getDrawable(R.styleable.FillBlankTextView_fbtvBlankBackground)

            blankBackgroundOffsetLeft = ta.getDimension(
                R.styleable.FillBlankTextView_fbtvBlankBackgroundOffsetLeft,
                blankBackgroundOffsetLeft.toFloat()
            ).toInt()

            blankBackgroundOffsetTop = ta.getDimension(
                R.styleable.FillBlankTextView_fbtvBlankBackgroundOffsetTop,
                blankBackgroundOffsetTop.toFloat()
            ).toInt()

            blankBackgroundOffsetRight = ta.getDimension(
                R.styleable.FillBlankTextView_fbtvBlankBackgroundOffsetRight,
                blankBackgroundOffsetRight.toFloat()
            ).toInt()

            blankBackgroundOffsetBottom = ta.getDimension(
                R.styleable.FillBlankTextView_fbtvBlankBackgroundOffsetBottom,
                blankBackgroundOffsetBottom.toFloat()
            ).toInt()
        } finally {
            ta.recycle()
        }
    }

    /**
     * ?????? RelativeLayout WRAP_CONTENT ????????????
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        val textViewLayoutParams = textView.layoutParams

        when (widthMode) {
            MeasureSpec.AT_MOST -> {
                textViewLayoutParams.width = LayoutParams.WRAP_CONTENT
            }
            else -> {
                textViewLayoutParams.width = LayoutParams.MATCH_PARENT
            }
        }

        when (heightMode) {
            MeasureSpec.AT_MOST -> {
                textViewLayoutParams.height = LayoutParams.WRAP_CONTENT
            }
            else -> {
                textViewLayoutParams.height = LayoutParams.MATCH_PARENT
            }
        }

        textView.layoutParams = textViewLayoutParams

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    /**
     * ????????????
     */
    private fun doParseFillBlank() {
        textView.movementMethod = linkMovementMethod

        val regex = Regex(SPILT_TAG_REGEX)
        val blankMatches = regex.findAll(text)
        textSplit = regex.split(text)

        spanList.clear()
        spannableStringBuilder = SpannableStringBuilder(text)

        blankMatches.forEachIndexed { index, result ->
            val span = ReplaceSpan(result.value).apply {
                this.index = index
                this.spanText = ""
                this.textColor = blankTextColor
                this.ellipsize = blankEllipsize
                this.underlineColor = blankUnderlineColor
                this.underlineWidth = blankUnderlineWidth
                this.onClickListener = spanOnClickListener
            }
            spanList.add(span)
            spannableStringBuilder.setSpan(
                span,
                result.range.first,
                result.range.last + 1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        textView.text = spannableStringBuilder
    }

    /**
     * ?????????Span???????????????
     */
    private fun updateTextReplacedBySpan(index: Int, answer: String) {
        if (spanList.size == 0 || index < 0 || index > spanList.size - 1) return

        val span = spanList[index]
        val start = spannableStringBuilder.getSpanStart(span)
        val end = spannableStringBuilder.getSpanEnd(span)

        @Suppress("LiftReturnOrAssignment")
        if (answer.isNotEmpty()) {
            val spannableString = SpannableString(answer)
            spannableString.setSpan(span, 0, answer.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannableStringBuilder = spannableStringBuilder.replace(start, end, spannableString)
        } else {
            val spannableString = SpannableString(span.blankText)
            spannableString.setSpan(
                span,
                0,
                span.blankText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableStringBuilder = spannableStringBuilder.replace(start, end, spannableString)
        }

        textView.text = spannableStringBuilder
    }

    /**
     * ??????Span???????????????
     */
    private fun getSpanRect(span: ReplaceSpan): RectF? {
        val layout = textView.layout ?: return null
        val buffer = textView.text as Spannable

        val l = buffer.getSpanStart(span)
        val r = buffer.getSpanEnd(span)
        var line = layout.getLineForOffset(l)
        if (spanRect == null) {
            spanRect = RectF()
            val fontMetrics: Paint.FontMetrics = textView.paint.fontMetrics

            fontT = fontMetrics.ascent.toInt()
            fontB = fontMetrics.descent.toInt()
        }

        spanRect?.let {
            it.left = layout.getPrimaryHorizontal(l)
            it.right = layout.getSecondaryHorizontal(r)
            // ?????????????????????
            line = layout.getLineBaseline(line)
            it.top = (line + fontT).toFloat()
            it.bottom = (line + fontB).toFloat()
        }

        return spanRect
    }

    /**
     * ????????????????????????
     */
    private fun setEditTextPosition(rect: RectF) {
        //??????et w,h??????
        val lp = editText.layoutParams as LayoutParams
        lp.width = (rect.right - rect.left).toInt()
        lp.height = (rect.bottom - rect.top).toInt()

        //??????et ?????????tv x,y???????????????
        lp.leftMargin = (textView.left + rect.left).toInt()
        lp.topMargin = (textView.top + rect.top).toInt()
        editText.layoutParams = lp

        //??????????????????????????????
        editText.visibility = View.VISIBLE
        editText.isFocusable = true
        editText.requestFocus()
        showEditTextImm(true)
    }

    /**
     * ???????????????
     */
    private fun showEditTextImm(on: Boolean) {
        try {
            if (on) {
                ImmFocus.show(editText, true)
            } else {
                ImmFocus.show(editText, false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * ??????????????????????????????????????????Span
     */
    private fun setLastCheckedSpanText() {
        checkedSpan?.spanText = editText.text.toString()
    }

    /**
     * ?????????????????????
     */
    @JvmOverloads
    fun setAnswer(index: Int, answer: String, extra: Any? = null) {
        if (spanList.size == 0 || index < 0 || index > spanList.size - 1) return

        val span = spanList[index]
        span.spanText = answer
        span.extra = extra

        textView.text = spannableStringBuilder // ????????????
    }

    /**
     * ????????????????????????
     */
    fun getAnswer(index: Int): String {
        if (spanList.size == 0 || index < 0 || index > spanList.size - 1) return ""

        val span = spanList[index]
        return span.spanText
    }

    /**
     * ????????????????????????????????????
     */
    fun getAnswerExtra(index: Int): Any? {
        if (spanList.size == 0 || index < 0 || index > spanList.size - 1) return null

        val span = spanList[index]
        return span.extra
    }

    /**
     * ???????????????Span
     * @param index
     */
    fun setSpanChecked(index: Int) {
        checkedSpan = spanList.getOrNull(index)
        if (checkedSpan == null) {
            editText.clearFocus()
        }

        for (i in spanList.indices) {
            val replaceSpan: ReplaceSpan = spanList[i]
            if (i == index) {
                replaceSpan.textColor = blankTextSelectColor
                replaceSpan.underlineColor = blankUnderlineSelectColor
            } else {
                replaceSpan.textColor = blankTextColor
                replaceSpan.underlineColor = blankUnderlineColor
            }
        }
        textView.invalidate()
    }

    /**
     * ?????????????????????, ???????????????????????????
     */
    fun setBlankBackgroundOffset(offset: Int) {
        blankBackgroundOffsetLeft = offset
        blankBackgroundOffsetTop = offset
        blankBackgroundOffsetRight = offset
        blankBackgroundOffsetBottom = offset
    }

    /**
     * ?????????????????????, ???????????????????????????
     */
    fun setBlankBackgroundOffset(horizontal: Int, vertical: Int) {
        blankBackgroundOffsetLeft = horizontal
        blankBackgroundOffsetTop = vertical
        blankBackgroundOffsetRight = horizontal
        blankBackgroundOffsetBottom = vertical
    }

    /**
     * ?????????????????????, ???????????????????????????
     */
    fun setBlankBackgroundOffset(left: Int, top: Int, right: Int, bottom: Int) {
        blankBackgroundOffsetLeft = left
        blankBackgroundOffsetTop = top
        blankBackgroundOffsetRight = right
        blankBackgroundOffsetBottom = bottom
    }
}