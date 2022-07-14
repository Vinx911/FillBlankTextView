package cn.vinshell.widget.fillblanktextview.demo

import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.vinshell.widget.FillBlankTextView

class MainActivity : AppCompatActivity() {
    private lateinit var fillBlankTextView: FillBlankTextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            Toast.makeText(this, "${fillBlankTextView.answerList}", Toast.LENGTH_SHORT).show()
        }

        fillBlankTextView = findViewById(R.id.tv_fill_blank)
        fillBlankTextView.apply {
            this.text = """
                            |天长地久。天地所以能长且久者，__________，__________。
                            |是以圣人后其身而身先，__________。
                            |非以其无私耶？故能成其私。
                        """.trimMargin()

            this.setAnswer(1, "故能长生")

            this.blankTextColor = Color.parseColor("#1d953f")
            this.blankTextSelectColor = Color.parseColor("#007947")
            this.blankUnderlineColor = Color.parseColor("#8552a1")
            this.blankUnderlineSelectColor = Color.parseColor("#f173ac")
            this.blankUnderlineWidth = 5f
            this.blankEllipsize = TextUtils.TruncateAt.START
//        this.blankBackground = getDrawable(R.drawable.bg_blank)
//        this.setBlankBackgroundOffset(0, 10)
        }

    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        //如果是点击事件，获取点击的view，并判断是否要收起键盘
        if (ev.action == MotionEvent.ACTION_DOWN) {
            //获取目前得到焦点的view
            val view = currentFocus

            if (view != null && isShouldHideKeyboard(view, ev)) {
                fillBlankTextView.setSpanChecked(-1)
            }
        }

        return super.dispatchTouchEvent(ev)
    }

    //判断是否要收起键盘
    private fun isShouldHideKeyboard(view: View, event: MotionEvent): Boolean {
        //如果目前得到焦点的这个view是editText的话进行判断点击的位置
        if (view === fillBlankTextView.editText) {
            val location = intArrayOf(0, 0)
            view.getLocationInWindow(location)
            val left = location[0]
            val top = location[1]
            val bottom = top + view.getHeight()
            val right = left + view.getWidth()
            // 点击EditText的事件，忽略它。
            return (event.x <= left || event.x >= right
                    || event.y <= top || event.y >= bottom)
        }
        return false
    }
}