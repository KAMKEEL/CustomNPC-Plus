package somehussar.janino.wrapper;

import io.github.somehussar.janinoloader.api.script.IScriptClassBody;
import noppes.npcs.scripted.event.NpcEvent;
import org.codehaus.commons.compiler.Sandbox;

public final class NPCWrapper {

    private final Sandbox sandbox;
    public final IScriptClassBody<AbstractNPCJavaHandler> scriptBody;

    public NPCWrapper(Sandbox sandbox, IScriptClassBody<AbstractNPCJavaHandler> scriptBody) {
        this.sandbox = sandbox;
        this.scriptBody = scriptBody;
    }

    public void prepareToUnload() {
        sandbox.confine((java.security.PrivilegedAction<Void>) () -> { scriptBody.get().prepareToUnload(); return null; });
    }

    public void onInitialize(noppes.npcs.api.event.INpcEvent.InitEvent event) {
        sandbox.confine((java.security.PrivilegedAction<Void>) () -> { scriptBody.get().onInitialize(event); return null; });
    }

    public void onUpdate(noppes.npcs.scripted.event.NpcEvent.UpdateEvent event) {
        sandbox.confine((java.security.PrivilegedAction<Void>) () -> { scriptBody.get().onUpdate(event); return null; });
    }

    public void onDialog(noppes.npcs.scripted.event.NpcEvent.DialogEvent event) {
        sandbox.confine((java.security.PrivilegedAction<Void>) () -> { scriptBody.get().onDialog(event); return null; });
    }

    public void onProjectileTick(noppes.npcs.scripted.event.ProjectileEvent.UpdateEvent event) {
        sandbox.confine((java.security.PrivilegedAction<Void>) () -> { scriptBody.get().onProjectileTick(event); return null; });
    }

    public void onDialogClosed(noppes.npcs.scripted.event.NpcEvent.DialogClosedEvent event) {
        sandbox.confine((java.security.PrivilegedAction<Void>) () -> { scriptBody.get().onDialogClosed(event); return null; });
    }

    public void onAttack(noppes.npcs.scripted.event.NpcEvent.MeleeAttackEvent event) {
        sandbox.confine((java.security.PrivilegedAction<Void>) () -> { scriptBody.get().onAttack(event); return null; });
    }

    public void onRangedAttack(noppes.npcs.scripted.event.NpcEvent.RangedLaunchedEvent event) {
        sandbox.confine((java.security.PrivilegedAction<Void>) () -> { scriptBody.get().onRangedAttack(event); return null; });
    }

    public void onMeleeSwing(noppes.npcs.scripted.event.NpcEvent.SwingEvent event) {
        sandbox.confine((java.security.PrivilegedAction<Void>) () -> { scriptBody.get().onMeleeSwing(event); return null; });
    }

    public void onKillEntity(noppes.npcs.scripted.event.NpcEvent.KilledEntityEvent event) {
        sandbox.confine((java.security.PrivilegedAction<Void>) () -> { scriptBody.get().onKillEntity(event); return null; });
    }

    public void onTarget(noppes.npcs.scripted.event.NpcEvent.TargetEvent event) {
        sandbox.confine((java.security.PrivilegedAction<Void>) () -> { scriptBody.get().onTarget(event); return null; });
    }

    public void onTargetLost(noppes.npcs.scripted.event.NpcEvent.TargetLostEvent event) {
        sandbox.confine((java.security.PrivilegedAction<Void>) () -> { scriptBody.get().onTargetLost(event); return null; });
    }

    public void onCollide(noppes.npcs.scripted.event.NpcEvent.CollideEvent event) {
        sandbox.confine((java.security.PrivilegedAction<Void>) () -> { scriptBody.get().onCollide(event); return null; });
    }

    public void onDamaged(noppes.npcs.scripted.event.NpcEvent.DamagedEvent event) {
        sandbox.confine((java.security.PrivilegedAction<Void>) () -> { scriptBody.get().onDamaged(event); return null; });
    }

    public void onNPCKilled(noppes.npcs.scripted.event.NpcEvent.DiedEvent event) {
        sandbox.confine((java.security.PrivilegedAction<Void>) () -> { scriptBody.get().onNPCKilled(event); return null; });
    }

    public void onTimer(noppes.npcs.scripted.event.NpcEvent.TimerEvent event) {
        sandbox.confine((java.security.PrivilegedAction<Void>) () -> { scriptBody.get().onTimer(event); return null; });
    }

    public void onProjectileImpact(noppes.npcs.scripted.event.ProjectileEvent.ImpactEvent event) {
        sandbox.confine((java.security.PrivilegedAction<Void>) () -> { scriptBody.get().onProjectileImpact(event); return null; });
    }

    public void onInteract(NpcEvent.InteractEvent event) {
        sandbox.confine((java.security.PrivilegedAction<Void>) () -> { scriptBody.get().onInteract(event); return null; });
    }
}
