/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: kamkeel.npcdbc.api
 */

export interface AbstractDBCAPI {

    // Methods
    getFormHandler(): import('./form/IFormHandler').IFormHandler;
    getAuraHandler(): import('./aura/IAuraHandler').IAuraHandler;
    getOutlineHandler(): import('./outline/IOutlineHandler').IOutlineHandler;
    getBonusHandler(): import('./effect/IBonusHandler').IBonusHandler;
    getDBCEffectHandler(): import('./effect/IDBCEffectHandler').IDBCEffectHandler;
    createForm(name: string): import('./form/IForm').IForm;
    createAura(name: string): import('./aura/IAura').IAura;
    getAura(name: string): import('./aura/IAura').IAura;
    getOrCreateForm(name: string): import('./form/IForm').IForm;
    getForm(name: string): import('./form/IForm').IForm;
    createOutline(name: string): import('./outline/IOutline').IOutline;
    getOutline(name: string): import('./outline/IOutline').IOutline;
    forceDodge(dodger: import('../../../noppes/npcs/api/entity/IEntity').IEntity, attacker: import('../../../noppes/npcs/api/entity/IEntity').IEntity): void;
    abstractDBCData(): import('./npc/IDBCStats').IDBCStats;
    getDBCData(npc: import('../../../noppes/npcs/api/entity/ICustomNpc').ICustomNpc): import('./npc/IDBCStats').IDBCStats;
    getDBCDisplay(npc: import('../../../noppes/npcs/api/entity/ICustomNpc').ICustomNpc): import('./npc/IDBCDisplay').IDBCDisplay;
    doDBCDamage(player: import('../../../noppes/npcs/api/entity/IPlayer').IPlayer, stats: import('./npc/IDBCStats').IDBCStats, damage: number): void;
    getRaceName(race: number): string;
    getFormName(race: number, form: number): string;
    getAllFormMasteryData(raceid: number, formId: number): string[];
    getAllFormsLength(race: number, nonRacial: boolean): number;
    getAllForms(race: number, nonRacial: boolean): string[];
    createKiAttack(): import('./IKiAttack').IKiAttack;
    createKiAttack(type: number, speed: number, damage: number, hasEffect: boolean, color: number, density: number, hasSound: boolean, chargePercent: number): import('./IKiAttack').IKiAttack;
    fireKiAttack(npc: import('../../../noppes/npcs/api/entity/ICustomNpc').ICustomNpc, type: number, speed: number, damage: number, hasEffect: boolean, color: number, density: number, hasSound: boolean, chargePercent: number): void;
    fireKiAttack(npc: import('../../../noppes/npcs/api/entity/ICustomNpc').ICustomNpc, kiAttack: import('./IKiAttack').IKiAttack): void;
    getSkillTPCostSingle(skillName: string, level: number): number;
    getSkillMindCostSingle(skillName: string, level: number): number;
    getSkillMindCostRecursive(skillName: string, level: number): number;
    getSkillTPCostRecursive(skillName: string, level: number): number;
    getSkillRacialTPCostSingle(race: number, level: number): number;
    getSkillRacialTPMindSingle(race: number, level: number): number;
    getSkillRacialTPCostSingleRecursive(race: number, level: number): number;
    getSkillRacialTPMindSingleRecursive(race: number, level: number): number;
    getUltraInstinctMaxLevel(): number;

}
