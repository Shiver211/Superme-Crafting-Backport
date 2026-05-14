package com.shiver.supermecrafting.furnace;

import com.shiver.supermecrafting.Tags;
import com.shiver.supermecrafting.net.MultiblockSyncPacket;
import com.shiver.supermecrafting.net.SCNetwork;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public class FurnaceTickHandler {

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.world instanceof WorldServer)) return;
        WorldServer level = (WorldServer) event.world;

        FurnaceTick.tickAll(level);
        CasingCascade.tick(level);
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.player instanceof EntityPlayerMP)) return;
        if (!(event.player.world instanceof WorldServer)) return;
        EntityPlayerMP player = (EntityPlayerMP) event.player;
        WorldServer level = (WorldServer) player.world;

        MultiblockRegions regions = MultiblockRegions.get(level);
        for (Region r : regions.all()) {
            int[] b = r.getBounds();
            SCNetwork.INSTANCE.sendTo(new MultiblockSyncPacket(
                    MultiblockSyncPacket.OP_ADD,
                    r.getId(),
                    new BlockPos(b[0], b[1], b[2]),
                    new BlockPos(b[3], b[4], b[5]),
                    r.getFront(),
                    r.isLit()), player);
        }
    }
}
