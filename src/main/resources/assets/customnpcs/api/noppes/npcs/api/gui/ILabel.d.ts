/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.gui
 */

export interface ILabel extends ICustomGuiComponent {

    // Methods
    getText(): string;
    setText(text: string): import('./ILabel').ILabel;
    getWidth(): number;
    getHeight(): number;
    setSize(width: number, height: number): import('./ILabel').ILabel;
    getScale(): number;
    setScale(scale: number): import('./ILabel').ILabel;
    getShadow(): boolean;
    setShadow(shadow: boolean): void;

}
