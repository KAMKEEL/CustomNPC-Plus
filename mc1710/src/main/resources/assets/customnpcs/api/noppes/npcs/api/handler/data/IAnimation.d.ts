/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * @javaFqn noppes.npcs.api.handler.data.IAnimation
 */
export interface IAnimation {
    getParent(): import('./IAnimationData').IAnimationData;
    currentFrame(): import('./IFrame').IFrame;
    getFrames(): import('./IFrame').IFrame[];
    setFrames(frames: import('./IFrame').IFrame[]): import('./IAnimation').IAnimation;
    clearFrames(): import('./IAnimation').IAnimation;
    addFrame(frame: import('./IFrame').IFrame): import('./IAnimation').IAnimation;
    addFrame(index: import('./int').int, frame: import('./IFrame').IFrame): import('./IAnimation').IAnimation;
    removeFrame(frame: import('./IFrame').IFrame): import('./IAnimation').IAnimation;
    setName(name: String): import('./IAnimation').IAnimation;
    getName(): String;
    setSpeed(speed: import('./float').float): import('./IAnimation').IAnimation;
    getSpeed(): import('./float').float;
    setSmooth(smooth: import('./byte').byte): import('./IAnimation').IAnimation;
    isSmooth(): import('./byte').byte;
    doWhileStanding(whileStanding: import('./boolean').boolean): import('./IAnimation').IAnimation;
    doWhileStanding(): import('./boolean').boolean;
    doWhileMoving(whileMoving: import('./boolean').boolean): import('./IAnimation').IAnimation;
    doWhileMoving(): import('./boolean').boolean;
    doWhileAttacking(whileAttacking: import('./boolean').boolean): import('./IAnimation').IAnimation;
    doWhileAttacking(): import('./boolean').boolean;
    setLoop(loopAtFrame: import('./int').int): import('./IAnimation').IAnimation;
    loop(): import('./int').int;
    save(): import('./IAnimation').IAnimation;
    getID(): import('./int').int;
    /**
     * Do not use this unless you know what you are changing. Dangerous to change.
     *
     * @param id the animation ID
     */
    setID(id: import('./int').int): import('./void').void;
    getTotalTime(): import('./long').long;
    hasData(key: String): import('./boolean').boolean;
    getData(key: String): Object;
    setData(key: String, v: Object): import('./IAnimation').IAnimation;
    removeData(key: String): import('./IAnimation').IAnimation;
    onStart(task: Java.java.util.function.Consumer<import('./IAnimation').IAnimation>): import('./IAnimation').IAnimation;
    onFrame(task: Java.java.util.function.BiConsumer<Integer, import('./IAnimation').IAnimation>): import('./IAnimation').IAnimation;
    onEnd(task: Java.java.util.function.Consumer<import('./IAnimation').IAnimation>): import('./IAnimation').IAnimation;
    parent: import('./AnimationData').AnimationData;
    id: import('./int').int;
    frames: import('./Frame').Frame[];
    currentFrame: import('./int').int;
    currentFrameTime: import('./int').int;
    name: String;
    speed: import('./float').float;
    smooth: import('./byte').byte;
    loop: import('./int').int;
    whileStanding: import('./boolean').boolean;
    whileAttacking: import('./boolean').boolean;
    whileMoving: import('./boolean').boolean;
    paused: import('./boolean').boolean;
    onAnimationStart: Java.java.util.function.Consumer<import('./IAnimation').IAnimation>;
    onAnimationFrame: Java.java.util.function.BiConsumer<Integer, import('./IAnimation').IAnimation>;
    onAnimationEnd: Java.java.util.function.Consumer<import('./IAnimation').IAnimation>;
}
