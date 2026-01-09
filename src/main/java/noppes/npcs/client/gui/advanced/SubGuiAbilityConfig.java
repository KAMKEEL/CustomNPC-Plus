package noppes.npcs.client.gui.advanced;

import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AbilityController;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.ability.telegraph.TelegraphType;
import kamkeel.npcs.controllers.data.ability.type.*;
import noppes.npcs.client.gui.SubGuiColorSelector;
import noppes.npcs.client.gui.select.GuiAnimationSelection;
import noppes.npcs.client.gui.select.GuiSoundSelection;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.gui.util.*;

/**
 * SubGui for editing ability configuration with tabbed interface.
 * Tabs: General, Type (if has type settings), Timing, Effects, Telegraph (if supported)
 */
public class SubGuiAbilityConfig extends SubGuiInterface implements ITextfieldListener, ISubGuiListener {

    private static final int TAB_GENERAL = 0;
    private static final int TAB_TYPE = 1;
    private static final int TAB_TIMING = 2;
    private static final int TAB_EFFECTS = 3;
    private static final int TAB_TELEGRAPH = 4;

    private final Ability ability;
    private final IAbilityConfigCallback parent;
    private int activeTab = TAB_GENERAL;

    // Cached values - General
    private String name;
    private boolean enabled;
    private int weight;
    private float minRange;
    private float maxRange;
    private TargetingMode targetingMode;

    // Cached values - Timing
    private int cooldownTicks;
    private int windUpTicks;
    private int activeTicks;
    private int recoveryTicks;
    private boolean interruptible;
    private float interruptThreshold;
    private boolean lockMovement;

    // Cached values - Telegraph
    private boolean showTelegraph;
    private int windUpColor;
    private int activeColor;
    private int editingColorId = 0; // Track which color button was clicked (22=windUp, 24=active)

    // Cached values - Effects (sounds/animations)
    private String windUpSound;
    private String activeSound;
    private int windUpAnimationId;
    private int activeAnimationId;
    private String windUpAnimationName = null;  // Cached name from selector
    private String activeAnimationName = null;  // Cached name from selector
    private int editingSoundId = 0;      // Track which sound button was clicked (30=windUp, 31=active)
    private int editingAnimationId = 0;  // Track which animation button was clicked (32=windUp, 33=active)

    // Ability type features
    private final boolean supportsTelegraph;
    private final boolean targetingModeLocked;
    private final boolean hasTypeSettings;

    public SubGuiAbilityConfig(Ability ability, IAbilityConfigCallback parent) {
        this.ability = ability;
        this.parent = parent;

        // Cache values from ability
        this.name = ability.getName() != null ? ability.getName() : "";
        this.enabled = ability.isEnabled();
        this.weight = ability.getWeight();
        this.minRange = ability.getMinRange();
        this.maxRange = ability.getMaxRange();
        this.targetingMode = ability.getTargetingMode();

        this.cooldownTicks = ability.getCooldownTicks();
        this.windUpTicks = ability.getWindUpTicks();
        this.activeTicks = ability.getActiveTicks();
        this.recoveryTicks = ability.getRecoveryTicks();
        this.interruptible = ability.isInterruptible();
        this.interruptThreshold = ability.getInterruptThreshold();
        this.lockMovement = ability.isLockMovement();

        this.showTelegraph = ability.isShowTelegraph();
        this.windUpColor = ability.getWindUpColor();
        this.activeColor = ability.getActiveColor();

        this.windUpSound = ability.getWindUpSound();
        this.activeSound = ability.getActiveSound();
        this.windUpAnimationId = ability.getWindUpAnimationId();
        this.activeAnimationId = ability.getActiveAnimationId();

        // Determine ability type features
        TelegraphType defaultType = ability.getTelegraphType();
        this.supportsTelegraph = defaultType != null && defaultType != TelegraphType.NONE;
        this.targetingModeLocked = ability.isTargetingModeLocked();
        this.hasTypeSettings = ability.hasTypeSettings();

        setBackground("menubg.png");
        xSize = 276;
        ySize = 216;
    }

    @Override
    public void initGui() {
        super.initGui();

        int y = guiTop + 4;
        int tabWidth = 60;
        int tabX = guiLeft + 4;

        // Tab buttons at top - dynamically sized based on available tabs
        addButton(new GuiNpcButton(90, tabX, y, tabWidth, 20, "ability.tab.general"));
        tabX += tabWidth + 2;

        if (hasTypeSettings) {
            addButton(new GuiNpcButton(91, tabX, y, tabWidth, 20, "ability.tab.type"));
            tabX += tabWidth + 2;
        }

        addButton(new GuiNpcButton(92, tabX, y, tabWidth, 20, "ability.tab.timing"));
        tabX += tabWidth + 2;

        addButton(new GuiNpcButton(94, tabX, y, tabWidth, 20, "ability.tab.effects"));
        tabX += tabWidth + 2;

        if (supportsTelegraph) {
            addButton(new GuiNpcButton(93, tabX, y, tabWidth, 20, "ability.tab.telegraph"));
        }

        // Highlight active tab
        getButton(90).setEnabled(activeTab != TAB_GENERAL);
        if (hasTypeSettings) {
            getButton(91).setEnabled(activeTab != TAB_TYPE);
        }
        getButton(92).setEnabled(activeTab != TAB_TIMING);
        getButton(94).setEnabled(activeTab != TAB_EFFECTS);
        if (supportsTelegraph) {
            getButton(93).setEnabled(activeTab != TAB_TELEGRAPH);
        }

        y += 24;

        // Draw content based on active tab
        switch (activeTab) {
            case TAB_GENERAL:
                initGeneralTab(y);
                break;
            case TAB_TYPE:
                initTypeTab(y);
                break;
            case TAB_TIMING:
                initTimingTab(y);
                break;
            case TAB_EFFECTS:
                initEffectsTab(y);
                break;
            case TAB_TELEGRAPH:
                initTelegraphTab(y);
                break;
        }

        // Bottom row - Enabled on left, Done/Cancel on right
        addLabel(new GuiNpcLabel(99, "gui.enabled", guiLeft + 8, guiTop + ySize - 19));
        GuiNpcButton enabledBtn = new GuiNpcButton(2, guiLeft + 55, guiTop + ySize - 24, 50, 20, new String[]{"gui.no", "gui.yes"}, enabled ? 1 : 0);
        enabledBtn.setTextColor(enabled ? 0x00FF00 : 0xFF0000);
        addButton(enabledBtn);

        addButton(new GuiNpcButton(66, guiLeft + xSize - 130, guiTop + ySize - 24, 60, 20, "gui.done"));
        addButton(new GuiNpcButton(67, guiLeft + xSize - 65, guiTop + ySize - 24, 60, 20, "gui.cancel"));
    }

    private void initGeneralTab(int startY) {
        int y = startY;
        int labelX = guiLeft + 8;
        int fieldX = guiLeft + 85;
        int col2LabelX = guiLeft + 145;
        int col2FieldX = guiLeft + 205;

        // Row 1: Name + Weight
        addLabel(new GuiNpcLabel(1, "gui.name", labelX, y + 5));
        addTextField(new GuiNpcTextField(1, this, fontRendererObj, fieldX, y, 100, 20, name));

        addLabel(new GuiNpcLabel(3, "ability.weight", col2LabelX, y + 5));
        GuiNpcTextField weightField = new GuiNpcTextField(3, this, fontRendererObj, col2FieldX, y, 50, 20, String.valueOf(weight));
        weightField.setIntegersOnly();
        weightField.setMinMaxDefault(1, 1000, 10);
        addTextField(weightField);

        y += 24;

        // Row 2: Min Range + Max Range
        addLabel(new GuiNpcLabel(5, "ability.minRange", labelX, y + 5));
        GuiNpcTextField minRangeField = new GuiNpcTextField(5, this, fontRendererObj, fieldX, y, 50, 20, String.valueOf((int) minRange));
        minRangeField.setIntegersOnly();
        minRangeField.setMinMaxDefault(0, 100, 0);
        addTextField(minRangeField);

        addLabel(new GuiNpcLabel(6, "ability.maxRange", col2LabelX, y + 5));
        GuiNpcTextField maxRangeField = new GuiNpcTextField(6, this, fontRendererObj, col2FieldX, y, 50, 20, String.valueOf((int) maxRange));
        maxRangeField.setIntegersOnly();
        maxRangeField.setMinMaxDefault(1, 100, 20);
        addTextField(maxRangeField);

        y += 24;

        // Row 3: Targeting Mode (if not locked)
        if (!targetingModeLocked) {
            addLabel(new GuiNpcLabel(4, "ability.targeting", labelX, y + 5));
            String[] targetingModes = getAvailableTargetingModes();
            int selectedIndex = getTargetingModeIndex(targetingMode);
            addButton(new GuiNpcButton(4, fieldX, y, 80, 20, targetingModes, selectedIndex));
        }
    }

    /**
     * Get available targeting modes for the current ability.
     * Some abilities restrict which modes can be used.
     */
    private String[] getAvailableTargetingModes() {
        TargetingMode[] modes = ability.getAllowedTargetingModes();
        if (modes == null) {
            modes = TargetingMode.values();
        }
        String[] result = new String[modes.length];
        for (int i = 0; i < modes.length; i++) {
            result[i] = "ability.target." + modes[i].name().toLowerCase();
        }
        return result;
    }

    /**
     * Get the index of current targeting mode in the allowed modes array.
     */
    private int getTargetingModeIndex(TargetingMode mode) {
        TargetingMode[] modes = ability.getAllowedTargetingModes();
        if (modes == null) {
            return mode.ordinal();
        }
        for (int i = 0; i < modes.length; i++) {
            if (modes[i] == mode) return i;
        }
        return 0;
    }

    private void initTypeTab(int startY) {
        // Delegate to type-specific rendering
        if (ability instanceof AbilitySlam) {
            initSlamSettings(startY, (AbilitySlam) ability);
        } else if (ability instanceof AbilityCharge) {
            initChargeSettings(startY, (AbilityCharge) ability);
        } else if (ability instanceof AbilityHeavyHit) {
            initHeavyHitSettings(startY, (AbilityHeavyHit) ability);
        } else if (ability instanceof AbilityCutter) {
            initCutterSettings(startY, (AbilityCutter) ability);
        } else if (ability instanceof AbilityOrb) {
            initOrbSettings(startY, (AbilityOrb) ability);
        } else if (ability instanceof AbilityVortex) {
            initVortexSettings(startY, (AbilityVortex) ability);
        } else if (ability instanceof AbilityShockwave) {
            initShockwaveSettings(startY, (AbilityShockwave) ability);
        } else if (ability instanceof AbilityDash) {
            initDashSettings(startY, (AbilityDash) ability);
        } else if (ability instanceof AbilityTeleport) {
            initTeleportSettings(startY, (AbilityTeleport) ability);
        } else if (ability instanceof AbilityProjectile) {
            initProjectileSettings(startY, (AbilityProjectile) ability);
        } else if (ability instanceof AbilityBeam) {
            initBeamSettings(startY, (AbilityBeam) ability);
        } else if (ability instanceof AbilityGuard) {
            initGuardSettings(startY, (AbilityGuard) ability);
        } else if (ability instanceof AbilityHeal) {
            initHealSettings(startY, (AbilityHeal) ability);
        } else if (ability instanceof AbilityHazard) {
            initHazardSettings(startY, (AbilityHazard) ability);
        } else if (ability instanceof AbilityTrap) {
            initTrapSettings(startY, (AbilityTrap) ability);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TYPE-SPECIFIC SETTINGS
    // ═══════════════════════════════════════════════════════════════════════════

    private void initSlamSettings(int startY, AbilitySlam slam) {
        int y = startY;
        int labelX = guiLeft + 8;
        int fieldX = guiLeft + 85;
        int col2LabelX = guiLeft + 145;
        int col2FieldX = guiLeft + 205;

        // Row 1: Damage + Radius
        addLabel(new GuiNpcLabel(100, "ability.damage", labelX, y + 5));
        addTextField(createFloatField(100, fieldX, y, 50, slam.getDamage()));

        addLabel(new GuiNpcLabel(101, "ability.radius", col2LabelX, y + 5));
        addTextField(createFloatField(101, col2FieldX, y, 50, slam.getRadius()));

        y += 24;

        // Row 2: Knockback + Leap Speed
        addLabel(new GuiNpcLabel(102, "ability.knockback", labelX, y + 5));
        addTextField(createFloatField(102, fieldX, y, 50, slam.getKnockbackStrength()));

        addLabel(new GuiNpcLabel(103, "ability.leapSpeed", col2LabelX, y + 5));
        addTextField(createFloatField(103, col2FieldX, y, 50, slam.getLeapSpeed()));

        y += 24;

        // Row 3: Min/Max Leap Distance
        addLabel(new GuiNpcLabel(104, "ability.minDist", labelX, y + 5));
        addTextField(createFloatField(104, fieldX, y, 50, slam.getMinLeapDistance()));

        addLabel(new GuiNpcLabel(105, "ability.maxDist", col2LabelX, y + 5));
        addTextField(createFloatField(105, col2FieldX, y, 50, slam.getMaxLeapDistance()));
    }

    private void initChargeSettings(int startY, AbilityCharge charge) {
        int y = startY;
        int labelX = guiLeft + 8;
        int fieldX = guiLeft + 85;
        int col2LabelX = guiLeft + 145;
        int col2FieldX = guiLeft + 205;

        // Row 1: Damage + Speed
        addLabel(new GuiNpcLabel(100, "ability.damage", labelX, y + 5));
        addTextField(createFloatField(100, fieldX, y, 50, charge.getDamage()));

        addLabel(new GuiNpcLabel(101, "ability.speed", col2LabelX, y + 5));
        addTextField(createFloatField(101, col2FieldX, y, 50, charge.getChargeSpeed()));

        y += 24;

        // Row 2: Knockback + Knockback Up
        addLabel(new GuiNpcLabel(102, "ability.knockback", labelX, y + 5));
        addTextField(createFloatField(102, fieldX, y, 50, charge.getKnockback()));

        addLabel(new GuiNpcLabel(103, "ability.knockbackUp", col2LabelX, y + 5));
        addTextField(createFloatField(103, col2FieldX, y, 50, charge.getKnockbackUp()));

        y += 24;

        // Row 3: Max Distance + Hit Radius
        addLabel(new GuiNpcLabel(104, "ability.maxDist", labelX, y + 5));
        addTextField(createFloatField(104, fieldX, y, 50, charge.getMaxDistance()));

        addLabel(new GuiNpcLabel(105, "ability.hitRadius", col2LabelX, y + 5));
        addTextField(createFloatField(105, col2FieldX, y, 50, charge.getHitRadius()));
    }

    private void initHeavyHitSettings(int startY, AbilityHeavyHit hit) {
        int y = startY;
        int labelX = guiLeft + 8;
        int fieldX = guiLeft + 85;
        int col2LabelX = guiLeft + 145;
        int col2FieldX = guiLeft + 205;

        // Row 1: Damage + Knockback
        addLabel(new GuiNpcLabel(100, "ability.damage", labelX, y + 5));
        addTextField(createFloatField(100, fieldX, y, 50, hit.getDamage()));

        addLabel(new GuiNpcLabel(101, "ability.knockback", col2LabelX, y + 5));
        addTextField(createFloatField(101, col2FieldX, y, 50, hit.getKnockback()));

        y += 24;

        // Row 2: Knockback Up + Stun Ticks
        addLabel(new GuiNpcLabel(102, "ability.knockbackUp", labelX, y + 5));
        addTextField(createFloatField(102, fieldX, y, 50, hit.getKnockbackUp()));

        addLabel(new GuiNpcLabel(103, "ability.stunTicks", col2LabelX, y + 5));
        addTextField(createIntField(103, col2FieldX, y, 50, hit.getStunTicks()));
    }

    private void initCutterSettings(int startY, AbilityCutter cutter) {
        int y = startY;
        int labelX = guiLeft + 8;
        int fieldX = guiLeft + 85;
        int col2LabelX = guiLeft + 145;
        int col2FieldX = guiLeft + 205;

        // Row 1: Arc Angle + Range
        addLabel(new GuiNpcLabel(100, "ability.arcAngle", labelX, y + 5));
        addTextField(createFloatField(100, fieldX, y, 50, cutter.getArcAngle()));

        addLabel(new GuiNpcLabel(101, "ability.range", col2LabelX, y + 5));
        addTextField(createFloatField(101, col2FieldX, y, 50, cutter.getRange()));

        y += 24;

        // Row 2: Damage + Knockback
        addLabel(new GuiNpcLabel(102, "ability.damage", labelX, y + 5));
        addTextField(createFloatField(102, fieldX, y, 50, cutter.getDamage()));

        addLabel(new GuiNpcLabel(103, "ability.knockback", col2LabelX, y + 5));
        addTextField(createFloatField(103, col2FieldX, y, 50, cutter.getKnockback()));

        y += 24;

        // Row 3: Sweep Waves + Wave Interval
        addLabel(new GuiNpcLabel(104, "ability.sweepWaves", labelX, y + 5));
        addTextField(createIntField(104, fieldX, y, 50, cutter.getSweepWaves()));

        addLabel(new GuiNpcLabel(105, "ability.waveInterval", col2LabelX, y + 5));
        addTextField(createIntField(105, col2FieldX, y, 50, cutter.getWaveInterval()));

        y += 24;

        // Row 4: Rotation Speed + Piercing
        addLabel(new GuiNpcLabel(106, "ability.rotSpeed", labelX, y + 5));
        addTextField(createFloatField(106, fieldX, y, 50, cutter.getRotationSpeed()));

        addLabel(new GuiNpcLabel(107, "ability.piercing", col2LabelX, y + 5));
        addButton(new GuiNpcButton(107, col2FieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, cutter.isPiercing() ? 1 : 0));
    }

    private void initOrbSettings(int startY, AbilityOrb orb) {
        int y = startY;
        int labelX = guiLeft + 8;
        int fieldX = guiLeft + 85;
        int col2LabelX = guiLeft + 145;
        int col2FieldX = guiLeft + 205;

        // Row 1: Speed + Size
        addLabel(new GuiNpcLabel(100, "ability.orbSpeed", labelX, y + 5));
        addTextField(createFloatField(100, fieldX, y, 50, orb.getOrbSpeed()));

        addLabel(new GuiNpcLabel(101, "ability.orbSize", col2LabelX, y + 5));
        addTextField(createFloatField(101, col2FieldX, y, 50, orb.getOrbSize()));

        y += 24;

        // Row 2: Damage + Knockback
        addLabel(new GuiNpcLabel(102, "ability.damage", labelX, y + 5));
        addTextField(createFloatField(102, fieldX, y, 50, orb.getDamage()));

        addLabel(new GuiNpcLabel(103, "ability.knockback", col2LabelX, y + 5));
        addTextField(createFloatField(103, col2FieldX, y, 50, orb.getKnockback()));

        y += 24;

        // Row 3: Max Distance + Max Lifetime
        addLabel(new GuiNpcLabel(104, "ability.maxDist", labelX, y + 5));
        addTextField(createFloatField(104, fieldX, y, 50, orb.getMaxDistance()));

        addLabel(new GuiNpcLabel(105, "ability.lifetime", col2LabelX, y + 5));
        addTextField(createIntField(105, col2FieldX, y, 50, orb.getMaxLifetime()));

        y += 24;

        // Row 4: Homing + Explosive
        addLabel(new GuiNpcLabel(106, "ability.homing", labelX, y + 5));
        addButton(new GuiNpcButton(106, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, orb.isHoming() ? 1 : 0));

        addLabel(new GuiNpcLabel(107, "ability.explosive", col2LabelX, y + 5));
        GuiNpcButton explosiveBtn = new GuiNpcButton(107, col2FieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, orb.isExplosive() ? 1 : 0);
        explosiveBtn.setHoverText("ability.explosive.noBlockDamage");
        addButton(explosiveBtn);

        y += 24;

        // Row 5: Explosion Radius (only shown when explosive)
        if (orb.isExplosive()) {
            addLabel(new GuiNpcLabel(108, "ability.explRadius", labelX, y + 5));
            addTextField(createFloatField(108, fieldX, y, 50, orb.getExplosionRadius()));
        }
    }

    private void initVortexSettings(int startY, AbilityVortex pull) {
        int y = startY;
        int labelX = guiLeft + 8;
        int fieldX = guiLeft + 85;
        int col2LabelX = guiLeft + 145;
        int col2FieldX = guiLeft + 205;

        // Row 1: Pull Radius + Pull Strength
        addLabel(new GuiNpcLabel(100, "ability.pullRadius", labelX, y + 5));
        addTextField(createFloatField(100, fieldX, y, 50, pull.getPullRadius()));

        addLabel(new GuiNpcLabel(101, "ability.pullStrength", col2LabelX, y + 5));
        addTextField(createFloatField(101, col2FieldX, y, 50, pull.getPullStrength()));

        y += 24;

        // Row 2: Damage + Pull To Distance
        addLabel(new GuiNpcLabel(102, "ability.damage", labelX, y + 5));
        addTextField(createFloatField(102, fieldX, y, 50, pull.getDamage()));

        addLabel(new GuiNpcLabel(103, "ability.pullTo", col2LabelX, y + 5));
        addTextField(createFloatField(103, col2FieldX, y, 50, pull.getPullToDistance()));

        y += 24;

        // Row 3: Stun Duration + Root Duration
        addLabel(new GuiNpcLabel(104, "ability.stunDur", labelX, y + 5));
        addTextField(createIntField(104, fieldX, y, 50, pull.getStunDuration()));

        addLabel(new GuiNpcLabel(105, "ability.rootDur", col2LabelX, y + 5));
        addTextField(createIntField(105, col2FieldX, y, 50, pull.getRootDuration()));

        y += 24;

        // Row 4: AOE + Max Targets
        addLabel(new GuiNpcLabel(106, "ability.aoe", labelX, y + 5));
        addButton(new GuiNpcButton(106, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, pull.isAoe() ? 1 : 0));

        addLabel(new GuiNpcLabel(107, "ability.maxTargets", col2LabelX, y + 5));
        addTextField(createIntField(107, col2FieldX, y, 50, pull.getMaxTargets()));
    }

    private void initShockwaveSettings(int startY, AbilityShockwave shock) {
        int y = startY;
        int labelX = guiLeft + 8;
        int fieldX = guiLeft + 85;
        int col2LabelX = guiLeft + 145;
        int col2FieldX = guiLeft + 205;

        // Row 1: Push Radius + Push Strength
        addLabel(new GuiNpcLabel(100, "ability.pushRadius", labelX, y + 5));
        addTextField(createFloatField(100, fieldX, y, 50, shock.getPushRadius()));

        addLabel(new GuiNpcLabel(101, "ability.pushStrength", col2LabelX, y + 5));
        addTextField(createFloatField(101, col2FieldX, y, 50, shock.getPushStrength()));

        y += 24;

        // Row 2: Damage + Push Up
        addLabel(new GuiNpcLabel(102, "ability.damage", labelX, y + 5));
        addTextField(createFloatField(102, fieldX, y, 50, shock.getDamage()));

        addLabel(new GuiNpcLabel(103, "ability.knockbackUp", col2LabelX, y + 5));
        addTextField(createFloatField(103, col2FieldX, y, 50, shock.getPushUp()));

        y += 24;

        // Row 3: Stun Duration + Max Targets
        addLabel(new GuiNpcLabel(104, "ability.stunDur", labelX, y + 5));
        addTextField(createIntField(104, fieldX, y, 50, shock.getStunDuration()));

        addLabel(new GuiNpcLabel(105, "ability.maxTargets", col2LabelX, y + 5));
        addTextField(createIntField(105, col2FieldX, y, 50, shock.getMaxTargets()));
    }

    private void initDashSettings(int startY, AbilityDash dash) {
        int y = startY;
        int labelX = guiLeft + 8;
        int fieldX = guiLeft + 85;
        int col2LabelX = guiLeft + 145;
        int col2FieldX = guiLeft + 205;

        // Row 1: Dash Mode
        addLabel(new GuiNpcLabel(100, "ability.dashMode", labelX, y + 5));
        String[] modes = new String[]{"ability.dash.aggressive", "ability.dash.defensive"};
        addButton(new GuiNpcButton(100, fieldX, y, 90, 20, modes, dash.getDashMode().ordinal()));

        y += 24;

        // Row 2: Distance + Speed
        addLabel(new GuiNpcLabel(101, "ability.distance", labelX, y + 5));
        addTextField(createFloatField(101, fieldX, y, 50, dash.getDashDistance()));

        addLabel(new GuiNpcLabel(102, "ability.speed", col2LabelX, y + 5));
        addTextField(createFloatField(102, col2FieldX, y, 50, dash.getDashSpeed()));
    }

    private void initTeleportSettings(int startY, AbilityTeleport tp) {
        int y = startY;
        int labelX = guiLeft + 8;
        int fieldX = guiLeft + 85;
        int col2LabelX = guiLeft + 145;
        int col2FieldX = guiLeft + 205;

        // Row 1: Blink Count + Blink Delay
        addLabel(new GuiNpcLabel(100, "ability.blinkCount", labelX, y + 5));
        addTextField(createIntField(100, fieldX, y, 50, tp.getBlinkCount()));

        addLabel(new GuiNpcLabel(101, "ability.blinkDelay", col2LabelX, y + 5));
        addTextField(createIntField(101, col2FieldX, y, 50, tp.getBlinkDelayTicks()));

        y += 24;

        // Row 2: Blink Radius + Min Radius
        addLabel(new GuiNpcLabel(102, "ability.blinkRadius", labelX, y + 5));
        addTextField(createFloatField(102, fieldX, y, 50, tp.getBlinkRadius()));

        addLabel(new GuiNpcLabel(103, "ability.minRadius", col2LabelX, y + 5));
        addTextField(createFloatField(103, col2FieldX, y, 50, tp.getMinBlinkRadius()));

        y += 24;

        // Row 3: Damage + Damage Radius
        addLabel(new GuiNpcLabel(104, "ability.damage", labelX, y + 5));
        addTextField(createFloatField(104, fieldX, y, 50, tp.getDamage()));

        addLabel(new GuiNpcLabel(105, "ability.dmgRadius", col2LabelX, y + 5));
        addTextField(createFloatField(105, col2FieldX, y, 50, tp.getDamageRadius()));

        y += 24;

        // Row 4: Line of Sight + Damage At Start
        addLabel(new GuiNpcLabel(106, "ability.lineOfSight", labelX, y + 5));
        addButton(new GuiNpcButton(106, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, tp.isRequireLineOfSight() ? 1 : 0));

        addLabel(new GuiNpcLabel(107, "ability.dmgAtStart", col2LabelX, y + 5));
        addButton(new GuiNpcButton(107, col2FieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, tp.isDamageAtStart() ? 1 : 0));
    }

    private void initProjectileSettings(int startY, AbilityProjectile proj) {
        int y = startY;
        int labelX = guiLeft + 8;
        int fieldX = guiLeft + 85;
        int col2LabelX = guiLeft + 145;
        int col2FieldX = guiLeft + 205;

        // Row 1: Damage + Speed
        addLabel(new GuiNpcLabel(100, "ability.damage", labelX, y + 5));
        addTextField(createFloatField(100, fieldX, y, 50, proj.getDamage()));

        addLabel(new GuiNpcLabel(101, "ability.speed", col2LabelX, y + 5));
        addTextField(createFloatField(101, col2FieldX, y, 50, proj.getSpeed()));

        y += 24;

        // Row 2: Knockback + Explosion Radius
        addLabel(new GuiNpcLabel(102, "ability.knockback", labelX, y + 5));
        addTextField(createFloatField(102, fieldX, y, 50, proj.getKnockback()));

        addLabel(new GuiNpcLabel(103, "ability.explRadius", col2LabelX, y + 5));
        addTextField(createFloatField(103, col2FieldX, y, 50, proj.getExplosionRadius()));

        y += 24;

        // Row 3: Explosive + Homing
        addLabel(new GuiNpcLabel(104, "ability.explosive", labelX, y + 5));
        GuiNpcButton projExplosiveBtn = new GuiNpcButton(104, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, proj.isExplosive() ? 1 : 0);
        projExplosiveBtn.setHoverText("ability.explosive.noBlockDamage");
        addButton(projExplosiveBtn);

        addLabel(new GuiNpcLabel(105, "ability.homing", col2LabelX, y + 5));
        addButton(new GuiNpcButton(105, col2FieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, proj.isHoming() ? 1 : 0));
    }

    private void initBeamSettings(int startY, AbilityBeam beam) {
        int y = startY;
        int labelX = guiLeft + 8;
        int fieldX = guiLeft + 85;
        int col2LabelX = guiLeft + 145;
        int col2FieldX = guiLeft + 205;

        // Row 1: Length + Width
        addLabel(new GuiNpcLabel(100, "ability.beamLength", labelX, y + 5));
        addTextField(createFloatField(100, fieldX, y, 50, beam.getBeamLength()));

        addLabel(new GuiNpcLabel(101, "ability.beamWidth", col2LabelX, y + 5));
        addTextField(createFloatField(101, col2FieldX, y, 50, beam.getBeamWidth()));

        y += 24;

        // Row 2: Damage + Damage Interval
        addLabel(new GuiNpcLabel(102, "ability.damage", labelX, y + 5));
        addTextField(createFloatField(102, fieldX, y, 50, beam.getDamage()));

        addLabel(new GuiNpcLabel(103, "ability.dmgInterval", col2LabelX, y + 5));
        addTextField(createIntField(103, col2FieldX, y, 50, beam.getDamageInterval()));

        y += 24;

        // Row 3: Sweep Angle + Sweep Speed
        addLabel(new GuiNpcLabel(104, "ability.sweepAngle", labelX, y + 5));
        addTextField(createFloatField(104, fieldX, y, 50, beam.getSweepAngle()));

        addLabel(new GuiNpcLabel(105, "ability.sweepSpeed", col2LabelX, y + 5));
        addTextField(createFloatField(105, col2FieldX, y, 50, beam.getSweepSpeed()));

        y += 24;

        // Row 4: Piercing + Lock On Target
        addLabel(new GuiNpcLabel(106, "ability.piercing", labelX, y + 5));
        addButton(new GuiNpcButton(106, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, beam.isPiercing() ? 1 : 0));

        addLabel(new GuiNpcLabel(107, "ability.lockTarget", col2LabelX, y + 5));
        addButton(new GuiNpcButton(107, col2FieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, beam.isLockOnTarget() ? 1 : 0));
    }

    private void initGuardSettings(int startY, AbilityGuard guard) {
        int y = startY;
        int labelX = guiLeft + 8;
        int fieldX = guiLeft + 85;
        int col2LabelX = guiLeft + 145;
        int col2FieldX = guiLeft + 205;

        // Row 1: Damage Reduction + Counter Damage
        addLabel(new GuiNpcLabel(100, "ability.dmgReduce", labelX, y + 5));
        addTextField(createFloatField(100, fieldX, y, 50, guard.getDamageReduction()));

        addLabel(new GuiNpcLabel(101, "ability.counterDmg", col2LabelX, y + 5));
        addTextField(createFloatField(101, col2FieldX, y, 50, guard.getCounterDamage()));

        y += 24;

        // Row 2: Can Counter + Counter Chance
        addLabel(new GuiNpcLabel(102, "ability.canCounter", labelX, y + 5));
        addButton(new GuiNpcButton(102, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, guard.isCanCounter() ? 1 : 0));

        addLabel(new GuiNpcLabel(103, "ability.counterChance", col2LabelX, y + 5));
        addTextField(createFloatField(103, col2FieldX, y, 50, guard.getCounterChance()));
    }

    private void initHealSettings(int startY, AbilityHeal heal) {
        int y = startY;
        int labelX = guiLeft + 8;
        int fieldX = guiLeft + 85;
        int col2LabelX = guiLeft + 145;
        int col2FieldX = guiLeft + 205;

        // Row 1: Heal Amount + Heal Percent
        addLabel(new GuiNpcLabel(100, "ability.healAmount", labelX, y + 5));
        addTextField(createFloatField(100, fieldX, y, 50, heal.getHealAmount()));

        addLabel(new GuiNpcLabel(101, "ability.healPercent", col2LabelX, y + 5));
        addTextField(createFloatField(101, col2FieldX, y, 50, heal.getHealPercent()));

        y += 24;

        // Row 2: Heal Radius + Instant Heal
        addLabel(new GuiNpcLabel(102, "ability.healRadius", labelX, y + 5));
        addTextField(createFloatField(102, fieldX, y, 50, heal.getHealRadius()));

        addLabel(new GuiNpcLabel(103, "ability.instant", col2LabelX, y + 5));
        addButton(new GuiNpcButton(103, col2FieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, heal.isInstantHeal() ? 1 : 0));

        y += 24;

        // Row 3: Heal Self + Heal Allies
        addLabel(new GuiNpcLabel(104, "ability.healSelf", labelX, y + 5));
        addButton(new GuiNpcButton(104, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, heal.isHealSelf() ? 1 : 0));

        addLabel(new GuiNpcLabel(105, "ability.healAllies", col2LabelX, y + 5));
        addButton(new GuiNpcButton(105, col2FieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, heal.isHealAllies() ? 1 : 0));
    }

    private void initHazardSettings(int startY, AbilityHazard hazard) {
        int y = startY;
        int labelX = guiLeft + 8;
        int fieldX = guiLeft + 85;
        int col2LabelX = guiLeft + 145;
        int col2FieldX = guiLeft + 205;

        // Row 1: Radius + Damage Per Tick
        addLabel(new GuiNpcLabel(100, "ability.radius", labelX, y + 5));
        addTextField(createFloatField(100, fieldX, y, 50, hazard.getRadius()));

        addLabel(new GuiNpcLabel(101, "ability.dmgPerTick", col2LabelX, y + 5));
        addTextField(createFloatField(101, col2FieldX, y, 50, hazard.getDamagePerTick()));

        y += 24;

        // Row 2: Damage Interval + Debuff Duration
        addLabel(new GuiNpcLabel(102, "ability.dmgInterval", labelX, y + 5));
        addTextField(createIntField(102, fieldX, y, 50, hazard.getDamageInterval()));

        addLabel(new GuiNpcLabel(103, "ability.debuffDur", col2LabelX, y + 5));
        addTextField(createIntField(103, col2FieldX, y, 50, hazard.getDebuffDuration()));

        y += 24;

        // Row 3: Shape + Placement
        addLabel(new GuiNpcLabel(104, "ability.shape", labelX, y + 5));
        String[] shapes = new String[]{"ability.shape.circle", "ability.shape.ring", "ability.shape.cone"};
        addButton(new GuiNpcButton(104, fieldX, y, 60, 20, shapes, hazard.getShape().ordinal()));

        addLabel(new GuiNpcLabel(105, "ability.placement", col2LabelX, y + 5));
        String[] placements = new String[]{"ability.place.caster", "ability.place.target", "ability.place.followCaster", "ability.place.followTarget"};
        addButton(new GuiNpcButton(105, col2FieldX, y, 60, 20, placements, hazard.getPlacement().ordinal()));

        y += 24;

        // Row 4: Affects Caster + Ignore Invuln Frames
        addLabel(new GuiNpcLabel(106, "ability.affectsCaster", labelX, y + 5));
        addButton(new GuiNpcButton(106, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, hazard.isAffectsCaster() ? 1 : 0));

        addLabel(new GuiNpcLabel(107, "ability.ignoreInvuln", col2LabelX, y + 5));
        addButton(new GuiNpcButton(107, col2FieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, hazard.isIgnoreInvulnFrames() ? 1 : 0));
    }

    private void initTrapSettings(int startY, AbilityTrap trap) {
        int y = startY;
        int labelX = guiLeft + 8;
        int fieldX = guiLeft + 85;
        int col2LabelX = guiLeft + 145;
        int col2FieldX = guiLeft + 205;

        // Row 1: Trigger Radius + Damage
        addLabel(new GuiNpcLabel(100, "ability.triggerRadius", labelX, y + 5));
        addTextField(createFloatField(100, fieldX, y, 50, trap.getTriggerRadius()));

        addLabel(new GuiNpcLabel(101, "ability.damage", col2LabelX, y + 5));
        addTextField(createFloatField(101, col2FieldX, y, 50, trap.getDamage()));

        y += 24;

        // Row 2: Arm Time + Max Triggers
        addLabel(new GuiNpcLabel(102, "ability.armTime", labelX, y + 5));
        addTextField(createIntField(102, fieldX, y, 50, trap.getArmTime()));

        addLabel(new GuiNpcLabel(103, "ability.maxTriggers", col2LabelX, y + 5));
        addTextField(createIntField(103, col2FieldX, y, 50, trap.getMaxTriggers()));

        y += 24;

        // Row 3: Knockback + Root Duration
        addLabel(new GuiNpcLabel(104, "ability.knockback", labelX, y + 5));
        addTextField(createFloatField(104, fieldX, y, 50, trap.getKnockback()));

        addLabel(new GuiNpcLabel(105, "ability.rootDur", col2LabelX, y + 5));
        addTextField(createIntField(105, col2FieldX, y, 50, trap.getRootDuration()));

        y += 24;

        // Row 4: Placement + Visible
        addLabel(new GuiNpcLabel(106, "ability.placement", labelX, y + 5));
        String[] placements = new String[]{"ability.place.caster", "ability.place.target", "ability.place.ahead"};
        addButton(new GuiNpcButton(106, fieldX, y, 60, 20, placements, trap.getPlacement().ordinal()));

        addLabel(new GuiNpcLabel(107, "ability.visible", col2LabelX, y + 5));
        addButton(new GuiNpcButton(107, col2FieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, trap.isVisible() ? 1 : 0));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // HELPER METHODS FOR FIELD CREATION
    // ═══════════════════════════════════════════════════════════════════════════

    private GuiNpcTextField createFloatField(int id, int x, int y, int width, float value) {
        GuiNpcTextField field = new GuiNpcTextField(id, this, fontRendererObj, x, y, width, 20, String.format("%.1f", value));
        return field;
    }

    private GuiNpcTextField createIntField(int id, int x, int y, int width, int value) {
        GuiNpcTextField field = new GuiNpcTextField(id, this, fontRendererObj, x, y, width, 20, String.valueOf(value));
        field.setIntegersOnly();
        return field;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TIMING TAB
    // ═══════════════════════════════════════════════════════════════════════════

    private void initTimingTab(int startY) {
        int y = startY;
        int labelX = guiLeft + 8;
        int fieldX = guiLeft + 85;
        int col2LabelX = guiLeft + 145;
        int col2FieldX = guiLeft + 205;

        // Row 1: Cooldown + Windup
        addLabel(new GuiNpcLabel(10, "ability.cooldown", labelX, y + 5));
        GuiNpcTextField cooldownField = new GuiNpcTextField(10, this, fontRendererObj, fieldX, y, 50, 20, String.valueOf(cooldownTicks));
        cooldownField.setIntegersOnly();
        cooldownField.setMinMaxDefault(0, 10000, 100);
        addTextField(cooldownField);

        addLabel(new GuiNpcLabel(11, "ability.windup", col2LabelX, y + 5));
        GuiNpcTextField windupField = new GuiNpcTextField(11, this, fontRendererObj, col2FieldX, y, 50, 20, String.valueOf(windUpTicks));
        windupField.setIntegersOnly();
        windupField.setMinMaxDefault(0, 1000, 20);
        addTextField(windupField);

        y += 24;

        // Row 2: Active + Recovery
        addLabel(new GuiNpcLabel(12, "ability.active", labelX, y + 5));
        GuiNpcTextField activeField = new GuiNpcTextField(12, this, fontRendererObj, fieldX, y, 50, 20, String.valueOf(activeTicks));
        activeField.setIntegersOnly();
        activeField.setMinMaxDefault(1, 1000, 10);
        addTextField(activeField);

        addLabel(new GuiNpcLabel(13, "ability.recovery", col2LabelX, y + 5));
        GuiNpcTextField recoveryField = new GuiNpcTextField(13, this, fontRendererObj, col2FieldX, y, 50, 20, String.valueOf(recoveryTicks));
        recoveryField.setIntegersOnly();
        recoveryField.setMinMaxDefault(0, 1000, 20);
        addTextField(recoveryField);

        y += 28;

        // Row 3: Interruptible + Threshold
        addLabel(new GuiNpcLabel(14, "ability.interruptible", labelX, y + 5));
        addButton(new GuiNpcButton(14, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, interruptible ? 1 : 0));

        addLabel(new GuiNpcLabel(15, "ability.threshold", col2LabelX, y + 5));
        GuiNpcTextField thresholdField = new GuiNpcTextField(15, this, fontRendererObj, col2FieldX, y, 50, 20, String.valueOf((int) interruptThreshold));
        thresholdField.setIntegersOnly();
        thresholdField.setMinMaxDefault(0, 1000, 10);
        thresholdField.setEnabled(interruptible);
        addTextField(thresholdField);

        y += 28;

        // Row 4: Lock Movement
        addLabel(new GuiNpcLabel(16, "ability.lockMove", labelX, y + 5));
        addButton(new GuiNpcButton(16, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, lockMovement ? 1 : 0));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // EFFECTS TAB (Sounds & Animations)
    // ═══════════════════════════════════════════════════════════════════════════

    private void initEffectsTab(int startY) {
        int y = startY;
        int labelX = guiLeft + 8;
        int fieldX = guiLeft + 100;
        int btnX = guiLeft + 215;

        // Row 1: Wind Up Sound
        addLabel(new GuiNpcLabel(30, "ability.windUpSound", labelX, y + 5));
        String windUpSoundDisplay = windUpSound != null && !windUpSound.isEmpty() ? truncateString(windUpSound, 15) : "None";
        addButton(new GuiNpcButton(30, fieldX, y, 110, 20, windUpSoundDisplay));
        addButton(new GuiNpcButton(35, btnX, y, 20, 20, "X"));

        y += 25;

        // Row 2: Active Sound
        addLabel(new GuiNpcLabel(31, "ability.activeSound", labelX, y + 5));
        String activeSoundDisplay = activeSound != null && !activeSound.isEmpty() ? truncateString(activeSound, 15) : "None";
        addButton(new GuiNpcButton(31, fieldX, y, 110, 20, activeSoundDisplay));
        addButton(new GuiNpcButton(36, btnX, y, 20, 20, "X"));

        y += 30;

        // Row 3: Wind Up Animation
        addLabel(new GuiNpcLabel(32, "ability.windUpAnim", labelX, y + 5));
        String windUpAnimName = getAnimationName(windUpAnimationId, true);
        addButton(new GuiNpcButton(32, fieldX, y, 110, 20, windUpAnimName));
        addButton(new GuiNpcButton(37, btnX, y, 20, 20, "X"));

        y += 25;

        // Row 4: Active Animation
        addLabel(new GuiNpcLabel(33, "ability.activeAnim", labelX, y + 5));
        String activeAnimName = getAnimationName(activeAnimationId, false);
        addButton(new GuiNpcButton(33, fieldX, y, 110, 20, activeAnimName));
        addButton(new GuiNpcButton(38, btnX, y, 20, 20, "X"));
    }

    private String truncateString(String str, int maxLen) {
        if (str == null) return "";
        if (str.length() <= maxLen) return str;
        // Try to show the end part (more useful for resource paths)
        int lastSlash = str.lastIndexOf('/');
        if (lastSlash >= 0 && str.length() - lastSlash <= maxLen) {
            return "..." + str.substring(lastSlash);
        }
        return "..." + str.substring(str.length() - maxLen + 3);
    }

    private String getAnimationName(int animId, boolean isWindUp) {
        if (animId < 0) return "None";
        // Use cached name if available
        String cachedName = isWindUp ? windUpAnimationName : activeAnimationName;
        if (cachedName != null && !cachedName.isEmpty()) {
            return cachedName;
        }
        return "ID: " + animId;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TELEGRAPH TAB
    // ═══════════════════════════════════════════════════════════════════════════

    private void initTelegraphTab(int startY) {
        int y = startY;
        int labelX = guiLeft + 8;
        int fieldX = guiLeft + 110;

        // Row 1: Show Telegraph
        addLabel(new GuiNpcLabel(20, "ability.showTelegraph", labelX, y + 5));
        addButton(new GuiNpcButton(20, fieldX, y, 50, 20, new String[]{"gui.no", "gui.yes"}, showTelegraph ? 1 : 0));

        y += 25;

        // Row 2: Wind Up Color
        addLabel(new GuiNpcLabel(21, "ability.windUpColor", labelX, y + 5));
        String windUpHex = String.format("%06X", windUpColor & 0xFFFFFF);
        GuiNpcButton windUpBtn = new GuiNpcButton(22, fieldX, y, 70, 20, windUpHex);
        windUpBtn.setEnabled(showTelegraph);
        windUpBtn.setTextColor(windUpColor & 0xFFFFFF);
        addButton(windUpBtn);

        y += 25;

        // Row 3: Active Color
        addLabel(new GuiNpcLabel(23, "ability.activeColor", labelX, y + 5));
        String activeHex = String.format("%06X", activeColor & 0xFFFFFF);
        GuiNpcButton activeBtn = new GuiNpcButton(24, fieldX, y, 70, 20, activeHex);
        activeBtn.setEnabled(showTelegraph);
        activeBtn.setTextColor(activeColor & 0xFFFFFF);
        addButton(activeBtn);

        y += 30;

        // Info: Telegraph type
        String typeKey = "ability.telegraph." + ability.getTelegraphType().name().toLowerCase();
        addLabel(new GuiNpcLabel(25, "ability.telegraphType", labelX, y));
        addLabel(new GuiNpcLabel(26, typeKey, fieldX, y));

        y += 18;

        // Info note
        addLabel(new GuiNpcLabel(27, "ability.telegraphSizeNote", labelX, y));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // BUTTON EVENTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public void buttonEvent(GuiButton guibutton) {
        int id = guibutton.id;

        // Tab switching
        if (id == 90) {
            activeTab = TAB_GENERAL;
            initGui();
            return;
        }
        if (id == 91 && hasTypeSettings) {
            activeTab = TAB_TYPE;
            initGui();
            return;
        }
        if (id == 92) {
            activeTab = TAB_TIMING;
            initGui();
            return;
        }
        if (id == 93 && supportsTelegraph) {
            activeTab = TAB_TELEGRAPH;
            initGui();
            return;
        }
        if (id == 94) {
            activeTab = TAB_EFFECTS;
            initGui();
            return;
        }

        // Enabled button (bottom left)
        if (id == 2) {
            enabled = ((GuiNpcButton) guibutton).getValue() == 1;
            initGui(); // Refresh to update button color
        }
        // Targeting mode
        else if (id == 4) {
            TargetingMode[] modes = ability.getAllowedTargetingModes();
            int idx = ((GuiNpcButton) guibutton).getValue();
            if (modes != null && idx < modes.length) {
                targetingMode = modes[idx];
            } else {
                targetingMode = TargetingMode.values()[idx];
            }
        }

        // Timing tab
        else if (id == 14) {
            interruptible = ((GuiNpcButton) guibutton).getValue() == 1;
            initGui();
        } else if (id == 16) {
            lockMovement = ((GuiNpcButton) guibutton).getValue() == 1;
        }

        // Telegraph tab
        else if (id == 20) {
            showTelegraph = ((GuiNpcButton) guibutton).getValue() == 1;
            initGui();
        }
        // Wind Up color button - open color selector
        else if (id == 22) {
            editingColorId = 22;
            setSubGui(new SubGuiColorSelector(windUpColor));
        }
        // Active color button - open color selector
        else if (id == 24) {
            editingColorId = 24;
            setSubGui(new SubGuiColorSelector(activeColor));
        }

        // Effects tab - Sound buttons
        else if (id == 30) {
            editingSoundId = 30;
            setSubGui(new GuiSoundSelection(windUpSound));
        }
        else if (id == 31) {
            editingSoundId = 31;
            setSubGui(new GuiSoundSelection(activeSound));
        }
        // Sound clear buttons
        else if (id == 35) {
            windUpSound = "";
            initGui();
        }
        else if (id == 36) {
            activeSound = "";
            initGui();
        }
        // Animation buttons
        else if (id == 32) {
            editingAnimationId = 32;
            setSubGui(new GuiAnimationSelection(windUpAnimationId));
        }
        else if (id == 33) {
            editingAnimationId = 33;
            setSubGui(new GuiAnimationSelection(activeAnimationId));
        }
        // Animation clear buttons
        else if (id == 37) {
            windUpAnimationId = -1;
            windUpAnimationName = null;
            initGui();
        }
        else if (id == 38) {
            activeAnimationId = -1;
            activeAnimationName = null;
            initGui();
        }

        // Type-specific buttons (100+)
        else if (id >= 100) {
            handleTypeButton(id, (GuiNpcButton) guibutton);
        }

        // Done/Cancel
        else if (id == 66) {
            applyToAbility();
            parent.onAbilitySaved(ability);
            close();
        } else if (id == 67) {
            close();
        }
    }

    /**
     * Called when an ability is loaded from saved presets.
     */
    public void loadAbility(Ability loadedAbility) {
        if (loadedAbility == null) return;

        // Copy all values from loaded ability to current ability
        NBTTagCompound nbt = loadedAbility.writeNBT();
        // Keep the current typeId since we can't change types mid-edit
        nbt.setString("typeId", ability.getTypeId());
        ability.readNBT(nbt);

        // Update cached values
        this.name = ability.getName() != null ? ability.getName() : "";
        this.enabled = ability.isEnabled();
        this.weight = ability.getWeight();
        this.minRange = ability.getMinRange();
        this.maxRange = ability.getMaxRange();
        this.targetingMode = ability.getTargetingMode();

        this.cooldownTicks = ability.getCooldownTicks();
        this.windUpTicks = ability.getWindUpTicks();
        this.activeTicks = ability.getActiveTicks();
        this.recoveryTicks = ability.getRecoveryTicks();
        this.interruptible = ability.isInterruptible();
        this.interruptThreshold = ability.getInterruptThreshold();
        this.lockMovement = ability.isLockMovement();

        this.showTelegraph = ability.isShowTelegraph();
        this.windUpColor = ability.getWindUpColor();
        this.activeColor = ability.getActiveColor();

        initGui();
    }

    private void handleTypeButton(int id, GuiNpcButton button) {
        int value = button.getValue();

        if (ability instanceof AbilityDash) {
            AbilityDash dash = (AbilityDash) ability;
            if (id == 100) dash.setDashMode(AbilityDash.DashMode.values()[value]);
        } else if (ability instanceof AbilityCutter) {
            AbilityCutter cutter = (AbilityCutter) ability;
            if (id == 107) cutter.setPiercing(value == 1);
        } else if (ability instanceof AbilityOrb) {
            AbilityOrb orb = (AbilityOrb) ability;
            if (id == 106) orb.setHoming(value == 1);
            else if (id == 107) {
                orb.setExplosive(value == 1);
                initGui(); // Refresh to show/hide explosion radius field
            }
        } else if (ability instanceof AbilityVortex) {
            AbilityVortex pull = (AbilityVortex) ability;
            if (id == 106) pull.setAoe(value == 1);
        } else if (ability instanceof AbilityTeleport) {
            AbilityTeleport tp = (AbilityTeleport) ability;
            if (id == 106) tp.setRequireLineOfSight(value == 1);
            else if (id == 107) tp.setDamageAtStart(value == 1);
        } else if (ability instanceof AbilityProjectile) {
            AbilityProjectile proj = (AbilityProjectile) ability;
            if (id == 104) proj.setExplosive(value == 1);
            else if (id == 105) proj.setHoming(value == 1);
        } else if (ability instanceof AbilityBeam) {
            AbilityBeam beam = (AbilityBeam) ability;
            if (id == 106) beam.setPiercing(value == 1);
            else if (id == 107) beam.setLockOnTarget(value == 1);
        } else if (ability instanceof AbilityGuard) {
            AbilityGuard guard = (AbilityGuard) ability;
            if (id == 102) guard.setCanCounter(value == 1);
        } else if (ability instanceof AbilityHeal) {
            AbilityHeal heal = (AbilityHeal) ability;
            if (id == 103) heal.setInstantHeal(value == 1);
            else if (id == 104) heal.setHealSelf(value == 1);
            else if (id == 105) heal.setHealAllies(value == 1);
        } else if (ability instanceof AbilityHazard) {
            AbilityHazard hazard = (AbilityHazard) ability;
            if (id == 104) hazard.setShape(AbilityHazard.HazardShape.values()[value]);
            else if (id == 105) hazard.setPlacement(AbilityHazard.PlacementMode.values()[value]);
            else if (id == 106) hazard.setAffectsCaster(value == 1);
            else if (id == 107) hazard.setIgnoreInvulnFrames(value == 1);
        } else if (ability instanceof AbilityTrap) {
            AbilityTrap trap = (AbilityTrap) ability;
            if (id == 106) trap.setPlacement(AbilityTrap.TrapPlacement.values()[value]);
            else if (id == 107) trap.setVisible(value == 1);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TEXT FIELD EVENTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public void unFocused(GuiNpcTextField textField) {
        int id = textField.id;

        // General tab
        if (id == 1) {
            name = textField.getText();
        } else if (id == 3) {
            weight = textField.getInteger();
        } else if (id == 5) {
            minRange = textField.getInteger();
        } else if (id == 6) {
            maxRange = textField.getInteger();
        }

        // Timing tab
        else if (id == 10) {
            cooldownTicks = textField.getInteger();
        } else if (id == 11) {
            windUpTicks = textField.getInteger();
        } else if (id == 12) {
            activeTicks = textField.getInteger();
        } else if (id == 13) {
            recoveryTicks = textField.getInteger();
        } else if (id == 15) {
            interruptThreshold = textField.getInteger();
        }

        // Type-specific fields (100+)
        else if (id >= 100) {
            handleTypeTextField(id, textField);
        }
    }

    private void handleTypeTextField(int id, GuiNpcTextField field) {
        if (ability instanceof AbilitySlam) {
            AbilitySlam slam = (AbilitySlam) ability;
            if (id == 100) slam.setDamage(parseFloat(field, slam.getDamage()));
            else if (id == 101) slam.setRadius(parseFloat(field, slam.getRadius()));
            else if (id == 102) slam.setKnockbackStrength(parseFloat(field, slam.getKnockbackStrength()));
            else if (id == 103) slam.setLeapSpeed(parseFloat(field, slam.getLeapSpeed()));
            else if (id == 104) slam.setMinLeapDistance(parseFloat(field, slam.getMinLeapDistance()));
            else if (id == 105) slam.setMaxLeapDistance(parseFloat(field, slam.getMaxLeapDistance()));
        } else if (ability instanceof AbilityCharge) {
            AbilityCharge charge = (AbilityCharge) ability;
            if (id == 100) charge.setDamage(parseFloat(field, charge.getDamage()));
            else if (id == 101) charge.setChargeSpeed(parseFloat(field, charge.getChargeSpeed()));
            else if (id == 102) charge.setKnockback(parseFloat(field, charge.getKnockback()));
            else if (id == 103) charge.setKnockbackUp(parseFloat(field, charge.getKnockbackUp()));
            else if (id == 104) charge.setMaxDistance(parseFloat(field, charge.getMaxDistance()));
            else if (id == 105) charge.setHitRadius(parseFloat(field, charge.getHitRadius()));
        } else if (ability instanceof AbilityHeavyHit) {
            AbilityHeavyHit hit = (AbilityHeavyHit) ability;
            if (id == 100) hit.setDamage(parseFloat(field, hit.getDamage()));
            else if (id == 101) hit.setKnockback(parseFloat(field, hit.getKnockback()));
            else if (id == 102) hit.setKnockbackUp(parseFloat(field, hit.getKnockbackUp()));
            else if (id == 103) hit.setStunTicks(field.getInteger());
        } else if (ability instanceof AbilityCutter) {
            AbilityCutter cutter = (AbilityCutter) ability;
            if (id == 100) cutter.setArcAngle(parseFloat(field, cutter.getArcAngle()));
            else if (id == 101) cutter.setRange(parseFloat(field, cutter.getRange()));
            else if (id == 102) cutter.setDamage(parseFloat(field, cutter.getDamage()));
            else if (id == 103) cutter.setKnockback(parseFloat(field, cutter.getKnockback()));
            else if (id == 104) cutter.setSweepWaves(field.getInteger());
            else if (id == 105) cutter.setWaveInterval(field.getInteger());
            else if (id == 106) cutter.setRotationSpeed(parseFloat(field, cutter.getRotationSpeed()));
        } else if (ability instanceof AbilityOrb) {
            AbilityOrb orb = (AbilityOrb) ability;
            if (id == 100) orb.setOrbSpeed(parseFloat(field, orb.getOrbSpeed()));
            else if (id == 101) orb.setOrbSize(parseFloat(field, orb.getOrbSize()));
            else if (id == 102) orb.setDamage(parseFloat(field, orb.getDamage()));
            else if (id == 103) orb.setKnockback(parseFloat(field, orb.getKnockback()));
            else if (id == 104) orb.setMaxDistance(parseFloat(field, orb.getMaxDistance()));
            else if (id == 105) orb.setMaxLifetime(field.getInteger());
            else if (id == 108) orb.setExplosionRadius(parseFloat(field, orb.getExplosionRadius()));
        } else if (ability instanceof AbilityVortex) {
            AbilityVortex pull = (AbilityVortex) ability;
            if (id == 100) pull.setPullRadius(parseFloat(field, pull.getPullRadius()));
            else if (id == 101) pull.setPullStrength(parseFloat(field, pull.getPullStrength()));
            else if (id == 102) pull.setDamage(parseFloat(field, pull.getDamage()));
            else if (id == 103) pull.setPullToDistance(parseFloat(field, pull.getPullToDistance()));
            else if (id == 104) pull.setStunDuration(field.getInteger());
            else if (id == 105) pull.setRootDuration(field.getInteger());
            else if (id == 107) pull.setMaxTargets(field.getInteger());
        } else if (ability instanceof AbilityShockwave) {
            AbilityShockwave shock = (AbilityShockwave) ability;
            if (id == 100) shock.setPushRadius(parseFloat(field, shock.getPushRadius()));
            else if (id == 101) shock.setPushStrength(parseFloat(field, shock.getPushStrength()));
            else if (id == 102) shock.setDamage(parseFloat(field, shock.getDamage()));
            else if (id == 103) shock.setPushUp(parseFloat(field, shock.getPushUp()));
            else if (id == 104) shock.setStunDuration(field.getInteger());
            else if (id == 105) shock.setMaxTargets(field.getInteger());
        } else if (ability instanceof AbilityDash) {
            AbilityDash dash = (AbilityDash) ability;
            if (id == 101) dash.setDashDistance(parseFloat(field, dash.getDashDistance()));
            else if (id == 102) dash.setDashSpeed(parseFloat(field, dash.getDashSpeed()));
        } else if (ability instanceof AbilityTeleport) {
            AbilityTeleport tp = (AbilityTeleport) ability;
            if (id == 100) tp.setBlinkCount(field.getInteger());
            else if (id == 101) tp.setBlinkDelayTicks(field.getInteger());
            else if (id == 102) tp.setBlinkRadius(parseFloat(field, tp.getBlinkRadius()));
            else if (id == 103) tp.setMinBlinkRadius(parseFloat(field, tp.getMinBlinkRadius()));
            else if (id == 104) tp.setDamage(parseFloat(field, tp.getDamage()));
            else if (id == 105) tp.setDamageRadius(parseFloat(field, tp.getDamageRadius()));
        } else if (ability instanceof AbilityProjectile) {
            AbilityProjectile proj = (AbilityProjectile) ability;
            if (id == 100) proj.setDamage(parseFloat(field, proj.getDamage()));
            else if (id == 101) proj.setSpeed(parseFloat(field, proj.getSpeed()));
            else if (id == 102) proj.setKnockback(parseFloat(field, proj.getKnockback()));
            else if (id == 103) proj.setExplosionRadius(parseFloat(field, proj.getExplosionRadius()));
        } else if (ability instanceof AbilityBeam) {
            AbilityBeam beam = (AbilityBeam) ability;
            if (id == 100) beam.setBeamLength(parseFloat(field, beam.getBeamLength()));
            else if (id == 101) beam.setBeamWidth(parseFloat(field, beam.getBeamWidth()));
            else if (id == 102) beam.setDamage(parseFloat(field, beam.getDamage()));
            else if (id == 103) beam.setDamageInterval(field.getInteger());
            else if (id == 104) beam.setSweepAngle(parseFloat(field, beam.getSweepAngle()));
            else if (id == 105) beam.setSweepSpeed(parseFloat(field, beam.getSweepSpeed()));
        } else if (ability instanceof AbilityGuard) {
            AbilityGuard guard = (AbilityGuard) ability;
            if (id == 100) guard.setDamageReduction(parseFloat(field, guard.getDamageReduction()));
            else if (id == 101) guard.setCounterDamage(parseFloat(field, guard.getCounterDamage()));
            else if (id == 103) guard.setCounterChance(parseFloat(field, guard.getCounterChance()));
        } else if (ability instanceof AbilityHeal) {
            AbilityHeal heal = (AbilityHeal) ability;
            if (id == 100) heal.setHealAmount(parseFloat(field, heal.getHealAmount()));
            else if (id == 101) heal.setHealPercent(parseFloat(field, heal.getHealPercent()));
            else if (id == 102) heal.setHealRadius(parseFloat(field, heal.getHealRadius()));
        } else if (ability instanceof AbilityHazard) {
            AbilityHazard hazard = (AbilityHazard) ability;
            if (id == 100) hazard.setRadius(parseFloat(field, hazard.getRadius()));
            else if (id == 101) hazard.setDamagePerTick(parseFloat(field, hazard.getDamagePerTick()));
            else if (id == 102) hazard.setDamageInterval(field.getInteger());
            else if (id == 103) hazard.setDebuffDuration(field.getInteger());
        } else if (ability instanceof AbilityTrap) {
            AbilityTrap trap = (AbilityTrap) ability;
            if (id == 100) trap.setTriggerRadius(parseFloat(field, trap.getTriggerRadius()));
            else if (id == 101) trap.setDamage(parseFloat(field, trap.getDamage()));
            else if (id == 102) trap.setArmTime(field.getInteger());
            else if (id == 103) trap.setMaxTriggers(field.getInteger());
            else if (id == 104) trap.setKnockback(parseFloat(field, trap.getKnockback()));
            else if (id == 105) trap.setRootDuration(field.getInteger());
        }
    }

    private float parseFloat(GuiNpcTextField field, float defaultValue) {
        try {
            return Float.parseFloat(field.getText());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // APPLY TO ABILITY
    // ═══════════════════════════════════════════════════════════════════════════

    private void applyToAbility() {
        // General
        ability.setName(name);
        ability.setEnabled(enabled);
        ability.setWeight(weight);
        ability.setMinRange(minRange);
        ability.setMaxRange(maxRange);
        ability.setTargetingMode(targetingMode);

        // Timing
        ability.setCooldownTicks(cooldownTicks);
        ability.setWindUpTicks(windUpTicks);
        ability.setActiveTicks(activeTicks);
        ability.setRecoveryTicks(recoveryTicks);
        ability.setInterruptible(interruptible);
        ability.setInterruptThreshold(interruptThreshold);
        ability.setLockMovement(lockMovement);

        // Telegraph
        ability.setShowTelegraph(showTelegraph);
        ability.setWindUpColor(windUpColor);
        ability.setActiveColor(activeColor);

        // Effects (sounds/animations)
        ability.setWindUpSound(windUpSound);
        ability.setActiveSound(activeSound);
        ability.setWindUpAnimationId(windUpAnimationId);
        ability.setActiveAnimationId(activeAnimationId);

        // Type-specific values are applied directly in handleTypeTextField and handleTypeButton
    }

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        if (subgui instanceof SubGuiColorSelector) {
            SubGuiColorSelector colorSelector = (SubGuiColorSelector) subgui;
            // Color selector returns RGB without alpha, so we need to add appropriate alpha
            // Wind up color uses 0x80 alpha (semi-transparent), active uses 0xC0 (more opaque)
            int rgb = colorSelector.color & 0x00FFFFFF;
            if (editingColorId == 22) {
                windUpColor = 0x80000000 | rgb;
            } else if (editingColorId == 24) {
                activeColor = 0xC0000000 | rgb;
            }
            editingColorId = 0;
            initGui();
        } else if (subgui instanceof GuiSoundSelection) {
            GuiSoundSelection soundSelector = (GuiSoundSelection) subgui;
            if (soundSelector.selectedResource != null) {
                String sound = soundSelector.selectedResource.toString();
                if (editingSoundId == 30) {
                    windUpSound = sound;
                } else if (editingSoundId == 31) {
                    activeSound = sound;
                }
            }
            editingSoundId = 0;
            initGui();
        } else if (subgui instanceof GuiAnimationSelection) {
            GuiAnimationSelection animSelector = (GuiAnimationSelection) subgui;
            // Update if user selected something or cleared
            if (editingAnimationId == 32) {
                windUpAnimationId = animSelector.selectedAnimationId;
                windUpAnimationName = animSelector.getSelectedName();
            } else if (editingAnimationId == 33) {
                activeAnimationId = animSelector.selectedAnimationId;
                activeAnimationName = animSelector.getSelectedName();
            }
            editingAnimationId = 0;
            initGui();
        }
    }
}
