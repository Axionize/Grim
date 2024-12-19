package ac.grim.grimac.checks.impl.crash;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.abstracts.AbstractPrePredictionCheck;
import ac.grim.grimac.checks.type.interfaces.PacketCheckI;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.inventory.inventory.MenuType;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenWindow;

@CheckData(name = "CrashD", description = "Clicking slots in lectern window")
public class CrashD extends AbstractPrePredictionCheck {

    public CrashD(GrimPlayer playerData) {
        super(playerData);
    }

    private MenuType type = MenuType.UNKNOWN;
    private int lecternId = -1;

    @Override
    public void onPacketSend(final PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.OPEN_WINDOW && isSupportedVersion()) {
            WrapperPlayServerOpenWindow window = new WrapperPlayServerOpenWindow(event);
            this.type = MenuType.getMenuType(window.getType());
            if (type == MenuType.LECTERN) lecternId = window.getContainerId();
        }
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW && isSupportedVersion()) {
            WrapperPlayClientClickWindow click = new WrapperPlayClientClickWindow(event);
            int clickType = click.getWindowClickType().ordinal();
            int button = click.getButton();
            int windowId = click.getWindowId();

            if (type == MenuType.LECTERN && windowId > 0 && windowId == lecternId) {
                if (flagAndAlert("clickType=" + clickType + " button=" + button)) {
                    event.setCancelled(true);
                    player.onPacketCancel();
                }
            }
        }
    }

    private boolean isSupportedVersion() {
        return PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_14);
    }

}
