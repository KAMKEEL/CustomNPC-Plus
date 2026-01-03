/**
 * Generated from Java file for CustomNPC+ DBC Addon
 * Package: kamkeel.npcdbc.api
 */

import { IDBCPlayer } from '../../../noppes/npcs/api/entity/IDBCPlayer';
import { IEntityLivingBase } from '../../../noppes/npcs/api/entity/IEntityLivingBase';
import { IForm } from './form/IForm';
import { IAura } from './aura/IAura';
import { IOutline } from './outline/IOutline';
import { IKiAttack } from './IKiAttack';
import { IPlayer } from '../../../noppes/npcs/api/entity/IPlayer';

export interface IDBCAddon extends IDBCPlayer {
    getAllFullAttributes(): number[];
    getUsedMind(): number;
    getAvailableMind(): number;
    setLockOnTarget(lockOnTarget: IEntityLivingBase): void;
    setKiFistOn(on: boolean): void;
    setKiProtectionOn(on: boolean): void;
    setKiWeaponType(type: number): void;
    kiFistOn(): boolean;
    kiProtectionOn(): boolean;
    getKiWeaponType(): number;
    isTurboOn(): boolean;
    setTurboState(on: boolean): void;
    getMaxBody(): number;
    getMaxHP(): number;
    getBodyPercentage(): number;
    getMaxKi(): number;
    getMaxStamina(): number;
    getAllAttributes(): number[];
    modifyAllAttributes(attri: number[], multiplyAddedAttris: boolean, multiValue: number): void;
    modifyAllAttributes(Num: number, setStatsToNum: boolean): void;
    modifyAllAttributes(submitted: number[], setStats: boolean): void;
    multiplyAttribute(statid: number, multi: number): void;
    multiplyAllAttributes(multi: number): void;
    getFullAttribute(statid: number): number;
    getRaceName(): string;
    getCurrentDBCFormName(): string;
    changeDBCMastery(formName: string, amount: number, add: boolean): void;
    getDBCMasteryValue(formName: string): number;
    getAllDBCMasteries(): string;
    isDBCFusionSpectator(): boolean;
    isChargingKi(): boolean;
    getSkillLevel(skillname: string): number;
    getMaxStat(attribute: number): number;
    getCurrentStat(attribute: number): number;
    getCurrentFormMultiplier(): number;
    getMajinAbsorptionRace(): number;
    setMajinAbsorptionRace(race: number): void;
    getMajinAbsorptionPower(): number;
    setMajinAbsorptionPower(power: number): void;
    isMUI(): boolean;
    isKO(): boolean;
    isUI(): boolean;
    isMystic(): boolean;
    isKaioken(): boolean;
    isGOD(): boolean;
    isLegendary(): boolean;
    isDivine(): boolean;
    isMajin(): boolean;

    // Custom Form Setters (Grouped)
    setCustomForm(formID: number): void;
    setCustomForm(formID: number, ignoreUnlockCheck: boolean): void;
    setCustomForm(form: IForm): void;
    setCustomForm(form: IForm, ignoreUnlockCheck: boolean): void;
    setCustomForm(formName: string): void;
    setCustomForm(formName: string, ignoreUnlockCheck: boolean): void;

    // Custom Form Management (Grouped)
    hasCustomForm(formName: string): boolean;
    hasCustomForm(formID: number): boolean;
    getCustomForms(): IForm[];
    giveCustomForm(formName: string): void;
    giveCustomForm(form: IForm): void;
    removeCustomForm(formName: string): void;
    removeCustomForm(formName: string, removesMastery: boolean): void;
    removeCustomForm(form: IForm): void;
    removeCustomForm(form: IForm, removesMastery: boolean): void;

    // Flight
    setFlight(flightOn: boolean): void;
    isFlying(): boolean;
    setAllowFlight(allowFlight: boolean): void;
    setFlightSpeedRelease(release: number): void;
    setBaseFlightSpeed(speed: number): void;
    setDynamicFlightSpeed(speed: number): void;
    setFlightGravity(isEffected: boolean): void;
    setFlightDefaults(): void;
    setSprintSpeed(speed: number): void;

    // Selected Form
    getSelectedForm(): IForm;
    setSelectedForm(form: IForm): void;
    setSelectedForm(formID: number): void;
    removeSelectedForm(): void;
    getSelectedDBCForm(): number;
    setSelectedDBCForm(formID: number): void;
    removeSelectedDBCForm(): void;
    getCurrentForm(): IForm;
    isInCustomForm(): boolean;
    isInCustomForm(form: IForm): boolean;
    isInCustomForm(formID: number): boolean;

    // Aura
    setAuraSelection(auraName: string): void;
    setAuraSelection(aura: IAura): void;
    setAuraSelection(auraID: number): void;
    giveAura(auraName: string): void;
    giveAura(aura: IAura): void;
    giveAura(auraID: number): void;
    hasAura(auraName: string): boolean;
    hasAura(auraId: number): boolean;
    removeAura(auraName: string): void;
    removeAura(aura: IAura): void;
    removeAura(auraID: number): void;
    setAura(auraName: string): void;
    setAura(aura: IAura): void;
    setAura(auraID: number): void;
    removeCurrentAura(): void;
    removeAuraSelection(): void;
    isInAura(): boolean;
    isInAura(aura: IAura): boolean;
    isInAura(auraName: string): boolean;
    isInAura(auraID: number): boolean;

    // Mastery
    setCustomMastery(formID: number, value: number): void;
    setCustomMastery(formID: number, value: number, ignoreUnlockCheck: boolean): void;
    setCustomMastery(form: IForm, value: number): void;
    setCustomMastery(form: IForm, value: number, ignoreUnlockCheck: boolean): void;
    addCustomMastery(formID: number, value: number): void;
    addCustomMastery(formID: number, value: number, ignoreUnlockCheck: boolean): void;
    addCustomMastery(form: IForm, value: number): void;
    addCustomMastery(form: IForm, value: number, ignoreUnlockCheck: boolean): void;
    getCustomMastery(formID: number): number;
    getCustomMastery(formID: number, checkFusion: boolean): number;
    getCustomMastery(form: IForm): number;
    getCustomMastery(form: IForm, checkFusion: boolean): number;
    removeCustomMastery(formID: number): void;
    removeCustomMastery(form: IForm): void;

    // Outline
    removeOutline(): void;
    setOutline(outline: IOutline): void;
    setOutline(outlineName: string): void;
    setOutline(outlineID: number): void;
    getOutline(): IOutline;

    // Other
    getFusionPartner(): IPlayer;
    fireKiAttack(type: number, speed: number, damage: number, hasEffect: boolean, color: number, density: number, hasSound: boolean, chargePercent: number): void;
    fireKiAttack(kiAttack: IKiAttack): void;
    isReleasing(): boolean;
    isMeditating(): boolean;
    isSuperRegen(): boolean;
    isSwooping(): boolean;
    isInMedicalLiquid(): boolean;
    getAttackFromSlot(slot: number): IKiAttack;
}
