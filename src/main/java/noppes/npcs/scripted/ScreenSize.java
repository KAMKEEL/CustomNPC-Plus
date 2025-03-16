package noppes.npcs.scripted;

import noppes.npcs.api.IScreenSize;

public class ScreenSize implements IScreenSize {
    private int width;
    private int height;

    public ScreenSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getWidthPercent(double percent) {
        return (int) (((double) width) * percent / 100);
    }

    @Override
    public int getHeightPercent(double percent) {
        return (int) (((double) height) * percent / 100);
    }
}
