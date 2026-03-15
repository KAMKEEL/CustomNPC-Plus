/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api
 */

/**
 * A particle that can be spawned in the world, complete with translation, scale, rotate, and color transformations.
 * (CNPC+ original!)
 * <p>
 * To create a particle object:
 * API.createParticle(directory);
 * Then modify its attributes as you please, and call the particle's spawn functions to see it in the world.
 * <p>
 * Spawning this particle in the world sends a hefty packet with all its data to the client. The particle is then
 * rendered with a custom renderer based on all the given attributes.
 * <p>
 * If a particle appears to not appear at first, try increasing its scale or changing its position. The entity it's
 * being spawned on or a block in the world may be blocking it.
 *
  * @javaFqn noppes.npcs.api.IParticle
*/
export interface IParticle {
    /**
     *
     * @param entity Spawns this particle object on the given entity. When spawned this way, the particle will always have its origin at the entity.
     */
    spawn(entity: import('./entity/IEntity').IEntity): import('./void').void;
    /**
     *
     * @param world Spawns the particle in the given world, at a position corresponding to this particle's position variables.
     */
    spawn(world: import('./IWorld').IWorld): import('./void').void;
    /**
     *
     * @param world Spawns the particle in the given world, at a position determined by the input parameters.
     * @param x     The X position the particle will spawn in the world.
     * @param y     The Y position the particle will spawn in the world.
     * @param z     The Z position the particle will spawn in the world.
     */
    spawn(world: import('./IWorld').IWorld, x: import('./double').double, y: import('./double').double, z: import('./double').double): import('./void').void;
    spawnOnEntity(entity: import('./entity/IEntity').IEntity): import('./void').void;
    spawnInWorld(world: import('./IWorld').IWorld): import('./void').void;
    spawnInWorld(world: import('./IWorld').IWorld, x: import('./double').double, y: import('./double').double, z: import('./double').double): import('./void').void;
    /**
     *
     * @param glows If true, this particle ignores all lighting and always renders with full brightness.
     */
    setGlows(glows: import('./boolean').boolean): import('./void').void;
    getGlows(): import('./boolean').boolean;
    setNoClip(noClip: import('./boolean').boolean): import('./void').void;
    getNoClip(): import('./boolean').boolean;
    /**
     *
     * @param facePlayer Whether the particle is always facing the player's camera. If this is disabled, the particle will appear to be laying flat face down on the ground if its rotation is unchanged.
     */
    setFacePlayer(facePlayer: import('./boolean').boolean): import('./void').void;
    getFacePlayer(): import('./boolean').boolean;
    /**
     *
     * @param directory The directory of this particle's texture. This can be any texture in a resource pack or mod, and even a URL!
     */
    setDirectory(directory: String): import('./void').void;
    getDirectory(): String;
    /**
     *
     * @param amount The amount of multiples of this particle to spawn in the world. Not too good looking if used, but has some edge cases where it's alright.
     */
    setAmount(amount: import('./int').int): import('./void').void;
    getAmount(): import('./int').int;
    /**
     *
     * @param maxAge The maximum age this particle will be around for, in MC ticks.
     */
    setMaxAge(maxAge: import('./int').int): import('./void').void;
    getMaxAge(): import('./int').int;
    /**
     * The width and height of the particle's texture you want to render in pixels. Anything more gets cut off.
     *
     * @param width  The width of the particle's texture, in pixels.
     * @param height The height of the particle's texture, in pixels.
     */
    setSize(width: import('./int').int, height: import('./int').int): import('./void').void;
    getWidth(): import('./int').int;
    getHeight(): import('./int').int;
    /**
     * The horizontal and vertical offset of the particle's texture from the top-left, starts rendering the particle at this point.
     *
     * @param offsetX The horizontal offset, in pixels (u).
     * @param offsetY The vertical offset, in pixels (v).
     */
    setOffset(offsetX: import('./int').int, offsetY: import('./int').int): import('./void').void;
    getOffsetX(): import('./int').int;
    getOffsetY(): import('./int').int;
    /**
     * Enables animation on the particle. Must set a custom width and height first using the setSize(width,height) function.
     * Every frame of animation the particle goes through should be saved on a single image file.
     * <p>
     * The renderer will read the frames of animation based on the given width and height of the particle, starting from the given X and Y offsets.
     * The renderer then goes rightwards (width) pixels for every frame. Once it can't go rightwards anymore,
     * the animation goes down (height) pixels, and once again keeps going rightwards if it has to.
     * <p>
     * If the animation loops, once there is nowhere downward to go, the animation will start over at the X and Y offset.
     *
     * @param animRate  The frame rate of the particle's animation, in MC ticks. (20 ticks = 1 second)
     * @param animLoop  Whether the animation loops or not.
     * @param animStart The amount of ticks before the particle starts animating.
     * @param animEnd   The amount of ticks the particle is around for before it stops animating.
     */
    setAnim(animRate: import('./int').int, animLoop: import('./boolean').boolean, animStart: import('./int').int, animEnd: import('./int').int): import('./void').void;
    getAnimRate(): import('./int').int;
    getAnimLoop(): import('./boolean').boolean;
    getAnimStart(): import('./int').int;
    getAnimEnd(): import('./int').int;
    /**
     *
     * @param x The X position from the particle's origin the particle will spawn at.
     * @param y The Y position from the particle's origin the particle will spawn at.
     * @param z The Z position from the particle's origin the particle will spawn at.
     */
    setPosition(x: import('./double').double, y: import('./double').double, z: import('./double').double): import('./void').void;
    getX(): import('./double').double;
    getY(): import('./double').double;
    getZ(): import('./double').double;
    setPosition(pos: import('./IPos').IPos): import('./void').void;
    getPos(): import('./void').void;
    setMotion(motionX: import('./double').double, motionY: import('./double').double, motionZ: import('./double').double, gravity: import('./float').float): import('./void').void;
    getMotionX(): import('./double').double;
    getMotionY(): import('./double').double;
    getMotionZ(): import('./double').double;
    getGravity(): import('./float').float;
    /**
     *
     * @param HEXColor      The starting HEX color of the particle.
     * @param HEXColor2     The ending HEX color of the particle.
     * @param HEXColorRate  The rate of interpolation between the two HEX colors.
     * @param HEXColorStart The amount of ticks the particle is around for before its colors begin interpolating between HEXColor and HEXColor2.
     */
    setHEXColor(HEXColor: import('./int').int, HEXColor2: import('./int').int, HEXColorRate: import('./float').float, HEXColorStart: import('./int').int): import('./void').void;
    getHEXColor1(): import('./int').int;
    getHEXColor2(): import('./int').int;
    getHEXColorRate(): import('./float').float;
    getHEXColorStart(): import('./int').int;
    /**
     *
     * @param alpha1         The starting transparency of the particle.
     * @param alpha2         The ending transparency of the particle.
     * @param alphaRate      The rate of interpolation between the two transparency values.
     * @param alphaRateStart The amount of ticks the particle is around for before its colors begin interpolating between alpha1 and alpha2.
     */
    setAlpha(alpha1: import('./float').float, alpha2: import('./float').float, alphaRate: import('./float').float, alphaRateStart: import('./int').int): import('./void').void;
    getAlpha1(): import('./float').float;
    getAlpha2(): import('./float').float;
    getAlphaRate(): import('./float').float;
    getAlphaRateStart(): import('./int').int;
    /**
     *
     * @param scale1         The starting scale/size of the particle.
     * @param scale2         The ending scale/size of the particle.
     * @param scaleRate      The rate of interpolation between the two scale values.
     * @param scaleRateStart The amount of ticks the particle is around for before its colors begin interpolating between scale1 and scale2.
     */
    setScale(scale1: import('./float').float, scale2: import('./float').float, scaleRate: import('./float').float, scaleRateStart: import('./int').int): import('./void').void;
    setScaleX(scale1: import('./float').float, scale2: import('./float').float, scaleRate: import('./float').float, scaleRateStart: import('./int').int): import('./void').void;
    getScaleX1(): import('./float').float;
    getScaleX2(): import('./float').float;
    getScaleXRate(): import('./float').float;
    getScaleXRateStart(): import('./int').int;
    setScaleY(scale1: import('./float').float, scale2: import('./float').float, scaleRate: import('./float').float, scaleRateStart: import('./int').int): import('./void').void;
    getScaleY1(): import('./float').float;
    getScaleY2(): import('./float').float;
    getScaleYRate(): import('./float').float;
    getScaleYRateStart(): import('./int').int;
    /**
     * Enables rotation about the X axis.
     * If the particle is set to always face the player, this axis is a constant horizontal line on the player's camera.
     *
     * @param rotationX1         The starting X rotation of the particle.
     * @param rotationX2         The ending X rotation of the particle.
     * @param rotationXRate      The rate of interpolation between the two X rotation values.
     * @param rotationXRateStart The amount of ticks the particle is around for before its colors begin interpolating between rotationX1 and rotationX2.
     */
    setRotationX(rotationX1: import('./float').float, rotationX2: import('./float').float, rotationXRate: import('./float').float, rotationXRateStart: import('./int').int): import('./void').void;
    getRotationX1(): import('./float').float;
    getRotationX2(): import('./float').float;
    getRotationXRate(): import('./float').float;
    getRotationXRateStart(): import('./int').int;
    /**
     * Enables rotation about the Y axis.
     * If the particle is set to always face the player, this axis is a constant vertical line on the player's camera.
     *
     * @param rotationY1         The starting Y rotation of the particle.
     * @param rotationY2         The ending Y rotation of the particle.
     * @param rotationYRate      The rate of interpolation between the two Y rotation values.
     * @param rotationYRateStart The amount of ticks the particle is around for before its colors begin interpolating between rotationY1 and rotationY2.
     */
    setRotationY(rotationY1: import('./float').float, rotationY2: import('./float').float, rotationYRate: import('./float').float, rotationYRateStart: import('./int').int): import('./void').void;
    getRotationY1(): import('./float').float;
    getRotationY2(): import('./float').float;
    getRotationYRate(): import('./float').float;
    getRotationYRateStart(): import('./int').int;
    /**
     * Enables rotation about the Y axis.
     * If the particle is set to always face the player, this axis is a constant line going into the player's camera.
     * You'll be using this one most often if the particle is set to face the player.
     *
     * @param rotationZ1         The starting Z rotation of the particle.
     * @param rotationZ2         The ending Z rotation of the particle.
     * @param rotationZRate      The rate of interpolation between the two Z rotation values.
     * @param rotationZRateStart The amount of ticks the particle is around for before its colors begin interpolating between rotationZ1 and rotationZ2.
     */
    setRotationZ(rotationZ1: import('./float').float, rotationZ2: import('./float').float, rotationZRate: import('./float').float, rotationZRateStart: import('./int').int): import('./void').void;
    getRotationZ1(): import('./float').float;
    getRotationZ2(): import('./float').float;
    getRotationZRate(): import('./float').float;
    getRotationZRateStart(): import('./int').int;
    directory: String;
    HEXColor: import('./int').int;
    HEXColor2: import('./int').int;
    HEXColorRate: import('./float').float;
    HEXColorStart: import('./int').int;
    amount: import('./int').int;
    maxAge: import('./int').int;
    motionX: import('./double').double;
    motionY: import('./double').double;
    motionZ: import('./double').double;
    gravity: import('./float').float;
    scaleX1: import('./float').float;
    scaleX2: import('./float').float;
    scaleXRate: import('./float').float;
    scaleY1: import('./float').float;
    scaleY2: import('./float').float;
    scaleYRate: import('./float').float;
    alpha1: import('./float').float;
    alpha2: import('./float').float;
    alphaRate: import('./float').float;
    scaleXRateStart: import('./int').int;
    scaleYRateStart: import('./int').int;
    alphaRateStart: import('./int').int;
    rotationX1: import('./float').float;
    rotationX2: import('./float').float;
    rotationXRate: import('./float').float;
    rotationXRateStart: import('./int').int;
    rotationY1: import('./float').float;
    rotationY2: import('./float').float;
    rotationYRate: import('./float').float;
    rotationYRateStart: import('./int').int;
    rotationZ1: import('./float').float;
    rotationZ2: import('./float').float;
    rotationZRate: import('./float').float;
    rotationZRateStart: import('./int').int;
    width: import('./int').int;
    height: import('./int').int;
    offsetX: import('./int').int;
    offsetY: import('./int').int;
    animRate: import('./int').int;
    animLoop: import('./boolean').boolean;
    animStart: import('./int').int;
    animEnd: import('./int').int;
    facePlayer: import('./boolean').boolean;
    glows: import('./boolean').boolean;
    noClip: import('./boolean').boolean;
}
