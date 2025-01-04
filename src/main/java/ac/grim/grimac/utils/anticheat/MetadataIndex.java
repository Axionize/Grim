package ac.grim.grimac.utils.anticheat;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;

public class MetadataIndex {

    public static final int AIR_TICKS;
    public static final int HEALTH;
    public static final int ABSORPTION;
    public static final int XP;
    public static final int TAMABLE_TAMED;
    public static final int TAMABLE_OWNER;

    static {
        ServerVersion serverVersion = PacketEvents.getAPI().getServerManager().getVersion();
        AIR_TICKS = 1;
        HEALTH = serverVersion.isNewerThanOrEquals(ServerVersion.V_1_17) ? 9 : (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_14) ? 8 : (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_10) ? 7 : 6));
        ABSORPTION = serverVersion.isNewerThanOrEquals(ServerVersion.V_1_17) ? 15 : (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_15) ? 14 : (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_14) ? 13 : (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_10) ? 11 : (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_9) ? 10 : 17))));
        XP = serverVersion.isNewerThanOrEquals(ServerVersion.V_1_17) ? 16 : (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_15) ? 15 : (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_14) ? 14 : (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_10) ? 12 : (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_9) ? 11 : 18))));
        if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_17)) {
            TAMABLE_TAMED = 17;
            TAMABLE_OWNER = 18;
        } else if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_15)) {
            TAMABLE_TAMED = 16;
            TAMABLE_OWNER = 17;
        } else if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_14)) {
            TAMABLE_TAMED = 15;
            TAMABLE_OWNER = 16;
        } else if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_12)) {
            TAMABLE_TAMED = 13;
            TAMABLE_OWNER = 14;
        } else {
            TAMABLE_TAMED = 16;
            TAMABLE_OWNER = 17;
        }
    }
}
