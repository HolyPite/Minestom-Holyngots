package org.example.utils.Explosion;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.Explosion;
import net.minestom.server.instance.ExplosionSupplier;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ExplosionPacket;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import org.example.utils.BlockWithPosition;
import org.example.utils.TKit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

public class ExplosionSupplierUtils {
    public static final ExplosionSupplier DEFAULT =
            (cx, cy, cz, strength, additionalData) ->
                    new Explosion(cx, cy, cz, strength) {

                        /* -------------------- PARAMÈTRES ISSUS DU NBT -------------------- */

                        private final float fireChance = additionalData != null
                                ? additionalData.getFloat("fireChance")
                                : 0.5f;

                        private final float kbFactor = additionalData != null
                                ? additionalData.getFloat("kb")
                                : 10.0f;

                        /* ---------------------------------------------------------------- */

                        @Override
                        protected List<Point> prepare(Instance instance) {
                            Pos center = new Pos(cx, cy, cz);

                            /* 1) Son + flash (inchangé) */
                            instance.playSound(
                                    Sound.sound(Key.key("entity.generic.explode"),
                                            Sound.Source.MASTER, 4f, 1f),
                                    cx, cy, cz);
                            instance.sendGroupedPacket(new ParticlePacket(
                                    Particle.FLASH, cx, cy, cz, 0, 0, 0, 0, 30));

                            /* 2) Blocs détruits (nouvelle logique) -------------------------------- */
                            double rCore      = 1.5 * strength - 1;        // noyau full-air
                            double rRing1     = 1.5 * strength;            // couronne 70 %
                            double rRing2     = 1.5 * strength + 1;        // couronne 20 %

                            // On prend tous les blocs du rayon max
                            List<BlockWithPosition> sphere = TKit.getBlocksInSphere(instance, center, rRing2);
                            List<Point> broken = new ArrayList<>();

                            ThreadLocalRandom rnd = ThreadLocalRandom.current();

                            for (BlockWithPosition b : sphere) {
                                if (b.block().isAir() || b.block() == Block.BEDROCK) continue;

                                double distSq = TKit.distanceSquared(center, b.pos()); // util qui renvoie Vec d'un bloc
                                double dist   = Math.sqrt(distSq);

                                boolean breakIt =
                                        dist <= rCore ? true :
                                                dist <= rRing1 ? rnd.nextDouble() < 0.90 :
                                                        dist <= rRing2 ? rnd.nextDouble() < 0.10 :
                                                                false;

                                if (!breakIt) continue;

                                instance.setBlock(b.x(), b.y(), b.z(), Block.AIR);
                                broken.add(new Pos(b.x(), b.y(), b.z()));
                            }

                            /* 3) Dégâts & knock-back (inchangé sauf rayon) ----------------------- */
                            double dmgRadius   = 2 * strength;
                            double dmgRadiusSq = dmgRadius * dmgRadius;

                            for (Entity e : instance.getEntities()) {
                                if (!(e instanceof LivingEntity living)) continue;

                                double distSq = TKit.distanceSquared(center, e.getPosition());
                                if (distSq > dmgRadiusSq) continue;

                                double dist = Math.sqrt(distSq);
                                double factor = 1.0 - dist / dmgRadius;

                                float dmg = (float) (2 * strength * factor);
                                if (dmg > 0) living.damage(DamageType.EXPLOSION, dmg);

                                if (kbFactor != 0) {
                                    Vec dir = e.getPosition().asVec().sub(center).normalize();
                                    living.setVelocity(living.getVelocity().add(dir.mul(kbFactor * factor)));
                                }
                            }
                            return broken;
                        }



                        /* 4) Effet de fumée persistante après l’explosion ----------------------------- */
                        @Override
                        protected void postExplosion(Instance instance, List<Point> broken, ExplosionPacket packet) {
                            final int[] ticksLeft = {10};

                            if (fireChance != 0) {
                                for (Point p : broken) {
                                    if (ThreadLocalRandom.current().nextDouble() >= fireChance)
                                        continue;                    // on saute ce bloc

                                    int fx = (int) p.x();
                                    int fy = (int) p.y();
                                    int fz = (int) p.z();

                                    // vérifie qu’il y a de l’air ici et un bloc solide dessous
                                    if (instance.getBlock(fx, fy, fz).isAir() &&
                                            !instance.getBlock(fx, fy - 1, fz).isAir() &&
                                            instance.getBlock(fx, fy - 1, fz) != Block.FIRE) {

                                        instance.setBlock(fx, fy, fz, Block.FIRE);
                                    }
                                }
                            }


                            AtomicReference<Task> ref = new AtomicReference<>();

                            ref.set(instance.scheduler().buildTask(() -> {
                                if (--ticksLeft[0] <= 0) {
                                    ref.get().cancel();
                                    return;
                                }
                                for (Player p : TKit.getPlayersInRadius(instance,
                                        new Pos(cx, cy, cz), strength + 30)) {
                                    p.sendPacket(new ParticlePacket(
                                            Particle.LARGE_SMOKE, cx, cy, cz,
                                            0.25f, 0.25f, 0.25f, 0, 8));
                                }
                            }).delay(TaskSchedule.tick(10))
                            .repeat(TaskSchedule.tick(10))
                            .schedule());

                        }

                    };
}
