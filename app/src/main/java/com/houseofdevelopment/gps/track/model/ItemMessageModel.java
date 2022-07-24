package com.houseofdevelopment.gps.track.model;

import com.houseofdevelopment.gps.homemap.model.Pos;

public class ItemMessageModel {
    private Pos pos;
    private Long t;

    public ItemMessageModel(Pos pos, Long t) {
        this.pos = pos;
        this.t = t;
    }

    public Long getT() {
        return t;
    }

    public void setT(Long t) {
        this.t = t;
    }

    public ItemMessageModel() {
    }

    public ItemMessageModel(Pos pos) {
        this.pos = pos;
    }

    public Pos getPos() {
        return pos;
    }

    public void setPos(Pos pos) {
        this.pos = pos;
    }
}
