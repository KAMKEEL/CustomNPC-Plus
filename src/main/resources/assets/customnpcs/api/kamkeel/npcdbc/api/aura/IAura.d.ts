export interface IAura {
    getName(): string;
    setName(name: string): void;
    getMenuName(): string;
    setMenuName(name: string): void;
    getID(): number;
    setID(newID: number): void;
    assignToPlayer(player: IPlayer): void;
    removeFromPlayer(player: IPlayer): void;
    assignToPlayer(playerName: string): void;
    removeFromPlayer(playerName: string): void;
    getDisplay(): any; // IAuraDisplay
    getSecondaryAuraID(): number;
    getSecondaryAura(): IAura;
    setSecondaryAura(id: number): void;
    setSecondaryAura(aura: IAura): void;
    clone(): IAura;
    save(): IAura;
}
