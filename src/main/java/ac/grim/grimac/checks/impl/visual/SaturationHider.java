package ac.grim.grimac.checks.impl.visual;

import ac.grim.grimac.api.config.ConfigManager;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.netty.channel.ChannelHelper;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateHealth;

@CheckData(name = "SaturationHider", description = "Hide player saturation for nametags hacks.")
public class SaturationHider extends Check implements PacketCheck {

    boolean enable;

    public SaturationHider(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (player.disableGrim || player.noModifyPacketPermission) {
            return;
        }

        if (enable && event.getPacketType() == PacketType.Play.Server.UPDATE_HEALTH) {
            WrapperPlayServerUpdateHealth wrapper = new WrapperPlayServerUpdateHealth(event);

            if (wrapper.getFoodSaturation() > 0) {
                event.setCancelled(true);
                WrapperPlayServerUpdateHealth metadata = new WrapperPlayServerUpdateHealth(wrapper.getHealth(), wrapper.getFood(), Float.NaN);
                ChannelHelper.runInEventLoop(player.user.getChannel(), () -> player.user.sendPacketSilently(metadata));
            }
        }
    }

    @Override
    public void onReload(ConfigManager config) {
        enable = config.getBooleanElse("visual.saturation-hider", false);
    }
}
