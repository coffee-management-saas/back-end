package com.futurenbetter.saas.modules.order.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class GoongDistanceResponse {
    private List<Row> rows;

    @Data
    public static class Row {
        private List<Element> elements;
    }

    @Data
    public static class Element {
        private Distance distance;
        private Duration duration;
        private String status;
    }

    @Data
    public static class Distance {
        private String text;
        private double value;
    }

    @Data
    public static class Duration {
        private String text;
        private double value;
    }
}
