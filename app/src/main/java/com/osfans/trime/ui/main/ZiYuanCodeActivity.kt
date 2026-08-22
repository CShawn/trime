/*
 * SPDX-FileCopyrightText: 2015 - 2026 Rime community
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.osfans.trime.ui.main

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.osfans.trime.databinding.ActivityZiyuanBinding
import com.osfans.trime.util.ZiYuanUtil

/*
 * Copyright (c) 2026 Shawn Chen
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
 *
 * Description: 
 *
 * Author: Shawn Chen
 * Date: 2026/6/29
 */
class ZiYuanCodeActivity : Activity() {
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityZiyuanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val text = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString() ?: ""
        if (text.length < 2) {
            finish()
            return
        }
        val code = ZiYuanUtil.saveIn(text)
        if (code.isEmpty()) {
            Toast.makeText(baseContext, "编码失败", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        binding.tvZi.text = text
        binding.tvCode.text = code.joinToString("\n")
        Handler(Looper.getMainLooper()).postDelayed({ finish() }, 800)
    }
}
