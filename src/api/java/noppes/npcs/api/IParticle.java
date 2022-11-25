package noppes.npcs.api;

import noppes.npcs.api.entity.IEntity;

/**
 * A particle that can be spawned in the world, complete with translation, scale, rotate, and color transformations.
 * (CNPC+ original!)
 *
 * To create a particle object:
 * API.createParticle(directory);
 * Then modify its attributes as you please, and call the particle's spawn functions to see it in the world.
 *
 * Spawning this particle in the world sends a hefty packet with all its data to the client. The particle is then
 * rendered with a custom renderer based on all the given attributes.
 *
 * If a particle appears to not appear at first, try increasing its scale or changing its position. The entity it's
 * being spawned on or a block in the world may be blocking it.
 *
 */
public interface IParticle {

    /**
     *
     * @param entity Spawns this particle object on the given entity. When spawned this way, the particle will always have its origin at the entity.
     */
    void spawn(IEntity entity);

    /**
     *
     * @param world Spawns the particle in the given world, at a position corresponding to this particle's position variables.
     */
    void spawn(IWorld world);

    /**
     *
     * @param world Spawns the particle in the given world, at a position determined by the input parameters.
     * @param x The X position the particle will spawn in the world.
     * @param y The Y position the particle will spawn in the world.
     * @param z The Z position the particle will spawn in the world.
     */
    void spawn(IWorld world, double x, double y, double z);

    @Deprecated
    void spawnOnEntity(IEntity entity);

    @Deprecated
    void spawnInWorld(IWorld world);

    @Deprecated
    void spawnInWorld(IWorld world, double x, double y, double z);

    /**
     *
     * @param glows If true, this particle ignores all lighting and always renders with full brightness.
     */
    void setGlows(boolean glows);
    boolean getGlows();

    void setNoClip(boolean noClip);
    boolean getNoClip();

    /**
     *
     * @param facePlayer Whether the particle is always facing the player's camera. If this is disabled, the particle will appear to be laying flat face down on the ground if its rotation is unchanged.
     */
    void setFacePlayer(boolean facePlayer);
    boolean getFacePlayer();

    /**
     *
     * @param directory The directory of this particle's texture. This can be any texture in a resource pack or mod, and even a URL!
     */
    void setDirectory(String directory);
    String getDirectory();

    /**
     *
     * @param amount The amount of multiples of this particle to spawn in the world. Not too good looking if used, but has some edge cases where it's alright.
     */
    void setAmount(int amount);
    int getAmount();

    /**
     *
     * @param maxAge The maximum age this particle will be around for, in MC ticks.
     */
    void setMaxAge(int maxAge);
    int getMaxAge();

    /**
     * The width and height of the particle's texture you want to render in pixels. Anything more gets cut off.
     * @param width The width of the particle's texture, in pixels.
     * @param height The height of the particle's texture, in pixels.
     */
    void setSize(int width, int height);
    int getWidth();
    int getHeight();

    /**
     *  The horizontal and vertical offset of the particle's texture from the top-left, starts rendering the particle at this point.
     * @param offsetX The horizontal offset, in pixels (u).
     * @param offsetY The vertical offset, in pixels (v).
     */
    void setOffset(int offsetX, int offsetY);
    int getOffsetX();
    int getOffsetY();

    /**
     * Enables animation on the particle. Must set a custom width and height first using the setSize(width,height) function.
     * Every frame of animation the particle goes through should be saved on a single image file.
     *
     * The renderer will read the frames of animation based on the given width and height of the particle, starting from the given X & Y offsets.
     * The renderer then goes rightwards (width) pixels for every frame. Once it can't go rightwards anymore,
     * the animation goes down (height) pixels, and once again keeps going rightwards if it has to.
     *
     * If the animation loops, once there is nowhere downward to go, the animation will start over at the X & Y offset.
     *
     * @param animRate The frame rate of the particle's animation, in MC ticks. (20 ticks = 1 second)
     * @param animLoop Whether the animation loops or not.
     * @param animStart The amount of ticks before the particle starts animating.
     * @param animEnd The amount of ticks the particle is around for before it stops animating.
     */
    void setAnim(int animRate, boolean animLoop, int animStart, int animEnd);
    int getAnimRate();
    boolean getAnimLoop();
    int getAnimStart();
    int getAnimEnd();

    /**
     *
     * @param x The X position from the particle's origin the particle will spawn at.
     * @param y The Y position from the particle's origin the particle will spawn at.
     * @param z The Z position from the particle's origin the particle will spawn at.
     */
    void setPosition(double x, double y, double z);
    double getX();
    double getY();
    double getZ();

    void setPosition(IPos pos);
    void getPos();

    void setMotion(double motionX, double motionY, double motionZ, float gravity);
    double getMotionX();
    double getMotionY();
    double getMotionZ();
    float getGravity();

    /**
     *
     * @param HEXColor The starting HEX color of the particle.
     * @param HEXColor2 The ending HEX color of the particle.
     * @param HEXColorRate The rate of interpolation between the two HEX colors.
     * @param HEXColorStart The amount of ticks the particle is around for before its colors begin interpolating between HEXColor and HEXColor2.
     */
    void setHEXColor(int HEXColor, int HEXColor2, float HEXColorRate, int HEXColorStart);
    int getHEXColor1();
    int getHEXColor2();
    float getHEXColorRate();
    int getHEXColorStart();

    /**
     *
     * @param alpha1 The starting transparency of the particle.
     * @param alpha2 The ending transparency of the particle.
     * @param alphaRate The rate of interpolation between the two transparency values.
     * @param alphaRateStart The amount of ticks the particle is around for before its colors begin interpolating between alpha1 and alpha2.
     */
    void setAlpha(float alpha1, float alpha2, float alphaRate, int alphaRateStart);
    float getAlpha1();
    float getAlpha2();
    float getAlphaRate();
    int getAlphaRateStart();

    /**
     *
     * @param scale1 The starting scale/size of the particle.
     * @param scale2 The ending scale/size of the particle.
     * @param scaleRate The rate of interpolation between the two scale values.
     * @param scaleRateStart The amount of ticks the particle is around for before its colors begin interpolating between scale1 and scale2.
     */
    void setScale(float scale1, float scale2, float scaleRate, int scaleRateStart);
    float getScale1();
    float getScale2();
    float getScaleRate();
    int getScaleRateStart();

    /**
     * Enables rotation about the X axis.
     * If the particle is set to always face the player, this axis is a constant horizontal line on the player's camera.
     *
     * @param rotationX1 The starting X rotation of the particle.
     * @param rotationX2 The ending X rotation of the particle.
     * @param rotationXRate The rate of interpolation between the two X rotation values.
     * @param rotationXRateStart The amount of ticks the particle is around for before its colors begin interpolating between rotationX1 and rotationX2.
     */
    void setRotationX(float rotationX1, float rotationX2, float rotationXRate, int rotationXRateStart);
    float getRotationX1();
    float getRotationX2();
    float getRotationXRate();
    int getRotationXRateStart();

    /**
     * Enables rotation about the Y axis.
     * If the particle is set to always face the player, this axis is a constant vertical line on the player's camera.
     *
     * @param rotationY1 The starting Y rotation of the particle.
     * @param rotationY2 The ending Y rotation of the particle.
     * @param rotationYRate The rate of interpolation between the two Y rotation values.
     * @param rotationYRateStart The amount of ticks the particle is around for before its colors begin interpolating between rotationY1 and rotationY2.
     */
    void setRotationY(float rotationY1, float rotationY2, float rotationYRate, int rotationYRateStart);
    float getRotationY1();
    float getRotationY2();
    float getRotationYRate();
    int getRotationYRateStart();

    /**
     * Enables rotation about the Y axis.
     * If the particle is set to always face the player, this axis is a constant line going into the player's camera.
     * You'll be using this one most often if the particle is set to face the player.
     *
     * @param rotationZ1 The starting Z rotation of the particle.
     * @param rotationZ2 The ending Z rotation of the particle.
     * @param rotationZRate The rate of interpolation between the two Z rotation values.
     * @param rotationZRateStart The amount of ticks the particle is around for before its colors begin interpolating between rotationZ1 and rotationZ2.
     */
    void setRotationZ(float rotationZ1, float rotationZ2, float rotationZRate, int rotationZRateStart);
    float getRotationZ1();
    float getRotationZ2();
    float getRotationZRate();
    int getRotationZRateStart();
    
}
