package com.example.pmgame;

import java.util.List;

public class Coordinates {

    private Float startX;
    private Float startY;
    private Float endX;
    private Float endY;
    private List<Float> daysEnds;

    List<Float> getDaysEnds() {
        return daysEnds;
    }

    void setDaysEnds(List<Float> daysEnds) {
        this.daysEnds = daysEnds;
    }

    Float getStartX() {
        return startX;
    }

    void setStartX(Float startX) {
        this.startX = startX;
    }

    Float getStartY() {
        return startY;
    }

    void setStartY(Float startY) {
        this.startY = startY;
    }

    public Float getEndX() {
        return endX;
    }

    void setEndX(Float endX) {
        this.endX = endX;
    }

    Float getEndY() {
        return endY;
    }

    void setEndY(Float endY) {
        this.endY = endY;
    }

}
