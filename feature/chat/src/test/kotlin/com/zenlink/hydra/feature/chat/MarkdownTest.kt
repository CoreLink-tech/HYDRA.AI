package com.zenlink.hydra.feature.chat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownTest {

    @Test
    fun headingsAndParagraphs() {
        val blocks = MarkdownParser.parse("# Title\n\nSome text\nsecond line\n\n### Small")
        assertEquals(MdBlock.Heading(1, "Title"), blocks[0])
        assertEquals(MdBlock.Paragraph("Some text\nsecond line"), blocks[1])
        assertEquals(MdBlock.Heading(3, "Small"), blocks[2])
    }

    @Test
    fun bulletsAndNumberedLists() {
        val blocks = MarkdownParser.parse("- one\n- two\n\n3. three\n4. four")
        assertEquals(MdBlock.Bullets(listOf("one", "two")), blocks[0])
        assertEquals(MdBlock.Numbered(3, listOf("three", "four")), blocks[1])
    }

    @Test
    fun fencedCodeKeepsLanguageAndBody() {
        val blocks = MarkdownParser.parse("Intro\n```kotlin\nval a = 1\nval b = 2\n```\nAfter")
        val code = blocks[1] as MdBlock.Code
        assertEquals("kotlin", code.language)
        assertEquals("val a = 1\nval b = 2", code.code)
        assertTrue(code.closed)
        assertEquals(MdBlock.Paragraph("After"), blocks[2])
    }

    @Test
    fun unclosedFenceStillRendersAsCodeWhileStreaming() {
        val blocks = MarkdownParser.parse("```python\nprint('hi')")
        val code = blocks.single() as MdBlock.Code
        assertFalse(code.closed)
        assertEquals("print('hi')", code.code)
    }

    @Test
    fun quotesAndRules() {
        val blocks = MarkdownParser.parse("> quoted\n> more\n\n---")
        assertEquals(MdBlock.Quote("quoted\nmore"), blocks[0])
        assertEquals(MdBlock.Rule, blocks[1])
    }

    @Test
    fun inlineBoldItalicCodeAndLink() {
        val runs = MarkdownParser.inline("a **bold** and *it* with `code` and [site](https://x.dev)")
        assertTrue(runs.any { it.text == "bold" && it.bold })
        assertTrue(runs.any { it.text == "it" && it.italic })
        assertTrue(runs.any { it.text == "code" && it.code })
        assertTrue(runs.any { it.text == "site" && it.link })
    }

    @Test
    fun unfinishedEmphasisStaysPlainText() {
        val runs = MarkdownParser.inline("this is **not closed yet")
        assertEquals("this is **not closed yet", runs.joinToString("") { it.text })
        assertTrue(runs.none { it.bold })
    }

    @Test
    fun snakeCaseIsNotItalic() {
        val runs = MarkdownParser.inline("use my_variable_name here")
        assertTrue(runs.none { it.italic })
        assertEquals("use my_variable_name here", runs.joinToString("") { it.text })
    }

    @Test
    fun highlighterFindsKeywordsStringsCommentsAndNumbers() {
        val tokens = CodeHighlighter.tokenize("val x = \"hi\" // note\nreturn 42", "kotlin")
        assertTrue(tokens.any { it.text == "val" && it.kind == TokenKind.Keyword })
        assertTrue(tokens.any { it.text == "\"hi\"" && it.kind == TokenKind.Str })
        assertTrue(tokens.any { it.text == "// note" && it.kind == TokenKind.Comment })
        assertTrue(tokens.any { it.text == "42" && it.kind == TokenKind.Number })
    }

    @Test
    fun highlighterReassemblesTheOriginalText() {
        val code = "def f(a):\n    # comment\n    return a * 2.5"
        assertEquals(code, CodeHighlighter.tokenize(code, "python").joinToString("") { it.text })
    }
}
