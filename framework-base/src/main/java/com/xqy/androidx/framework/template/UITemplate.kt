package com.xqy.androidx.framework.template

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import com.google.android.material.circularreveal.coordinatorlayout.CircularRevealCoordinatorLayout
import com.xqy.androidx.framework.state.UIStateCallback
import com.xqy.androidx.framework.template.creator.AppbarCreator
import com.xqy.androidx.framework.template.creator.ContentCreator
import com.xqy.androidx.framework.template.creator.StatusCreator
import com.xqy.androidx.framework.template.creator.UIStateCreator
import com.xqy.androidx.framework.template.creator.model.ConstrainSetModel
import com.xqy.androidx.framework.template.creator.model.UIModel
import com.xqy.androidx.framework.utils.dp


interface UITemplate {
    companion object {
        internal var titleSize: Float = 16f
        internal var titleColor: Int = android.R.color.background_light
        internal var navIcon: Int = 0
        internal var canScroll: Boolean = true
        internal var clearElevation: Boolean = false
        internal var titlePadding: Int = 0
        internal var mToolbarHeight: Int = 56.dp
        internal var themeColor: Int = android.R.color.background_light
        fun Builder(init: Builder.() -> Unit) = Builder().apply(init)
    }

    val isNeedAppbar: Boolean get() = false
    val mTemplate: Int
    val mLayoutResId: Int
    val mCenterTitle: String? get() = null
    val mToolbarTitle: String get() = ""
    val mEnableArrowIcon: Boolean get() = true
    val mIsNeedToolbar: Boolean get() = true
    val handleNavListener: () -> Boolean
        get() = {
            false
        }

    fun handleNavListener(): Boolean {
        return false
    }

    private val mActivity: AppCompatActivity
        get() {
            return when (this) {
                is AppCompatActivity -> this
                is Fragment -> this.activity!! as AppCompatActivity
                else -> {
                    throw Exception("")
                }
            }
        }

    fun inflateContentView(rootView: ViewGroup): View {
        return mActivity.layoutInflater.inflate(mLayoutResId, rootView, false)
    }

    fun createContentView(): View {
        var constrainSetModel: ConstrainSetModel?= null
        val template: ViewGroup = when (mTemplate) {
            SCAFFOLD -> CircularRevealCoordinatorLayout(mActivity)
            CONSTRAINT -> {
                ConstraintLayout(mActivity).apply {
                    val constraintSet = ConstraintSet()
                    constraintSet.clone(this)
                    constrainSetModel =
                        ConstrainSetModel(constraintSet = constraintSet)
                }
            }
            else -> throw IllegalArgumentException("unKnow template")
        }
        val mUIStateCallback = if (this is UIStateCallback) {
            this
        } else {
            null
        }
        val mUIModel = UIModel(
            themeColor,
            isNeedAppbar,
            mToolbarHeight,
            mIsNeedToolbar,
            mToolbarTitle,
            mEnableArrowIcon,
            null,
            mCenterTitle,
            mUIStateCallback,
            handleNavListener
        )
        with(StatusCreator()){
            mNextUICreator = if (mUIModel.isNeedToolbar){
                AppbarCreator().apply {
                    val mContentView = inflateContentView(template)
                    mUIModel.contentView = mContentView
                    mNextUICreator = ContentCreator().apply {
                        mUIModel.uiStateCallback?.let {
                            mNextUICreator = UIStateCreator()
                        }
                    }
                }

            }else{
                ContentCreator().apply {
                    val mContentView = inflateContentView(template)
                    mUIModel.contentView = mContentView
                    mUIModel.uiStateCallback?.let {
                        mNextUICreator = UIStateCreator()
                    }
                }
            }
            assembleWidget(template, mUIModel,constrainSetModel)

        }



        if (mTemplate== CONSTRAINT){
            constrainSetModel?.constraintSet!!.applyTo(template as ConstraintLayout)
        }

        return template
    }


    class Builder {
        fun titlePadding(init: () -> Int) {
            titlePadding = init()
        }

        fun clearElevation(init: () -> Boolean) {
            clearElevation = init()
        }

        fun toolbarHeight(init: () -> Int) {
            mToolbarHeight = init()
        }

        fun behavior(init: () -> Boolean) {
            canScroll = init()
        }

        fun titleSize(init: () -> Float) {
            titleSize = init()
        }

        fun titleColor(init: () -> Int) {
            titleColor = init()
        }

        fun navIcon(init: () -> Int) {
            navIcon = init()
        }

        fun themeColor(init: () -> Int) {
            themeColor = init()
        }

    }


}