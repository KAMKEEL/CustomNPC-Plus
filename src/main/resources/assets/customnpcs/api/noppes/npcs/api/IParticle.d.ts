/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api
 */

export interface IParticle {

    // Methods
    spawn(entity: import('./entity/IEntity').IEntity): void;
    spawn(world: import('./IWorld').IWorld): void;
    spawn(world: import('./IWorld').IWorld, x: number, y: number, z: number): void;
    spawnOnEntity(entity: import('./entity/IEntity').IEntity): void;
    spawnInWorld(world: import('./IWorld').IWorld): void;
    spawnInWorld(world: import('./IWorld').IWorld, x: number, y: number, z: number): void;
    setGlows(glows: boolean): void;
    getGlows(): boolean;
    setNoClip(noClip: boolean): void;
    getNoClip(): boolean;
    setFacePlayer(facePlayer: boolean): void;
    getFacePlayer(): boolean;
    setDirectory(directory: string): void;
    getDirectory(): string;
    setAmount(amount: number): void;
    getAmount(): number;
    setMaxAge(maxAge: number): void;
    getMaxAge(): number;
    setSize(width: number, height: number): void;
    getWidth(): number;
    getHeight(): number;
    setOffset(offsetX: number, offsetY: number): void;
    getOffsetX(): number;
    getOffsetY(): number;
    setAnim(animRate: number, animLoop: boolean, animStart: number, animEnd: number): void;
    getAnimRate(): number;
    getAnimLoop(): boolean;
    getAnimStart(): number;
    getAnimEnd(): number;
    setPosition(x: number, y: number, z: number): void;
    getX(): number;
    getY(): number;
    getZ(): number;
    setPosition(pos: import('./IPos').IPos): void;
    getPos(): void;
    setMotion(motionX: number, motionY: number, motionZ: number, gravity: number): void;
    getMotionX(): number;
    getMotionY(): number;
    getMotionZ(): number;
    getGravity(): number;
    setHEXColor(HEXColor: number, HEXColor2: number, HEXColorRate: number, HEXColorStart: number): void;
    getHEXColor1(): number;
    getHEXColor2(): number;
    getHEXColorRate(): number;
    getHEXColorStart(): number;
    setAlpha(alpha1: number, alpha2: number, alphaRate: number, alphaRateStart: number): void;
    getAlpha1(): number;
    getAlpha2(): number;
    getAlphaRate(): number;
    getAlphaRateStart(): number;
    setScale(scale1: number, scale2: number, scaleRate: number, scaleRateStart: number): void;
    setScaleX(scale1: number, scale2: number, scaleRate: number, scaleRateStart: number): void;
    getScaleX1(): number;
    getScaleX2(): number;
    getScaleXRate(): number;
    getScaleXRateStart(): number;
    setScaleY(scale1: number, scale2: number, scaleRate: number, scaleRateStart: number): void;
    getScaleY1(): number;
    getScaleY2(): number;
    getScaleYRate(): number;
    getScaleYRateStart(): number;
    setRotationX(rotationX1: number, rotationX2: number, rotationXRate: number, rotationXRateStart: number): void;
    getRotationX1(): number;
    getRotationX2(): number;
    getRotationXRate(): number;
    getRotationXRateStart(): number;
    setRotationY(rotationY1: number, rotationY2: number, rotationYRate: number, rotationYRateStart: number): void;
    getRotationY1(): number;
    getRotationY2(): number;
    getRotationYRate(): number;
    getRotationYRateStart(): number;
    setRotationZ(rotationZ1: number, rotationZ2: number, rotationZRate: number, rotationZRateStart: number): void;
    getRotationZ1(): number;
    getRotationZ2(): number;
    getRotationZRate(): number;
    getRotationZRateStart(): number;

}
