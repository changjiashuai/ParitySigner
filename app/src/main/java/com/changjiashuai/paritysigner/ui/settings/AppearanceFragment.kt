package com.changjiashuai.paritysigner.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.lifecycleScope
import com.changjiashuai.paritysigner.BaseFragment
import com.changjiashuai.paritysigner.R
import com.changjiashuai.paritysigner.databinding.FragmentAppearanceBinding
import com.changjiashuai.paritysigner.ext.*
import com.changjiashuai.paritysigner.utils.ThemeHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/8/6 14:57.
 */
class AppearanceFragment : BaseFragment() {

    private var _binding: FragmentAppearanceBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppearanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        setupExtras()
        setupView()
//        setupViewModel()
    }

    private fun setupView() {
        val themes = resources.getStringArray(R.array.themes)
//        val themesValue = resources.getStringArray(R.array.themesValue)
//        val pureThemes = resources.getStringArray(R.array.pureThemes)
//        val pureThemesValue = resources.getStringArray(R.array.pureThemes)
//        val accents = resources.getStringArray(R.array.accents)
        lifecycleScope.launch {
            val themeIndex = context?.globalDataStore?.data?.map { prefs ->
                prefs[PREF_KEY_THEME]
            }?.first() ?: 0
//            val pureTheme = context?.globalDataStore?.data?.map { prefs ->
//                prefs[PREF_KEY_PURE_THEME] ?: 0
//            }?.first() ?: 0
//            val accent = context?.globalDataStore?.data?.map { prefs ->
//                prefs[PREF_KEY_ACCENT] ?: 0
//            }?.first() ?: 0
            binding.tvThemeValue.text = themes[themeIndex]
//            binding.tvPureThemeValue.text = [pureTheme]
//            binding.tvAccentValue.text = [accent]
            binding.llTheme.setOnClickListener {
                context?.showSingleChoiceAlert(
                    title = "Theme",
                    itemsId = R.array.themes,
                    checkedItem = themeIndex,
                    cancelText = "Cancel",
                    listener = { dialog, which ->
                        dialog.dismiss()
                        Toast.makeText(context, "which->$which", Toast.LENGTH_LONG).show()
                        lifecycleScope.launch {
                            context?.globalDataStore?.edit {
                                it[PREF_KEY_THEME] = which
                            }
                        }
                        ThemeHelper.updateNightMode(which)
                    }
                )
            }
            binding.llPureTheme.setOnClickListener {
                context?.showSingleChoiceAlert(
                    title = "Pure Themes",
                    itemsId = R.array.pureThemes,
                    checkedItem = 0,
                    cancelText = "Cancel",
                    listener = { dialog, which ->
                        dialog.dismiss()
                        Toast.makeText(context, "which->$which", Toast.LENGTH_LONG).show()
                    }
                )
            }
            binding.llAccent.setOnClickListener {
                context?.showSingleChoiceAlert(
                    title = "Accent",
                    itemsId = R.array.accents,
                    checkedItem = 0,
                    cancelText = "Cancel",
                    listener = { dialog, which ->
                        dialog.dismiss()
                        Toast.makeText(context, "which->$which", Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}