package noppes.npcs.constants.animation;

import noppes.npcs.client.utils.Color;

public enum EnumFrameType {
    ROTATION_X(0xffff3352), ROTATION_Y(0xff8bdc00), ROTATION_Z(0xff2890ff), PIVOT_X(0xffff3352), PIVOT_Y(0xff8bdc00), PIVOT_Z(0xff2890ff);

    public int color;

    EnumFrameType(int color) {
        this.color = color;
    }

    public Color getColor() {
        return new Color(color, 1);
    }
}
