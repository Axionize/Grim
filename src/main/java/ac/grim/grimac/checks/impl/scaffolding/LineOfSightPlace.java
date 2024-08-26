package ac.grim.grimac.checks.impl.scaffolding;

import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.BlockPlaceCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.BlockPlace;
import ac.grim.grimac.utils.data.HitData;
import ac.grim.grimac.utils.nmsutil.BlockRayTrace;
import ac.grim.grimac.utils.nmsutil.Ray;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.util.Vector3i;
import org.bukkit.util.Vector;

import java.util.*;

@CheckData(name = "LineOfSightPlace")
public class LineOfSightPlace extends BlockPlaceCheck {

    double flagBuffer = 0; // If the player flags once, force them to play legit, or we will cancel the tick before.
    boolean ignorePost = false;
    boolean useBlockWhitelist;
    private HashSet<StateType> blockWhitelist;

    public LineOfSightPlace(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onBlockPlace(final BlockPlace place) {
        if (place.getMaterial() == StateTypes.SCAFFOLDING) return;
        if (player.gamemode == GameMode.SPECTATOR) return; // you don't send flying packets when spectating entities
        if (useBlockWhitelist) {
            if (!isBlockTypeWhitelisted(player.compensatedWorld.getWrappedBlockStateAt(place.getPlacedAgainstBlockLocation()).getType())) {
                return;
            }
        }

        if (flagBuffer > 0 && !didRayTraceHit(place)) {
            // If the player hit and has flagged this check recently
            if (flagAndAlert("pre-flying") && shouldModifyPackets() && shouldCancel()) {
                place.resync();  // Deny the block placement.
            }
        }
    }

    // Use post flying because it has the correct rotation, and can't false easily.
    @Override
    public void onPostFlyingBlockPlace(BlockPlace place) {
        if (place.getMaterial() == StateTypes.SCAFFOLDING) return;
        if (player.gamemode == GameMode.SPECTATOR) return; // you don't send flying packets when spectating entities

        if (useBlockWhitelist) {
            if (!isBlockTypeWhitelisted(player.compensatedWorld.getWrappedBlockStateAt(place.getPlacedAgainstBlockLocation()).getType())) {
                return;
            }
        }

        // Don't flag twice
        if (ignorePost) {
            ignorePost = false;
            return;
        }

        // Ray trace to try and hit the target block.
        boolean hit = didRayTraceHit(place);
        // This can false with rapidly moving yaw in 1.8+ clients
        if (!hit) {
            flagBuffer = 1;
            flagAndAlert("post-flying");
        } else {
            flagBuffer = Math.max(0, flagBuffer - 0.1);
        }
    }

    private boolean didRayTraceHit(BlockPlace place) {
        Vector3i interactBlockVec = new Vector3i(place.getPlacedAgainstBlockLocation().getX(),
                place.getPlacedAgainstBlockLocation().getY(), place.getPlacedAgainstBlockLocation().getZ());

        // We do not need to add 0.03/0.0002 to maxDistance to ensure our raytrace hits blocks
        // Since we expand the hitboxes of the expectedTargetBlock by 0.03/0.002 already later
        double maxDistance = player.compensatedEntities.getSelf()
                .getAttributeValue(Attributes.PLAYER_BLOCK_INTERACTION_RANGE);
        List<Vector3f> possibleLookDirs = new ArrayList<>(Arrays.asList(
                new Vector3f(player.lastXRot, player.yRot, 0),
                new Vector3f(player.xRot, player.yRot, 0)
        ));
        for (double eyeHeight : player.getPossibleEyeHeights()) {
            for (Vector3f lookDir : possibleLookDirs) {
                Vector eyePosition = new Vector(player.x, player.y + eyeHeight, player.z);
                Vector eyeLookDir = new Ray(player, eyePosition.getX(), eyePosition.getY(), eyePosition.getZ(), lookDir.x, lookDir.y).calculateDirection();

                Vector3i rayTracedBlockVec = getTargetBlock(eyePosition, eyeLookDir, maxDistance, interactBlockVec);

                if (rayTracedBlockVec == null) {
                    continue;
                }

                if (interactBlockVec.equals(rayTracedBlockVec)) {
                    return true;
                }
            }
        }

        return false;
    }

    private Vector3i getTargetBlock(Vector eyePosition, Vector eyeDirection, double maxDistance, Vector3i targetBlockVec) {
        HitData hitData = BlockRayTrace.getNearestReachHitResult(player, eyePosition, eyeDirection, maxDistance, maxDistance, targetBlockVec);
        if (hitData == null) return null;
        return hitData.getPosition();
    }

    private boolean isBlockTypeWhitelisted(StateType type) {
        return blockWhitelist.contains(type);
    }

    @Override
    public void reload() {
        super.reload();

        useBlockWhitelist = getConfig().getBooleanElse("LineOfSightPlace.use-block-whitelist", false);
        blockWhitelist = new HashSet<>();
        List<String> blocks = getConfig().getList("LineOfSightPlace.block-whitelist");
        for (String block : blocks) {
            blockWhitelist.add(StateTypes.getByName(block));
        }
    }
}
