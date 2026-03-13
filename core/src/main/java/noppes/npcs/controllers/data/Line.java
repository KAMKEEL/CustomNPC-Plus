package noppes.npcs.controllers.data;

import noppes.npcs.api.handler.data.ILine;


public class Line implements ILine {

    public Line() {

    }

    public Line(String text) {
        this.text = text;
    }

    public String text = "";
    public String sound = "";
    public boolean hideText = false;

    public ILine copy() {
        Line line = new Line(text);
        line.sound = sound;
        line.hideText = hideText;
        return line;
    }

    @Override
    public String getText() {
        return this.text;
    }

    @Override
    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String getSound() {
        return this.sound;
    }

    @Override
    public void setSound(String sound) {
        this.sound = sound;
    }

    @Override
    public void hideText(boolean hide) {
        this.hideText = hide;
    }

    @Override
    public boolean hideText() {
        return this.hideText;
    }
}
