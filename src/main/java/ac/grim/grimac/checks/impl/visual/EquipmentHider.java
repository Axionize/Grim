package ac.grim.grimac.checks.impl.visual;

import ac.grim.grimac.api.config.ConfigManager;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.player.GrimPlayer;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.netty.channel.ChannelHelper;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.enchantment.Enchantment;
import com.github.retrooper.packetevents.protocol.item.enchantment.type.EnchantmentTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;

import java.util.Collections;
import java.util.List;

@CheckData(name = "EquipmentHider", description = "Hide player equipment for nametags hacks.")
public class EquipmentHider extends Check implements PacketCheck {

    boolean useDamageableInterface = PacketEvents.getAPI().getServerManager()
            .getVersion().isNewerThanOrEquals(ServerVersion.V_1_13);
    boolean enable, spoofAmount, spoofDurability, spoofEnchantments;

    private final List<Enchantment> enchantmentList = Collections.singletonList(
            Enchantment.builder().type(EnchantmentTypes.UNBREAKING).level(3).build());

    public EquipmentHider(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (player.disableGrim || player.noModifyPacketPermission) {
            return;
        }

        if (enable && event.getPacketType() == PacketType.Play.Server.ENTITY_EQUIPMENT) {
            WrapperPlayServerEntityEquipment wrapper = new WrapperPlayServerEntityEquipment(event);

            List<Equipment> equipmentList = wrapper.getEquipment();

            if (equipmentList.isEmpty()) {
                return;
            }

            equipmentList.forEach(equipment -> this.handleEquipment(equipment, wrapper.getClientVersion()));

            event.setCancelled(true);
            WrapperPlayServerEntityEquipment metadata = new WrapperPlayServerEntityEquipment(wrapper.getEntityId(),
                    equipmentList);
            ChannelHelper.runInEventLoop(player.user.getChannel(), () -> player.user.sendPacketSilently(metadata));
        }
    }

    private void handleEquipment(Equipment equipment, ClientVersion clientVersion) {
        ItemStack itemStack = equipment.getItem();

        if (itemStack == null) {
            return;
        }

        if (this.spoofAmount && itemStack.getAmount() > 1) {
            itemStack.setAmount(1);
            equipment.setItem(itemStack);
        }

        if (this.spoofDurability && itemStack.isDamageableItem()) {
            if (this.useDamageableInterface) {
                itemStack.setDamageValue(0);
            } else {
                itemStack.setLegacyData(0);
            }

            equipment.setItem(itemStack);
        }
        if (this.spoofEnchantments && itemStack.isEnchanted(clientVersion)) {
            itemStack.setEnchantments(this.enchantmentList, clientVersion);
            equipment.setItem(itemStack);
        }
    }

    @Override
    public void onReload(ConfigManager config) {
        enable = config.getBooleanElse("visual.equipment-hider.enable", false);
        spoofAmount = config.getBooleanElse("visual.equipment-hider.amount", false);
        spoofEnchantments = config.getBooleanElse("visual.equipment-hider.enchantments", false);
        spoofDurability = config.getBooleanElse("visual.equipment-hider.durability", false);
    }
}
