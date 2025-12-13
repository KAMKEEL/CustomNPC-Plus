package somehussar.janino.wrapper;

import io.github.somehussar.janinoloader.annotations.PermissableScriptHandler;
import noppes.npcs.api.event.INpcEvent;
import noppes.npcs.scripted.event.ProjectileEvent;

public abstract class AbstractNPCJavaHandler {

    public void prepareToUnload() {
    }

    public void onInitialize(INpcEvent.InitEvent event) {
    }

    public void onUpdate(INpcEvent.UpdateEvent event) {
    }

    public void onDialog(INpcEvent.DialogEvent event) {
    }

    public void onProjectileTick(ProjectileEvent.UpdateEvent event) {
    }

    public void onDialogClosed(INpcEvent.DialogClosedEvent event) {
    }

    public void onAttack(INpcEvent.MeleeAttackEvent event) {
    }

    public void onRangedAttack(INpcEvent.RangedLaunchedEvent event) {
    }

    public void onMeleeSwing(INpcEvent.SwingEvent event) {
    }

    public void onKillEntity(INpcEvent.KilledEntityEvent event) {
    }

    public void onTarget(INpcEvent.TargetEvent event) {
    }

    public void onTargetLost(INpcEvent.TargetLostEvent event) {
    }

    public void onCollide(INpcEvent.CollideEvent event) {
    }

    public void onDamaged(INpcEvent.DamagedEvent event) {
    }

    public void onNPCKilled(INpcEvent.DiedEvent event) {
    }

    public void onTimer(INpcEvent.TimerEvent event) {
    }

    public void onProjectileImpact(ProjectileEvent.ImpactEvent event) {
    }

    public void onInteract(INpcEvent.InteractEvent event) {
    }
}
