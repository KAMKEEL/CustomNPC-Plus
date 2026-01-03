export interface IOutline {
    setInnerColor(color: number, alpha: number): void;
    setOuterColor(color: number, alpha: number): void;
    setSize(size: number): IOutline;
    setNoiseSize(size: number): IOutline;
    setSpeed(speed: number): IOutline;
    setPulsingSpeed(speed: number): IOutline;
    setColorSmoothness(smoothness: number): IOutline;
    setColorInterpolation(interp: number): IOutline;
    getName(): string;
    setName(name: string): void;
    getMenuName(): string;
    setMenuName(name: string): void;
    getID(): number;
    setID(newID: number): void;
    clone(): IOutline;
    save(): IOutline;
}
