/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.gui
 */

export interface IButton extends ICustomGuiComponent {

    // Methods
    getWidth(): number;
    getHeight(): number;
    setSize(width: number, height: number): import('./IButton').IButton;
    getLabel(): string;
    setLabel(text: string): import('./IButton').IButton;
    getTexture(): string;
    hasTexture(): boolean;
    setTexture(texture: string): import('./IButton').IButton;
    getTextureX(): number;
    getTextureY(): number;
    setTextureOffset(textureX: number, textureY: number): import('./IButton').IButton;
    setScale(scale: number): void;
    getScale(): number;
    setEnabled(enabled: boolean): void;
    isEnabled(): boolean;

}
