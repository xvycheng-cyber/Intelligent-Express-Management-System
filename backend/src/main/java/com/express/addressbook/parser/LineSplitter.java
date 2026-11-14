package com.express.addressbook.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * PR-01/PR-02：去 BOM、统一换行、按物理行切分、跳过空白行并保留行号。
 */
final class LineSplitter {

    SplitResult split(String rawText) {
        String text = rawText == null ? "" : rawText;
        if (!text.isEmpty() && text.charAt(0) == '\uFEFF') {
            text = text.substring(1);
        }
        text = text.replace("\r\n", "\n").replace('\r', '\n');

        String[] rawLines = text.split("\n", -1);
        List<Line> lines = new ArrayList<>();
        int skipped = 0;

        for (int i = 0; i < rawLines.length; i++) {
            String rawLine = rawLines[i];
            if (TextSegments.isBlank(rawLine)) {
                skipped++;
                continue;
            }
            lines.add(new Line(i + 1, rawLine));
        }
        return new SplitResult(rawLines.length, skipped, lines);
    }

    record Line(int lineNo, String rawLine) {
    }

    record SplitResult(int totalLines, int skippedLines, List<Line> lines) {
        SplitResult {
            lines = List.copyOf(lines);
        }
    }
}
