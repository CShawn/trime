/*
 * SPDX-FileCopyrightText: 2015 - 2026 Rime community
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.osfans.trime.util
import com.osfans.trime.data.base.DataManager
import java.io.File

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
 * Date: 2026/8/22
 */
object ZiYuanUtil {
    private val dictFile = File(DataManager.defaultDataDir, "ziyuan_single.dict.yaml")
    private val customFile = File(DataManager.defaultDataDir, "custom.txt")

    fun saveIn(text: String): List<String> {
        val code = genCode(text)
        if (code.isEmpty()) {
            return code
        }
        appendText(text)
        writeDict(text, code)
        return code
    }

    fun genCode(text: String, dictFile: File = ZiYuanUtil.dictFile): List<String> {
        val originMap = originCode(text, dictFile)
        if (text.length != originMap.size) {
            return listOf()
        }
        val codes = text.map { originMap[it.toString()] ?: return listOf() }
        // originMap为{zi:[code1,code2,...]}，code内容为2~4个字母，text遍历为多个zi
        // 编码规则：将text编码成2~4个字母的result，text中每个zi对应有多种code，排列组合成多个result，形成list返回
        // 1. text长度为2，则每个zi取各个code中的前两个（code长度为1的只取1个）
        // 2. text长度为3，则前2个zi取各个code中的第一个，第3个zi取code前2个字母（code长度为1的只取1个）
        // 3. text长度大于3，则前3个zi以及最后一个zi取各自code中的第一个
        fun prefixes(codeList: List<String>, maxLen: Int): List<String> {
            return codeList.map { if (it.length <= maxLen) it else it.substring(0, maxLen) }
                .filter { it.isNotEmpty() }
                .distinct()
        }

        val results = mutableListOf<String>()
        when (text.length) {
            2 -> {
                val firstOptions = prefixes(codes[0], 2)
                val secondOptions = prefixes(codes[1], 2)
                for (first in firstOptions) {
                    for (second in secondOptions) {
                        results.add(first + second)
                    }
                }
            }
            3 -> {
                val firstOptions = prefixes(codes[0], 1)
                val secondOptions = prefixes(codes[1], 1)
                val thirdOptions = prefixes(codes[2], 2)
                for (first in firstOptions) {
                    for (second in secondOptions) {
                        for (third in thirdOptions) {
                            results.add(first + second + third)
                        }
                    }
                }
            }
            else -> {
                val firstOptions = prefixes(codes[0], 1)
                val secondOptions = prefixes(codes[1], 1)
                val thirdOptions = prefixes(codes[2], 1)
                val lastOptions = prefixes(codes.last(), 1)
                for (first in firstOptions) {
                    for (second in secondOptions) {
                        for (third in thirdOptions) {
                            for (last in lastOptions) {
                                results.add(first + second + third + last)
                            }
                        }
                    }
                }
            }
        }
        return results
    }

    private fun appendText(text: String) {
        // 1. 按行读取customFile文件，放入set去重
        // 2. text不在set中存在，则将text追加到customFile文件后
        val existing = mutableSetOf<String>()
        if (customFile.exists()) {
            customFile.useLines { lines ->
                lines.forEach { line ->
                    if (line.isNotBlank()) {
                        existing.add(line.trim())
                    }
                }
            }
        } else {
            customFile.parentFile?.mkdirs()
            customFile.createNewFile()
        }
        if (!existing.contains(text)) {
            customFile.appendText("$text\n")
        }
    }

    private fun writeDict(text: String, codes: List<String>) {
        // 遍历codes，将"{text}\t\{code}\n"追加写入dictFile
        if (!dictFile.exists()) {
            dictFile.parentFile?.mkdirs()
            dictFile.createNewFile()
        }
        // 避免在循环中多次打开/写入文件，先构造完整内容再一次性追加
        val sb = StringBuilder()
        for (code in codes) {
            sb.append(text).append('\t').append(code).append('\n')
        }
        if (sb.isNotEmpty()) {
            dictFile.appendText(sb.toString())
        }
    }

    private fun originCode(text: String, dictFile: File): Map<String, List<String>> {
        // 1. 分离词语text包含的汉字到texts
        val texts = mutableSetOf<String>()
        for (ch in text) {
            texts.add(ch.toString())
        }
        // 2. 按行读取dictFile文件，从行内容为"..."后边为要解析的内容，前边的内容跳过
        // 3. 每行按照tab分割，第一项为zi，第二项为code，zi长度大于1则结束。当zi在texts中不存在时跳过。
        // 每行的zi可能重复，将zi和code放入map{zi:[code1,code2,...]}并返回
        val result = linkedMapOf<String, MutableList<String>>()
        if (!dictFile.exists()) {
            return result
        }
        var parse = false
        dictFile.forEachLine { line ->
            if (!parse) {
                if (line.trim() == "...") {
                    parse = true
                }
                return@forEachLine
            }
            val parts = line.split("\t")
            val zi = parts[0].trim()
            val code = parts[1].trim()
            if (zi.length != 1) return@forEachLine
            if (!texts.contains(zi)) return@forEachLine
            if (code.isEmpty()) return@forEachLine
            val codes = result.getOrPut(zi) { mutableListOf() }
            if (!codes.contains(code)) {
                codes.add(code)
            }
        }
        return result
    }
}
