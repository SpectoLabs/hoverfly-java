package io.specto.hoverfly.junit.core.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import io.specto.hoverfly.junit.api.view.DiffView;
import java.net.URL;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DiffViewTest {

    private URL emptyDiffsJson = Resources.getResource("diff/empty-diffs.json");
    private URL recordedDiffsJson = Resources.getResource("diff/recorded-diffs.json");

    @Test
    public void shouldDeserializeDiffJsonAndCreateDiffMessage() throws Exception {
        // Given
        ObjectMapper objectMapper = new ObjectMapper();

        // When
        DiffView actual = objectMapper.readValue(recordedDiffsJson, DiffView.class);

        // Then
        assertThat(actual.getDiffs()).hasSize(2);
        assertThat(actual.getDiffs().get(0).getDiffReports()).hasSize(2);
        assertThat(actual.getDiffs().get(0).getDiffReports().get(0).getEntries()).hasSize(2);
        assertThat(actual.getDiffs().get(0).getDiffReports().get(1).getEntries()).hasSize(2);
        assertThat(actual.getDiffs().get(1).getDiffReports()).hasSize(1);
        assertThat(actual.getDiffs().get(1).getDiffReports().get(0).getEntries()).hasSize(2);

        assertThat(actual.getDiffs().get(0).createDiffMessage())
            .contains("first/path")
            .contains("have been recorded 2 diff(s)")
            .contains("at 2018-03-15T15:44:00+01:00")
            .contains("at 2018-03-15T15:45:00+01:00")
            .contains("header/Status")
            .contains("expected message")
            .contains("first actual message on the first path")
            .contains("second actual message on the first path");

        assertThat(actual.getDiffs().get(1).createDiffMessage())
            .contains("second/path")
            .contains("have been recorded 1 diff(s)")
            .contains("at 2018-03-15T15:44:00+01:00")
            .contains("header/Status")
            .contains("expected message")
            .contains("first actual message on the second path");
    }

    @Test
    public void shouldDeserializeEmptyDiffsJson() throws Exception {
        // Given
        ObjectMapper objectMapper = new ObjectMapper();

        // When
        DiffView actual = objectMapper.readValue(emptyDiffsJson, DiffView.class);

        // Then
        assertThat(actual.getDiffs()).isNull();
    }
}
