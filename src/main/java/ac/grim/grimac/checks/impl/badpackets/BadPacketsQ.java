package ac.grim.grimac.checks.impl.badpackets;

import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.abstracts.AbstractPacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Client;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction.Action;

@CheckData(name = "BadPacketsQ")
public class BadPacketsQ extends AbstractPacketCheck {
    public BadPacketsQ(final GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == Client.ENTITY_ACTION) {
            WrapperPlayClientEntityAction wrapper = new WrapperPlayClientEntityAction(event);

            if (wrapper.getJumpBoost() < 0 || wrapper.getJumpBoost() > 100 || wrapper.getEntityId() != player.entityID || (wrapper.getAction() != Action.START_JUMPING_WITH_HORSE && wrapper.getJumpBoost() != 0)) {
                if (flagAndAlert("boost=" + wrapper.getJumpBoost() + ", action=" + wrapper.getAction() + ", entity=" + wrapper.getEntityId()) && shouldModifyPackets()) {
                    event.setCancelled(true);
                    player.onPacketCancel();
                }
            }
        }
    }
}
