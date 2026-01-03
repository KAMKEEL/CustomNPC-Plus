/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

export interface ICustomEffectHandler {

    // Methods
    createEffect(name: string): import('./data/ICustomEffect').ICustomEffect;
    getEffect(name: string): import('./data/ICustomEffect').ICustomEffect;
    deleteEffect(name: string): void;
    hasEffect(player: import('../entity/IPlayer').IPlayer, id: number): boolean;
    hasEffect(player: import('../entity/IPlayer').IPlayer, effect: import('./data/ICustomEffect').ICustomEffect): boolean;
    getEffectDuration(player: import('../entity/IPlayer').IPlayer, id: number): number;
    getEffectDuration(player: import('../entity/IPlayer').IPlayer, effect: import('./data/ICustomEffect').ICustomEffect): number;
    applyEffect(player: import('../entity/IPlayer').IPlayer, id: number, duration: number, level: byte): void;
    applyEffect(player: import('../entity/IPlayer').IPlayer, effect: import('./data/ICustomEffect').ICustomEffect, duration: number, level: byte): void;
    removeEffect(player: import('../entity/IPlayer').IPlayer, id: number): void;
    removeEffect(player: import('../entity/IPlayer').IPlayer, effect: import('./data/ICustomEffect').ICustomEffect): void;
    clearEffects(player: import('../entity/IPlayer').IPlayer): void;
    applyEffect(player: import('../entity/IPlayer').IPlayer, id: number, duration: number, level: byte, index: number): void;
    applyEffect(player: import('../entity/IPlayer').IPlayer, effect: import('./data/ICustomEffect').ICustomEffect, duration: number, level: byte, index: number): void;
    removeEffect(player: import('../entity/IPlayer').IPlayer, id: number, index: number): void;
    removeEffect(player: import('../entity/IPlayer').IPlayer, effect: import('./data/ICustomEffect').ICustomEffect, index: number): void;
    clearEffects(player: import('../entity/IPlayer').IPlayer, index: number): void;
    getEffectDuration(player: import('../entity/IPlayer').IPlayer, id: number, index: number): number;
    getEffectDuration(player: import('../entity/IPlayer').IPlayer, effect: import('./data/ICustomEffect').ICustomEffect, index: number): number;
    getEffect(name: string, index: number): import('./data/ICustomEffect').ICustomEffect;
    getEffect(id: number, index: number): import('./data/ICustomEffect').ICustomEffect;
    saveEffect(customEffect: import('./data/ICustomEffect').ICustomEffect): import('./data/ICustomEffect').ICustomEffect;

}
