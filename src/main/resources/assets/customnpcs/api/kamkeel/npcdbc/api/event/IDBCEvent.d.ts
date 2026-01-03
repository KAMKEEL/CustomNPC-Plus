/**
 * Generated from Java file for CustomNPC+ DBC Addon
 * Package: kamkeel.npcdbc.api.event
 */

import { IPlayerEvent } from '../../../noppes/npcs/api/event/IPlayerEvent';
import { IDamageSource } from '../../../noppes/npcs/api/IDamageSource';

export type IDBCEvent = IPlayerEvent

export namespace IDBCEvent {
    export interface CapsuleUsedEvent extends IDBCEvent {
        getType(): number;
        getSubType(): number;
        getCapsuleName(): string;
    }

    export type SenzuUsedEvent = IDBCEvent

    export interface FormChangeEvent extends IDBCEvent {
        getFormBeforeID(): number;
        getFormAfterID(): number;
        isFormBeforeCustom(): boolean;
        isFormAfterCustom(): boolean;
    }

    export interface DamagedEvent extends IDBCEvent {
        getDamage(): number;
        setDamage(damage: number): void;

        getStaminaReduced(): number;
        setStaminaReduced(stamina: number): void;

        willKo(): boolean;

        getKiReduced(): number;
        setKiReduced(ki: number): void;

        getDamageSource(): IDamageSource;
        isDamageSourceKiAttack(): boolean;
        getType(): number;
    }

    export type DBCReviveEvent = IDBCEvent

    export interface DBCKnockout extends IDBCEvent {
        getDamageSource(): IDamageSource;
    }
}
