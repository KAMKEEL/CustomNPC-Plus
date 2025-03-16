package noppes.npcs.constants;

public enum EnumAnimationPart {
    HEAD(0),
    BODY(1),
    RIGHT_ARM(2),
    LEFT_ARM(3),
    RIGHT_LEG(4),
    LEFT_LEG(5),
    FULL_MODEL(6);

    public int id;

    EnumAnimationPart(int id) {
        this.id = id;
    }
}
