package com.opentext.dropmerge

import org.junit.Assert
import org.junit.Test

class WikiTableBuilderTest {

    @Test
    public void testConstructor() {
        new WikiTableBuilder();
    }

    @Test
    public void testEmpty() {
        Writer w = new StringWriter();
        new WikiTableBuilder(w).process()

        Assert.assertEquals("", w.toString());
    }

    @Test
    public void testOnlyAHeader() {
        Writer w = new StringWriter();
        final builder = new WikiTableBuilder(new IndentPrinter(w, '', false))
        builder.setHeaders(['A'])
        builder.process()

        Assert.assertEquals("<table class='confluenceTable'><tbody><tr>" +
                "<th class='confluenceTh'>A</th>" +
                "</tr></tbody></table>", w.toString());
    }

    @Test
    public void testOnlyHeaders() {
        Writer w = new StringWriter();
        final builder = new WikiTableBuilder(new IndentPrinter(w, '', false))
        builder.setHeaders(['A', 'B', 'C'])
        builder.process()

        Assert.assertEquals("<table class='confluenceTable'><tbody><tr>" +
                "<th class='confluenceTh'>A</th>" +
                "<th class='confluenceTh'>B</th>" +
                "<th class='confluenceTh'>C</th>" +
                "</tr></tbody></table>", w.toString());
    }

    @Test
    public void testDataWithHeaders() {
        Writer w = new StringWriter();
        final builder = new WikiTableBuilder(new IndentPrinter(w, '', false))
        builder.addRow(['A': '1', 'B': '2', 'C': '3'])
        builder.process()

        Assert.assertEquals("<table class='confluenceTable'><tbody>" +
                ("<tr>" +
                        "<th class='confluenceTh'>A</th>" +
                        "<th class='confluenceTh'>B</th>" +
                        "<th class='confluenceTh'>C</th>" +
                        "</tr>") +
                ("<tr>" +
                        "<td class='confluenceTd'>1</td>" +
                        "<td class='confluenceTd'>2</td>" +
                        "<td class='confluenceTd'>3</td>" +
                        "</tr>") +
                "</tbody></table>", w.toString());
    }

    @Test
    public void testMultipleDataWithHeaders() {
        Writer w = new StringWriter();
        final builder = new WikiTableBuilder(new IndentPrinter(w, '', false))
        builder.addRow(['A': '1', 'B': '2', 'C': '3'])
        builder.addRow(['A': '4', 'B': '5', 'C': '6'])
        builder.process()

        Assert.assertEquals("<table class='confluenceTable'><tbody>" +
                ("<tr>" +
                        "<th class='confluenceTh'>A</th>" +
                        "<th class='confluenceTh'>B</th>" +
                        "<th class='confluenceTh'>C</th>" +
                        "</tr>") +
                ("<tr>" +
                        "<td class='confluenceTd'>1</td>" +
                        "<td class='confluenceTd'>2</td>" +
                        "<td class='confluenceTd'>3</td>" +
                        "</tr>") +
                ("<tr>" +
                        "<td class='confluenceTd'>4</td>" +
                        "<td class='confluenceTd'>5</td>" +
                        "<td class='confluenceTd'>6</td>" +
                        "</tr>") +
                "</tbody></table>", w.toString());
    }

    @Test
    public void testMultipleDataWithOutOfOrderHeaders() {
        Writer w = new StringWriter();
        final builder = new WikiTableBuilder(new IndentPrinter(w, '', false))
        builder.addRow(['A': '1', 'B': '2', 'C': '3'])
        builder.addRow(['C': '6', 'A': '4', 'B': '5'])
        builder.process()

        Assert.assertEquals("<table class='confluenceTable'><tbody>" +
                ("<tr>" +
                        "<th class='confluenceTh'>A</th>" +
                        "<th class='confluenceTh'>B</th>" +
                        "<th class='confluenceTh'>C</th>" +
                        "</tr>") +
                ("<tr>" +
                        "<td class='confluenceTd'>1</td>" +
                        "<td class='confluenceTd'>2</td>" +
                        "<td class='confluenceTd'>3</td>" +
                        "</tr>") +
                ("<tr>" +
                        "<td class='confluenceTd'>4</td>" +
                        "<td class='confluenceTd'>5</td>" +
                        "<td class='confluenceTd'>6</td>" +
                        "</tr>") +
                "</tbody></table>", w.toString());
    }

    @Test
    public void testMultipleDataWithMissingColumns() {
        Writer w = new StringWriter();
        final builder = new WikiTableBuilder(new IndentPrinter(w, '', false))
        builder.addRow(['A': '1', 'B': '2', 'C': '3'])
        builder.addRow(['C': '6', 'A': '4'])
        builder.process()

        Assert.assertEquals("<table class='confluenceTable'><tbody>" +
                ("<tr>" +
                        "<th class='confluenceTh'>A</th>" +
                        "<th class='confluenceTh'>B</th>" +
                        "<th class='confluenceTh'>C</th>" +
                        "</tr>") +
                ("<tr>" +
                        "<td class='confluenceTd'>1</td>" +
                        "<td class='confluenceTd'>2</td>" +
                        "<td class='confluenceTd'>3</td>" +
                        "</tr>") +
                ("<tr>" +
                        "<td class='confluenceTd'>4</td>" +
                        "<td class='confluenceTd' />" +
                        "<td class='confluenceTd'>6</td>" +
                        "</tr>") +
                "</tbody></table>", w.toString());
    }

    @Test
    public void testMultipleDataWithMoreMissingColumns() {
        Writer w = new StringWriter();
        final builder = new WikiTableBuilder(new IndentPrinter(w, '', false))
        builder.addRow(['A': '1', 'B': '2'])
        builder.addRow(['C': '6', 'A': '4'])
        builder.process()

        Assert.assertEquals("<table class='confluenceTable'><tbody>" +
                ("<tr>" +
                        "<th class='confluenceTh'>A</th>" +
                        "<th class='confluenceTh'>B</th>" +
                        "<th class='confluenceTh'>C</th>" +
                        "</tr>") +
                ("<tr>" +
                        "<td class='confluenceTd'>1</td>" +
                        "<td class='confluenceTd'>2</td>" +
                        "<td class='confluenceTd' />" +
                        "</tr>") +
                ("<tr>" +
                        "<td class='confluenceTd'>4</td>" +
                        "<td class='confluenceTd' />" +
                        "<td class='confluenceTd'>6</td>" +
                        "</tr>") +
                "</tbody></table>", w.toString());
    }

    @Test
    public void testWithAnonymousData() {
        Writer w = new StringWriter();
        final builder = new WikiTableBuilder(new IndentPrinter(w, '', false))
        builder.addRow(['A': '1', 'B': '2', 'C': '3'])
        builder.addRow(['4', '5', '6'])
        builder.process()

        Assert.assertEquals("<table class='confluenceTable'><tbody>" +
                ("<tr>" +
                        "<th class='confluenceTh'>A</th>" +
                        "<th class='confluenceTh'>B</th>" +
                        "<th class='confluenceTh'>C</th>" +
                        "</tr>") +
                ("<tr>" +
                        "<td class='confluenceTd'>1</td>" +
                        "<td class='confluenceTd'>2</td>" +
                        "<td class='confluenceTd'>3</td>" +
                        "</tr>") +
                ("<tr>" +
                        "<td class='confluenceTd'>4</td>" +
                        "<td class='confluenceTd'>5</td>" +
                        "<td class='confluenceTd'>6</td>" +
                        "</tr>") +
                "</tbody></table>", w.toString());
    }
}
