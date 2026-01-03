/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity
 */

export interface IDBCPlayer extends IPlayer {

    // Methods
    setStat(stat: string, value: number): void;
    getStat(stat: string): number;
    addBonusAttribute(stat: string, bonusID: string, operation: string, attributeValue: number): void;
    addBonusAttribute(stat: string, bonusID: string, operation: string, attributeValue: number, endOfTheList: boolean): void;
    addToBonusAttribute(stat: string, bonusID: string, operation: string, attributeValue: number): void;
    setBonusAttribute(stat: string, bonusID: string, operation: string, attributeValue: number): void;
    getBonusAttribute(stat: string, bonusID: string): void;
    removeBonusAttribute(stat: string, bonusID: string): void;
    clearBonusAttribute(stat: string): void;
    bonusAttribute(action: string, stat: string, bonusID: string): string;
    bonusAttribute(action: string, stat: string, bonusID: string, operation: string, attributeValue: number, endOfTheList: boolean): string;
    setRelease(release: byte): void;
    getRelease(): byte;
    setBody(body: number): void;
    getBody(): number;
    setHP(hp: number): void;
    getHP(): number;
    setStamina(stamina: number): void;
    getStamina(): number;
    setKi(ki: number): void;
    getKi(): number;
    setTP(tp: number): void;
    getTP(): number;
    setGravity(gravity: number): void;
    getGravity(): number;
    isBlocking(): boolean;
    setHairCode(hairCode: string): void;
    getHairCode(): string;
    setExtraCode(extraCode: string): void;
    getExtraCode(): string;
    setItem(itemStack: import('../item/IItemStack').IItemStack, slot: byte, vanity: boolean): void;
    getItem(slot: byte, vanity: boolean): import('../item/IItemStack').IItemStack;
    getInventory(): import('../item/IItemStack').IItemStack[];
    setForm(form: byte): void;
    getForm(): byte;
    setForm2(form2: byte): void;
    getForm2(): byte;
    getRacialFormMastery(form: byte): number;
    setRacialFormMastery(form: byte, value: number): void;
    addRacialFormMastery(form: byte, value: number): void;
    getOtherFormMastery(formName: string): number;
    setOtherFormMastery(formName: string, value: number): void;
    addOtherFormMastery(formName: string, value: number): void;
    setPowerPoints(points: number): void;
    getPowerPoints(): number;
    setAuraColor(color: number): void;
    getAuraColor(): number;
    setFormLevel(level: number): void;
    getFormLevel(): number;
    setSkills(skills: string): void;
    getSkills(): string;
    setJRMCSE(statusEffects: string): void;
    getJRMCSE(): string;
    setRace(race: byte): void;
    getRace(): number;
    setDBCClass(dbcClass: byte): void;
    getDBCClass(): byte;
    setPowerType(powerType: byte): void;
    getPowerType(): number;
    getKillCount(type: string): number;
    getFusionString(): string;

}
