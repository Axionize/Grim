package ac.grim.grimac.checks.impl.visual;

import ac.grim.grimac.api.config.ConfigManager;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEffect;

@CheckData(name = "EffectsHider")
public class EffectsHider extends Check implements PacketCheck {

    boolean enabled;

    public EffectsHider(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (!enabled)
            return;

        if (event.getPacketType() == PacketType.Play.Server.ENTITY_EFFECT) {
            WrapperPlayServerEntityEffect wrapper = new WrapperPlayServerEntityEffect(event);

            if (wrapper.getEntityId() != player.entityID) {
                // Cancel send packet about other entity potions.
                event.setCancelled(true);
            }
        }
    }

    @Override
    public void onReload(ConfigManager config) {
        enabled = config.getBooleanElse("visual.effects-hider", false);
    }
}
