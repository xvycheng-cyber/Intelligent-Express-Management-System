package com.express.addressbook.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LikeEscaperTest {

    @Test
    void nullAndBlankAreNoFilter() {
        assertThat(LikeEscaper.escape(null)).isNull();
        assertThat(LikeEscaper.escape("")).isNull();
        assertThat(LikeEscaper.escape("   ")).isNull();
    }

    @Test
    void plainKeywordIsUnchanged() {
        assertThat(LikeEscaper.escape("西湖")).isEqualTo("西湖");
        assertThat(LikeEscaper.escape("West Lake")).isEqualTo("West Lake");
    }

    @Test
    void escapesLikeWildcardsAndEscapeChar() {
        assertThat(LikeEscaper.escape("!")).isEqualTo("!!");
        assertThat(LikeEscaper.escape("%")).isEqualTo("!%");
        assertThat(LikeEscaper.escape("_")).isEqualTo("!_");
        assertThat(LikeEscaper.escape("!%_")).isEqualTo("!!!%!_");
    }

    @Test
    void backslashIsLiteral() {
        assertThat(LikeEscaper.escape("\\")).isEqualTo("\\");
    }
}
