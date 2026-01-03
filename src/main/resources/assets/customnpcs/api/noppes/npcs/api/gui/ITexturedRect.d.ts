/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.gui
 */

export interface ITexturedRect extends ICustomGuiComponent {

    // Methods
    getTexture(): string;
    setTexture(texture: string): import('./ITexturedRect').ITexturedRect;
    getWidth(): number;
    getHeight(): number;
    setSize(width: number, height: number): import('./ITexturedRect').ITexturedRect;
    getScale(): number;
    setScale(scale: number): import('./ITexturedRect').ITexturedRect;
    getTextureX(): number;
    getTextureY(): number;
    setTextureOffset(textureX: number, textureY: number): import('./ITexturedRect').ITexturedRect;

}
