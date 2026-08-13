package com.littleapp.todonote.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.widget.SearchView

val <T> T.exhaustive: T
    get() = this

inline fun SearchView.onQueryTextChanged(crossinline listener: (String) -> Unit) {
    this.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean = true

        override fun onQueryTextChange(newText: String?): Boolean {
            listener(newText.orEmpty())
            return true
        }
    })
}

inline fun <reified T : Activity> Context.launchActivity(
    block: Intent.() -> Unit = {}
) {
    val intent = Intent(this, T::class.java)
    intent.block()
    startActivity(intent)
}