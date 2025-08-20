package bitc.fullstack502.android_studio.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.core.widget.NestedScrollView
import bitc.fullstack502.android_studio.BuildConfig
import bitc.fullstack502.android_studio.R

open class BaseFooterActivity : AppCompatActivity() {

    // 리소스 ID로 쓸 때 (XML 레이아웃 아이디)
    override fun setContentView(@LayoutRes layoutResID: Int) {
        val shell = LayoutInflater.from(this).inflate(R.layout.activity_shell, null)
        super.setContentView(shell)

        val container = shell.findViewById<ViewGroup>(R.id.contentContainer)
        val content = LayoutInflater.from(this).inflate(layoutResID, container, false)
        container.addView(content)

        val footer = LayoutInflater.from(this).inflate(R.layout.inc_footer_jeu, container, false)
        attachFooterAtBottom(content, footer)
    }

    override fun setContentView(view: View?) {
        // 다른 오버로드로 위임 (널 들어오면 기본 params로)
        setContentView(
            requireNotNull(view) { "view is null in setContentView(view)" },
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }

    override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
        val safeView = requireNotNull(view) { "view is null in setContentView(view, params)" }
        val safeParams = params ?: ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        val shell = LayoutInflater.from(this)
            .inflate(R.layout.activity_shell, /*root*/ null, /*attachToRoot*/ false)
        super.setContentView(shell)

        val container = shell.findViewById<ViewGroup>(R.id.contentContainer)
        container.addView(safeView, safeParams)

        val footer = LayoutInflater.from(this)
            .inflate(R.layout.inc_footer_jeu, container, /*attachToRoot*/ false)

        attachFooterAtBottom(safeView, footer)
    }



    /**
     * content 안에서 스크롤러를 찾아 LinearLayout 맨 아래에 footer를 추가.
     * 없으면 우리가 NestedScrollView로 감싸고 footer 포함.
     */
    private fun attachFooterAtBottom(contentRoot: View, footer: View) {
        when (contentRoot) {
            is NestedScrollView -> {
                val child = contentRoot.getChildAt(0)
                if (child is LinearLayout) {
                    addFooterToLinear(child, footer)
                    return
                }
            }
            is ScrollView -> {
                val child = contentRoot.getChildAt(0)
                if (child is LinearLayout) {
                    addFooterToLinear(child, footer)
                    return
                }
            }
        }

        val scroller = findScroller(contentRoot)
        if (scroller != null) {
            val child = scroller.getChildAt(0)
            if (child is LinearLayout) {
                addFooterToLinear(child, footer)
                return
            }
        }

        wrapWithScrollAndAttach(contentRoot, footer)
    }

    /** 트리에서 NestedScrollView/ScrollView 찾기 */
    private fun findScroller(root: View): ViewGroup? {
        if (root is NestedScrollView || root is ScrollView) return root as ViewGroup
        if (root is ViewGroup) {
            root.children.forEach { child ->
                val hit = findScroller(child)
                if (hit != null) return hit
            }
        }
        return null
    }

    /** 스크롤러가 없으면 감싸서 footer 포함 */
    private fun wrapWithScrollAndAttach(contentRoot: View, footer: View) {
        val wrapper = NestedScrollView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            isFillViewport = true
            clipToPadding = false
        }
        val column = LinearLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
        }

        val parent = contentRoot.parent as ViewGroup
        val idx = parent.indexOfChild(contentRoot)
        parent.removeViewAt(idx)
        parent.addView(wrapper, idx)

        wrapper.addView(column)
        column.addView(
            contentRoot,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        addFooterToLinear(column, footer)
    }

    /**
     * 조상들의 padding(start/end/bottom) 누적 → 음수 마진으로 상쇄(풀블리드).
     * 스크롤러 클리핑 해제. 중복 추가 방지.
     */
    private fun addFooterToLinear(linear: LinearLayout, footer: View) {
        // 중복 추가 방지: 같은 타입/아이디의 footer가 이미 마지막에 있으면 제거
        if (linear.childCount > 0) {
            val last = linear.getChildAt(linear.childCount - 1)
            if (last.id == footer.id || last::class == footer::class) {
                linear.removeView(last)
            }
        }

        (linear.parent as? ViewGroup)?.let { p ->
            if (p is NestedScrollView || p is ScrollView) {
                p.clipToPadding = false
            }
        }
        linear.clipToPadding = false

        var accStart = 0
        var accEnd   = 0
        var accBottom = 0
        var v: View? = linear
        while (v != null && v !is androidx.drawerlayout.widget.DrawerLayout) {
            accStart  += v.paddingStart
            accEnd    += v.paddingEnd
            accBottom += v.paddingBottom
            v = v.parent as? View
        }

        val lp = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            marginStart  = -accStart
            marginEnd    = -accEnd
            bottomMargin = -accBottom
        }
        footer.layoutParams = lp

        linear.addView(footer)
    }



}
