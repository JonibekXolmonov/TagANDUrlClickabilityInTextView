package com.example.tagurl_clickability_in_textview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import java.util.regex.Matcher
import java.util.regex.Pattern

class MainActivity : AppCompatActivity() {

    private lateinit var editText: EditText
    private lateinit var tagUrlDetectorButton: Button
    private lateinit var tvText: TextView
    private lateinit var inputText: String
    private lateinit var tags: ArrayList<String>
    private lateinit var urls: ArrayList<String>
    private lateinit var tagsUrls: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
    }

    private fun initViews() {
        editText = findViewById(R.id.edt_spannable)
        tagUrlDetectorButton = findViewById(R.id.btn_detect_tag_url)
        tvText = findViewById(R.id.tv_text_saver)
        tags = ArrayList()
        urls = ArrayList()
        tagsUrls = ArrayList()

        tagUrlDetectorButton.setOnClickListener {
            tags.clear()
            urls.clear()

            inputText = editText.text.toString()
            findAllTags(inputText)
            findAllUrls(inputText)

            tagsUrls.addAll(tags)
            tagsUrls.addAll(urls)

            makeClickableInTextView()
        }
    }


    private fun makeClickableInTextView() {
        val textViewWithClick = ArrayList<Pair<String, View.OnClickListener>>()

        tagsUrls.forEach { element ->
            val startIndex = editText.text.indexOf(element)
            val endIndex = editText.text.indexOf(element) + element.length

            textViewWithClick.add(
                Pair(editText.text.substring(startIndex, endIndex),
                    View.OnClickListener {
                        if (isFromTags(element)) {
                            makeToastForTag()
                        } else {
                            makeToastForUrl()
                        }
                    })
            )
        }
        tvText.makeLinks(textViewWithClick)
    }

    private fun TextView.makeLinks(links: ArrayList<Pair<String, View.OnClickListener>>) {
        this.text = editText.text
        val spannableString = SpannableString(this.text)
        var startIndexOfLink = -1
        for (link in links) {
            val clickableSpan = object : ClickableSpan() {
                override fun updateDrawState(textPaint: TextPaint) {
                    textPaint.color = textPaint.linkColor
                    textPaint.isUnderlineText = true
                }

                override fun onClick(view: View) {
                    Selection.setSelection((view as TextView).text as Spannable, 0)
                    view.invalidate()
                    link.second.onClick(view)
                }
            }
            startIndexOfLink = this.text.toString().indexOf(link.first, startIndexOfLink + 1)
            if (startIndexOfLink != -1) {
                spannableString.setSpan(
                    clickableSpan, startIndexOfLink, startIndexOfLink + link.first.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        this.movementMethod =
            LinkMovementMethod.getInstance() // without LinkMovementMethod, link can not click
        this.setText(spannableString, TextView.BufferType.SPANNABLE)
    }

    private fun makeToastForUrl() {
        Toast.makeText(this, "URL", Toast.LENGTH_SHORT).show()
    }

    private fun makeToastForTag() {
        Toast.makeText(this, "Tag", Toast.LENGTH_SHORT).show()
    }

    private fun findAllTags(text: String) {
        text.split(' ').forEach {
            if (it.startsWith('#')) {
                val checkedTag = checkValidity(it)
                tags.add(checkedTag)
            }
        }
    }

    private fun checkValidity(it: String): String {
        if (it.indexOf("#", 1, false) != -1) {
            return it.substring(0, it.indexOf("#", 1, false))
        }
        return it
    }

    private fun findAllUrls(text: String) {
        val URL_REGEX = "(www|http:|https:)+[^\\s]+[\\w]"
        val matcher: Matcher = Pattern.compile(URL_REGEX).matcher(text)
        while (matcher.find()) {
            urls.add(matcher.group())
        }
    }

    private fun isFromTags(it: String?): Boolean = tags.contains(it)
}