package noppes.npcs.constants;

public enum EnumParticleType {
    None(""),
    Smoke("smoke"),
    Portal("portal"),
    Redstone("reddust"),
    Lightning("magicCrit"),
    LargeSmoke("largesmoke"),
    Magic("witchMagic"),
    Enchant("enchantmenttable"),
    Crit("crit"),
    Explode("explode"),
    Music("note"),
    Flame("flame"),
    Lava("lava"),
    Splash("splash"),
    Slime("slime"),
    Heart("heart"),
    AngryVillager("angryVillager"),
    HappyVillager("happyVillager"),
    Custom("custom");

    public String particleName;

    EnumParticleType(String name) {
        this.particleName = name;
    }
}
