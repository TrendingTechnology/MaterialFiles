/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.fileproperties

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.StringRes
import androidx.core.view.ViewCompat
import androidx.core.view.get
import androidx.core.view.size
import androidx.fragment.app.Fragment
import me.zhanghai.android.files.compat.scrollIndicatorsCompat
import me.zhanghai.android.files.databinding.FilePropertiesTabFragmentBinding
import me.zhanghai.android.files.databinding.FilePropertiesTabItemBinding
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.util.Failure
import me.zhanghai.android.files.util.Loading
import me.zhanghai.android.files.util.Stateful
import me.zhanghai.android.files.util.fadeToVisibilityUnsafe
import me.zhanghai.android.files.util.layoutInflater
import me.zhanghai.android.files.util.valueCompat

abstract class FilePropertiesTabFragment : Fragment() {
    protected lateinit var binding: FilePropertiesTabFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = FilePropertiesTabFragmentBinding.inflate(inflater, container, false)
        .also { binding = it }
        .root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.swipeRefreshLayout.setOnRefreshListener { refresh() }
        if (Settings.MATERIAL_DESIGN_2.valueCompat) {
            binding.scrollView.scrollIndicatorsCompat =
                ViewCompat.SCROLL_INDICATOR_TOP or ViewCompat.SCROLL_INDICATOR_BOTTOM
        } else {
            binding.linearLayout.clipChildren = false
            binding.linearLayout.clipToPadding = false
        }
    }

    abstract fun refresh()

    protected inline fun <T> bindView(stateful: Stateful<T>, block: ViewBuilder.(T) -> Unit) {
        val value = stateful.value
        val hasValue = value != null
        binding.progress.fadeToVisibilityUnsafe(stateful is Loading && !hasValue)
        binding.swipeRefreshLayout.isRefreshing = stateful is Loading && hasValue
        binding.errorText.fadeToVisibilityUnsafe(stateful is Failure)
        if (stateful is Failure) {
            binding.errorText.text = stateful.throwable.toString()
        }
        binding.scrollView.fadeToVisibilityUnsafe(hasValue)
        if (value != null) {
            ViewBuilder(binding.linearLayout).apply {
                block(value)
                build()
            }
        }
    }

    protected class ViewBuilder(private val linearLayout: LinearLayout) {
        private var itemCount = 0

        fun addItemView(hint: String, text: String, isDropDown: Boolean = false) {
            val itemBinding = if (itemCount < linearLayout.size) {
                linearLayout[itemCount].tag as FilePropertiesTabItemBinding
            } else {
                FilePropertiesTabItemBinding.inflate(
                    linearLayout.context.layoutInflater, linearLayout, true
                ).also { it.root.tag = it }
            }
            itemBinding.textInputLayout.hint = hint
            itemBinding.textInputLayout.setDropDown(isDropDown)
            itemBinding.textText.setText(text)
            itemBinding.textText.setTextIsSelectable(!isDropDown)
            ++itemCount
        }

        fun addItemView(@StringRes hintRes: Int, text: String, isDropDown: Boolean = false) {
            addItemView(linearLayout.context.getString(hintRes), text, isDropDown)
        }

        fun build() {
            for (index in linearLayout.size - 1 downTo itemCount) {
                linearLayout.removeViewAt(index)
            }
        }
    }
}
