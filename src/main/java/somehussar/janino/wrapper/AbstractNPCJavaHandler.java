package somehussar.janino.wrapper;

import io.github.somehussar.janinoloader.annotations.PermissableScriptHandler;
import noppes.npcs.api.event.INpcEvent;
import noppes.npcs.scripted.event.NpcEvent;
import noppes.npcs.scripted.event.ProjectileEvent;

public abstract class AbstractNPCJavaHandler {

    public void prepareToUnload() {
    }

    public void onInitialize(INpcEvent.InitEvent event) {
    }

    public void onUpdate(NpcEvent.UpdateEvent event) {
    }

    public void onDialog(NpcEvent.DialogEvent event) {
    }

    public void onProjectileTick(ProjectileEvent.UpdateEvent event) {
    }

    public void onDialogClosed(NpcEvent.DialogClosedEvent event) {
    }

    public void onAttack(NpcEvent.MeleeAttackEvent event) {
    }

    public void onRangedAttack(NpcEvent.RangedLaunchedEvent event) {
    }

    public void onMeleeSwing(NpcEvent.SwingEvent event) {
    }

    public void onKillEntity(NpcEvent.KilledEntityEvent event) {
    }

    public void onTarget(NpcEvent.TargetEvent event) {
    }

    public void onTargetLost(NpcEvent.TargetLostEvent event) {
    }

    public void onCollide(NpcEvent.CollideEvent event) {
    }

    public void onDamaged(NpcEvent.DamagedEvent event) {
    }

    public void onNPCKilled(NpcEvent.DiedEvent event) {
    }

    public void onTimer(NpcEvent.TimerEvent event) {
    }

    public void onProjectileImpact(ProjectileEvent.ImpactEvent event) {
    }
}
