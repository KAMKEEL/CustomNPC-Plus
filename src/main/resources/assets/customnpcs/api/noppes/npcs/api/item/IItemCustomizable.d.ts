/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.item
 */

export interface IItemCustomizable extends IItemStack {

    // Methods
    getScriptHandler(): any;
    getMaxStackSize(): number;
    getArmorType(): number;
    isTool(): boolean;
    isNormalItem(): boolean;
    getDigSpeed(): number;
    getDurabilityValue(): number;
    getMaxItemUseDuration(): number;
    getItemUseAction(): number;
    getEnchantability(): number;
    getTexture(): string;
    setTexture(texture: string): void;
    getDurabilityShow(): Boolean;
    setDurabilityShow(durabilityShow: Boolean): void;
    getDurabilityColor(): Integer;
    setDurabilityColor(durabilityColor: Integer): void;
    getColor(): Integer;
    setColor(color: Integer): void;
    setRotation(rotationX: Float, rotationY: Float, rotationZ: Float): void;
    setRotationRate(rotationXRate: Float, rotationYRate: Float, rotationZRate: Float): void;
    setScale(scaleX: Float, scaleY: Float, scaleZ: Float): void;
    setTranslate(translateX: Float, translateY: Float, translateZ: Float): void;
    getRotationX(): Float;
    getRotationY(): Float;
    getRotationZ(): Float;
    getRotationXRate(): Float;
    getRotationYRate(): Float;
    getRotationZRate(): Float;
    getScaleX(): Float;
    getScaleY(): Float;
    getScaleZ(): Float;
    getTranslateX(): Float;
    getTranslateY(): Float;
    getTranslateZ(): Float;

}
