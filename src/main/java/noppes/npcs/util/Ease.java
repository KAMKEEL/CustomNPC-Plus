package noppes.npcs.util;

public enum Ease {
    LINEAR(0, f -> f), CONSTANT(1, f -> 0f),
    INSINE(2, Easing::inSine), OUTSINE(3, Easing::outSine), INOUTSINE(4, Easing::inOutSine),
    INCUBIC(5, Easing::inCubic), OUTCUBIC(6, Easing::outCubic), INOUTCUBIC(7, Easing::inOutCubic),
    INQUAD(8, Easing::inQuad), OUTQUAD(9, Easing::outQuad), INOUTQUAD(10, Easing::inOutQuad),
    INQUART(11, Easing::inQuart), OUTQUART(12, Easing::outQuart), INOUTQUART(13, Easing::inOutQuart),
    INQUINT(14, Easing::inQuint), OUTQUINT(15, Easing::outQuint), INOUTQUINT(16, Easing::inOutQuint),
    INEXPO(17, Easing::inExpo), OUTEXPO(18, Easing::outExpo), INOUTEXPO(19, Easing::inOutExpo),
    INCIRC(20, Easing::inCirc), OUTCIRC(21, Easing::outCirc), INOUTCIRC(22, Easing::inOutCirc),
    INBACK(23, Easing::inBack), OUTBACK(24, Easing::outBack), INOUTBACK(25, Easing::inOutBack),
    INELASTIC(26, Easing::inElastic), OUTELASTIC(27, Easing::outElastic), INOUTELASTIC(28, Easing::inOutElastic),
    INBOUNCE(29, Easing::inBounce), OUTBOUNCE(30, Easing::outBack), INOUTBOUNCE(31, Easing::inOutBounce);

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
