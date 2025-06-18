package com.volsib.repositorysearcher.ui.profile

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.volsib.repositorysearcher.ui.fragments.SearchHistoryFragment

class ProfilePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SearchHistoryFragment()
            1 -> FavoritesFragment()
            else -> throw IllegalArgumentException("Invalid position $position")
        }
    }
} 