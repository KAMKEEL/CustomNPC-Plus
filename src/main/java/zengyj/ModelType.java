package zengyj;

public enum ModelType {
    HumanMale("npchumanmale"),
    Villager("npcvillager"),
    Pony("npcpony"),
    HumanFemale("npchumanfemale"),
    DwarfMale("npcdwarfmale"),
    FurryMale("npcfurrymale"),
    MonsterMale("npczombiemale"),
    MonsterFemale("npczombiefemale"),
    Skeleton("npcskeleton"),
    DwarfFemale("npcdwarffemale"),
    FurryFemale("npcfurryfemale"),
    OrcMale("npcorcfmale"),
    OrcFemale("npcorcfemale"),
    ElfMale("npcelfmale"),
    ElfFemale("npcelffemale"),
    Crystal("npccrystal"),
    Golem("npcGolem"),
    EnderChibi("npcenderchibi"),
    EnderMan("npcEnderman"),
    NagaMale("npcnagamale"),
    NagaFemale("npcnagafemale"),
    Slime("NpcSlime"),
    Dragon("NpcDragon");

    public String entityName;

    private ModelType(String entityName) {
        this.entityName = entityName;
    }
}
