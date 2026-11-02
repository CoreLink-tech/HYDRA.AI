package com.zenlink.hydra.feature.chat

/** One block of a reply. Pure data so the parser can be unit tested on the JVM. */
sealed interface MdBlock {
    data class Heading(val level: Int, val text: String) : MdBlock
    data class Paragraph(val text: String) : MdBlock
    data class Bullets(val items: List<String>) : MdBlock
    data class Numbered(val start: Int, val items: List<String>) : MdBlock
    data class Quote(val text: String) : MdBlock

    /** [closed] is false while the closing fence has not streamed in yet. */
    data class Code(val language: String?, val code: String, val closed: Boolean) : MdBlock
    data object Rule : MdBlock
}

/** A run of text inside a block with its inline styling. */
data class Inline(
    val text: String,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val code: Boolean = false,
    val link: Boolean = false,
)

/**
 * A small markdown reader for model replies: headings, lists, quotes, rules, fenced code, and
 * bold, italic, inline code and links. It tolerates half-finished input, because replies are
 * rendered while they are still streaming (an unclosed fence is shown as code, an unclosed `**`
 * stays as plain text until its partner arrives).
 */
object MarkdownParser {

    private val heading = Regex("^(#{1,6})\\s+(.*)$")
    private val rule = Regex("^\\s*([-*_])(\\s*\\1){2,}\\s*$")
    private val bullet = Regex("^\\s*[-*+]\\s+(.*)$")
    private val numbered = Regex("^\\s*(\\d{1,9})[.)]\\s+(.*)$")
    private val quote = Regex("^\\s*>\\s?(.*)$")

    fun parse(source: String): List<MdBlock> {
        val lines = source.replace("\r\n", "\n").split('\n')
        val out = mutableListOf<MdBlock>()
        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            val trimmed = line.trim()

            if (trimmed.startsWith("```")) {
                val language = trimmed.removePrefix("```").trim().takeIf { it.isNotEmpty() }
                val body = mutableListOf<String>()
                i++
                var closed = false
                while (i < lines.size) {
                    if (lines[i].trim().startsWith("```")) {
                        closed = true
                        i++
                        break
                    }
                    body += lines[i]
                    i++
                }
                out += MdBlock.Code(language, body.joinToString("\n"), closed)
                continue
            }

            if (trimmed.isEmpty()) {
                i++
                continue
            }

            val h = heading.matchEntire(trimmed)
            if (h != null) {
                out += MdBlock.Heading(h.groupValues[1].length, h.groupValues[2].trim())
                i++
                continue
            }

            if (rule.matches(trimmed)) {
                out += MdBlock.Rule
                i++
                continue
            }

            if (bullet.matches(line)) {
                val items = mutableListOf<String>()
                while (i < lines.size) {
                    val m = bullet.matchEntire(lines[i])
                    if (m != null) {
                        items += m.groupValues[1].trim()
                    } else if (lines[i].isNotBlank() && lines[i].startsWith("  ") && items.isNotEmpty() &&
                        !numbered.matches(lines[i])
                    ) {
                        items[items.lastIndex] = items.last() + " " + lines[i].trim()
                    } else {
                        break
                    }
                    i++
                }
                out += MdBlock.Bullets(items)
                continue
            }

            val n = numbered.matchEntire(line)
            if (n != null) {
                val start = n.groupValues[1].toIntOrNull() ?: 1
                val items = mutableListOf<String>()
                while (i < lines.size) {
                    val m = numbered.matchEntire(lines[i])
                    if (m != null) {
                        items += m.groupValues[2].trim()
                    } else if (lines[i].isNotBlank() && lines[i].startsWith("  ") && items.isNotEmpty() &&
                        !bullet.matches(lines[i])
                    ) {
                        items[items.lastIndex] = items.last() + " " + lines[i].trim()
                    } else {
                        break
                    }
                    i++
                }
                out += MdBlock.Numbered(start, items)
                continue
            }

            if (quote.matches(line)) {
                val body = mutableListOf<String>()
                while (i < lines.size) {
                    val m = quote.matchEntire(lines[i]) ?: break
                    body += m.groupValues[1]
                    i++
                }
                out += MdBlock.Quote(body.joinToString("\n").trim())
                continue
            }

            // Paragraph: consecutive plain lines, until a blank line or the start of another block.
            val para = mutableListOf<String>()
            while (i < lines.size) {
                val l = lines[i]
                val t = l.trim()
                if (t.isEmpty() || t.startsWith("```") || heading.matches(t) || rule.matches(t) ||
                    bullet.matches(l) || numbered.matches(l) || quote.matches(l)
                ) {
                    break
                }
                para += t
                i++
            }
            if (para.isEmpty()) {
                i++
            } else {
                out += MdBlock.Paragraph(para.joinToString("\n"))
            }
        }
        return out
    }

    /** Splits one line or paragraph into styled runs. */
    fun inline(source: String): List<Inline> {
        val out = mutableListOf<Inline>()
        parseInline(source, bold = false, italic = false, out = out)
        return merge(out)
    }

    private fun parseInline(src: String, bold: Boolean, italic: Boolean, out: MutableList<Inline>) {
        val plain = StringBuilder()

        fun flush() {
            if (plain.isNotEmpty()) {
                out += Inline(plain.toString(), bold = bold, italic = italic)
                plain.clear()
            }
        }

        var i = 0
        loop@ while (i < src.length) {
            val c = src[i]

            if (c == '\\' && i + 1 < src.length && src[i + 1] in "\\`*_[]()#>-+.!") {
                plain.append(src[i + 1])
                i += 2
                continue@loop
            }

            if (c == '`') {
                val end = src.indexOf('`', i + 1)
                if (end > i + 1) {
                    flush()
                    out += Inline(src.substring(i + 1, end), bold = bold, italic = italic, code = true)
                    i = end + 1
                    continue@loop
                }
            }

            if (src.startsWith("**", i) || src.startsWith("__", i)) {
                val marker = src.substring(i, i + 2)
                val end = src.indexOf(marker, i + 2)
                if (end > i + 2) {
                    flush()
                    parseInline(src.substring(i + 2, end), bold = true, italic = italic, out = out)
                    i = end + 2
                    continue@loop
                }
            }

            if (c == '*' || c == '_') {
                val prev = if (i > 0) src[i - 1] else ' '
                val startsWord = i + 1 < src.length && !src[i + 1].isWhitespace()
                // Underscores inside words (snake_case) are not emphasis.
                val boundaryOk = c == '*' || !prev.isLetterOrDigit()
                if (startsWord && boundaryOk) {
                    var end = src.indexOf(c, i + 1)
                    while (end != -1 && (src[end - 1].isWhitespace() || (c == '_' && end + 1 < src.length && src[end + 1].isLetterOrDigit()))) {
                        end = src.indexOf(c, end + 1)
                    }
                    if (end > i + 1) {
                        flush()
                        parseInline(src.substring(i + 1, end), bold = bold, italic = true, out = out)
                        i = end + 1
                        continue@loop
                    }
                }
            }

            if (c == '[') {
                val close = src.indexOf(']', i + 1)
                if (close > i && src.startsWith("](", close)) {
                    val paren = src.indexOf(')', close + 2)
                    if (paren > close) {
                        flush()
                        out += Inline(src.substring(i + 1, close), bold = bold, italic = italic, link = true)
                        i = paren + 1
                        continue@loop
                    }
                }
            }

            plain.append(c)
            i++
        }
        flush()
    }

    private fun merge(runs: List<Inline>): List<Inline> {
        val merged = mutableListOf<Inline>()
        for (r in runs) {
            val last = merged.lastOrNull()
            if (last != null && last.bold == r.bold && last.italic == r.italic && last.code == r.code && last.link == r.link) {
                merged[merged.lastIndex] = last.copy(text = last.text + r.text)
            } else {
                merged += r
            }
        }
        return merged
    }
}

enum class TokenKind { Plain, Keyword, Str, Comment, Number }

data class CodeToken(val text: String, val kind: TokenKind)

/** A light, language-agnostic highlighter: keywords, strings, comments and numbers. */
object CodeHighlighter {

    private val keywords = setOf(
        "fun", "val", "var", "def", "class", "object", "interface", "enum", "struct", "impl", "fn", "let", "const",
        "return", "if", "else", "elif", "when", "for", "while", "do", "in", "is", "as", "not", "and", "or",
        "import", "from", "package", "use", "module", "export", "default", "function", "async", "await",
        "public", "private", "protected", "static", "final", "void", "int", "long", "float", "double", "bool",
        "boolean", "string", "char", "new", "this", "self", "super", "try", "catch", "except", "finally",
        "throw", "raise", "switch", "case", "break", "continue", "true", "false", "null", "nil", "none",
        "True", "False", "None", "mut", "match", "type", "lambda", "yield", "with", "pass", "SELECT", "FROM",
        "WHERE", "INSERT", "UPDATE", "DELETE", "JOIN", "CREATE", "TABLE", "INTO", "VALUES",
    )

    private val hashComment = setOf("python", "py", "bash", "sh", "shell", "zsh", "yaml", "yml", "ruby", "rb", "toml", "r", "perl")
    private val dashComment = setOf("sql", "lua", "haskell")

    fun tokenize(code: String, language: String?): List<CodeToken> {
        val lang = language?.lowercase()
        val out = mutableListOf<CodeToken>()
        val plain = StringBuilder()

        fun flush() {
            if (plain.isNotEmpty()) {
                out += CodeToken(plain.toString(), TokenKind.Plain)
                plain.clear()
            }
        }

        var i = 0
        while (i < code.length) {
            val c = code[i]
            val commentStart = when {
                lang in hashComment -> c == '#'
                lang in dashComment -> code.startsWith("--", i)
                else -> code.startsWith("//", i)
            }
            when {
                commentStart -> {
                    flush()
                    val end = code.indexOf('\n', i).let { if (it == -1) code.length else it }
                    out += CodeToken(code.substring(i, end), TokenKind.Comment)
                    i = end
                }
                lang !in hashComment && lang !in dashComment && code.startsWith("/*", i) -> {
                    flush()
                    val close = code.indexOf("*/", i + 2)
                    val end = if (close == -1) code.length else close + 2
                    out += CodeToken(code.substring(i, end), TokenKind.Comment)
                    i = end
                }
                c == '"' || c == '\'' || c == '`' -> {
                    flush()
                    var j = i + 1
                    while (j < code.length && code[j] != c && (c == '`' || code[j] != '\n')) {
                        if (code[j] == '\\' && j + 1 < code.length) j++
                        j++
                    }
                    val end = if (j < code.length && code[j] == c) j + 1 else j
                    out += CodeToken(code.substring(i, end), TokenKind.Str)
                    i = end
                }
                c.isDigit() && (i == 0 || !(code[i - 1].isLetterOrDigit() || code[i - 1] == '_')) -> {
                    flush()
                    var j = i
                    while (j < code.length && (code[j].isLetterOrDigit() || code[j] == '.' || code[j] == '_')) j++
                    out += CodeToken(code.substring(i, j), TokenKind.Number)
                    i = j
                }
                c.isLetter() || c == '_' -> {
                    var j = i
                    while (j < code.length && (code[j].isLetterOrDigit() || code[j] == '_')) j++
                    val word = code.substring(i, j)
                    if (word in keywords) {
                        flush()
                        out += CodeToken(word, TokenKind.Keyword)
                    } else {
                        plain.append(word)
                    }
                    i = j
                }
                else -> {
                    plain.append(c)
                    i++
                }
            }
        }
        flush()
        return out
    }
}
