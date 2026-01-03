export interface IKiAttack {
    getType(): number;
    setType(type: number): void;
    getSpeed(): number;
    setSpeed(speed: number): void;
    getDamage(): number;
    setDamage(damage: number): void;
    hasEffect(): boolean;
    setHasEffect(hasEffect: boolean): void;
    getColor(): number;
    setColor(color: number): void;
    getDensity(): number;
    setDensity(density: number): void;
    hasSound(): boolean;
    setHasSound(hasSound: boolean): void;
    getChargePercent(): number;
    setChargePercent(chargePercent: number): void;
    respectFormDestoryerConfig(): boolean;
    setRespectFormDestroyerConfig(respectFormConfig: boolean): void;
    isDestroyerAttack(): boolean;
    setDestroyerAttack(isDestroyer: boolean): void;
}
