package noppes.npcs.util;

public enum Ease {
    LINEAR(0, f -> f, "ease."), CONSTANT(1, f -> 0f, "ease."),
    INSINE(2, Easing::inSine, "ease."), OUTSINE(3, Easing::outSine, "ease."), INOUTSINE(4, Easing::inOutSine, "ease."),
    INCUBIC(5, Easing::inCubic, "ease."), OUTCUBIC(6, Easing::outCubic, "ease."), INOUTCUBIC(7, Easing::inOutCubic, "ease."),
    INQUAD(8, Easing::inQuad, "ease."), OUTQUAD(9, Easing::outQuad, "ease."), INOUTQUAD(10, Easing::inOutQuad, "ease."),
    INQUART(11, Easing::inQuart, "ease."), OUTQUART(12, Easing::outQuart, "ease."), INOUTQUART(13, Easing::inOutQuart, "ease."),
    INQUINT(14, Easing::inQuint, "ease."), OUTQUINT(15, Easing::outQuint, "ease."), INOUTQUINT(16, Easing::inOutQuint, "ease."),
    INEXPO(17, Easing::inExpo, "ease."), OUTEXPO(18, Easing::outExpo, "ease."), INOUTEXPO(19, Easing::inOutExpo, "ease."),
    INCIRC(20, Easing::inCirc, "ease."), OUTCIRC(21, Easing::outCirc, "ease."), INOUTCIRC(22, Easing::inOutCirc, "ease."),
    INBACK(23, Easing::inBack, "ease."), OUTBACK(24, Easing::outBack, "ease."), INOUTBACK(25, Easing::inOutBack, "ease."),
    INELASTIC(26, Easing::inElastic, "ease."), OUTELASTIC(27, Easing::outElastic, "ease."), INOUTELASTIC(28, Easing::inOutElastic, "ease."),
    INBOUNCE(29, Easing::inBounce, "ease."), OUTBOUNCE(30, Easing::outBack, "ease."), INOUTBOUNCE(31, Easing::inOutBounce, "ease.");

    final byte id;
    private final EasingFunction impl;
    public String lang;

    Ease(byte id, EasingFunction impl, String lang){
        this.id = id;
        this.impl = impl;
    }

    Ease(int id, EasingFunction impl, String lang) {
        this((byte) id, impl, "ease.");
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

    public static Ease getFromOld(byte smoothing) {
        switch (smoothing) {
            case 1:
                return LINEAR;
            case 2:
                return CONSTANT;
            default:
                return INSINE;
        }
    }

        private interface EasingFunction {
        float invoke(float f);
    }
}
