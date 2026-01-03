export interface IForm {
    getName(): string;
    setName(name: string): void;
    getMenuName(): string;
    setMenuName(name: string): void;
    getRace(): number;
    setRace(race: number): void;
    getAllMulti(): number[]; // float[]
    setAllMulti(allMulti: number): void; // float
    raceEligible(race: number): boolean;
    raceEligible(player: IPlayer): boolean;
    setAttributeMulti(id: number, multi: number): void;
    getAttributeMulti(id: number): number;
    assignToPlayer(player: IPlayer): void;
    removeFromPlayer(player: IPlayer): void;
    assignToPlayer(playerName: string): void;
    removeFromPlayer(playerName: string): void;
    removeFromPlayer(player: IPlayer, removesMastery: boolean): void;
    removeFromPlayer(playerName: string, removesMastery: boolean): void;
    getAscendSound(): string;
    setAscendSound(directory: string): void;
    getDescendSound(): string;
    setDescendSound(directory: string): void;
    getID(): number;
    setID(newID: number): void;
    getChildID(): number;
    hasChild(): boolean;
    linkChild(formID: number): void;
    linkChild(form: IForm): void;
    isFromParentOnly(): boolean;
    setFromParentOnly(set: boolean): void;
    addFormRequirement(race: number, state: number): void;
    removeFormRequirement(race: number): void;
    getFormRequirement(race: number): number;
    isChildOf(parent: IForm): boolean;
    getChild(): IForm;
    removeChildForm(): void;
    getParentID(): number;
    hasParent(): boolean;
    linkParent(formID: number): void;
    linkParent(form: IForm): void;
    getParent(): IForm;
    getTimer(): number;
    setTimer(timeInTicks: number): void;
    hasTimer(): boolean;
    removeParentForm(): void;
    getMastery(): any; // IFormMastery
    getDisplay(): any; // IFormDisplay
    getStackable(): any; // IFormStackable
    getAdvanced(): any; // IFormAdvanced
    setMindRequirement(mind: number): void;
    getMindRequirement(): number;
    clone(): IForm;
    save(): IForm;
}
