package com.sunnychung.lib.android.kotlite.interpreter.demo

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import demoScripts
import interpretKotlite

class MainActivity : Activity() {

    var btnRun: Button? = null
    var txtCode: EditText? = null
    var txtOutput: EditText? = null
    var spnExample: Spinner? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        btnRun = findViewById(R.id.btnRun)
        txtCode = findViewById(R.id.txtCode)
        txtOutput = findViewById(R.id.txtOutput)
        spnExample = findViewById(R.id.spnExample)

        spnExample?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                txtCode?.setText(demoScripts.values.toList()[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
        spnExample?.adapter = ArrayAdapter(this, R.layout.better_spinner_item, demoScripts.keys.toList())

        btnRun?.setOnClickListener {
            btnRun?.isEnabled = false

            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            txtCode?.windowToken?.let { imm.hideSoftInputFromWindow(it, 0) }

            Thread {
                txtCode?.text?.toString()?.let { code ->
                    val result = interpretKotlite(code)
                    runOnUiThread {
                        txtOutput?.setText(result)
                    }
                }
                runOnUiThread {
                    btnRun?.isEnabled = true
                }
            }.start()
        }
    }
}
