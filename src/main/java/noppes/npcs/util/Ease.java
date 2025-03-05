package noppes.npcs.util;

public enum Ease {
    LINEAR(0, f -> f), CONSTANT(1, f -> 0f),
    INSINE(6, Easing::inSine), OUTSINE(7, Easing::outSine), INOUTSINE(8, Easing::inOutSine),
    INCUBIC(9, Easing::inCubic), OUTCUBIC(10, Easing::outCubic), INOUTCUBIC(11, Easing::inOutCubic),
    INQUAD(12, Easing::inQuad), OUTQUAD(13, Easing::outQuad), INOUTQUAD(14, Easing::inOutQuad),
    INQUART(15, Easing::inQuart), OUTQUART(16, Easing::outQuart), INOUTQUART(17, Easing::inOutQuart),
    INQUINT(18, Easing::inQuint), OUTQUINT(19, Easing::outQuint), INOUTQUINT(20, Easing::inOutQuint),
    INEXPO(21, Easing::inExpo), OUTEXPO(22, Easing::outExpo), INOUTEXPO(23, Easing::inOutExpo),
    INCIRC(24, Easing::inCirc), OUTCIRC(25, Easing::outCirc), INOUTCIRC(26, Easing::inOutCirc),
    INBACK(27, Easing::inBack), OUTBACK(28, Easing::outBack), INOUTBACK(29, Easing::inOutBack),
    INELASTIC(30, Easing::inElastic), OUTELASTIC(31, Easing::outElastic), INOUTELASTIC(32, Easing::inOutElastic),
    INBOUNCE(33, Easing::inBounce), OUTBOUNCE(34, Easing::outBack), INOUTBOUNCE(35, Easing::inOutBounce);

    final byte id;
    private final EasingFunction impl;

    Ease(byte id, EasingFunction impl){
        this.id = id;
        this.impl = impl;
    }

    Ease(int id, EasingFunction impl) {
        this((byte) id, impl);
    }

    /**
     * Apply the easing
     * @param f float between 0 and 1
     * @return ease(f)
     */
    public float apply(float f) {
        return impl.invoke(f);
    }

    //To be able to send these as bytes instead of String names.
    public static Ease getEase(byte b){
        for(Ease ease:Ease.values()){
            if(ease.id == b) return ease;
        }
        return LINEAR;
    }

    private interface EasingFunction {
        float invoke(float f);
    }
}
