package com.mrcrayfish.guns.common;

import com.mrcrayfish.guns.Config;
import com.mrcrayfish.guns.Reference;
import com.mrcrayfish.guns.init.ModSyncedDataKeys;
import com.mrcrayfish.guns.item.GunItem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableLong;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class SpreadTracker
{
    private static final Map<Player, SpreadTracker> TRACKER_MAP = new WeakHashMap<>();

    private final Map<GunItem, Pair<MutableLong, MutableInt>> SPREAD_TRACKER_MAP = new HashMap<>();
    
    private int sprintBaseSpread;

    public void update(Player player, GunItem item)
    {
        Pair<MutableLong, MutableInt> entry = SPREAD_TRACKER_MAP.computeIfAbsent(item, gun -> Pair.of(new MutableLong(-1), new MutableInt()));
        MutableLong lastFire = entry.getLeft();
        MutableInt spreadCount = entry.getRight();
        sprintBaseSpread = (int) Math.floor((float) Config.COMMON.projectileSpread.maxCount.get()/2F);
        if(lastFire.getValue() != -1)
        {
            long deltaTime = System.currentTimeMillis() - lastFire.getValue();
            if(deltaTime < Config.COMMON.projectileSpread.spreadThreshold.get())
            {
                if(spreadCount.getValue() < Config.COMMON.projectileSpread.maxCount.get())
                {
                    spreadCount.increment();

                    /* Increases the spread count quicker if the player is not aiming down sight - can be disabled in the config */
                    if(spreadCount.getValue() < Config.COMMON.projectileSpread.maxCount.get() && !ModSyncedDataKeys.AIMING.getValue(player) && (Config.COMMON.projectileSpread.doSpreadHipFirePenalty.get()))
                    {
                        spreadCount.increment();
                    }
                }
            }
            else
            {
            	spreadCount.setValue(0);
            }
    		/* Spread is always at least ~50% when sprinting */
        	if(player.isSprinting())
        	{
                if (spreadCount.getValue() < sprintBaseSpread)
                spreadCount.setValue(sprintBaseSpread);
        	}
        }
        lastFire.setValue(System.currentTimeMillis());
    }

    public float getNextSpread(GunItem item, float aim)
    {
    	Pair<MutableLong, MutableInt> entry = SPREAD_TRACKER_MAP.get(item);
        if(entry != null)
        {
            float nextSpread = (Config.COMMON.projectileSpread.doSpreadHipFirePenalty.get() ? 1F+aim : 1F);
        	return ((float) entry.getRight().getValue()+nextSpread) / (float) Config.COMMON.projectileSpread.maxCount.get();
        }
        return 0F;
    }

    public float getSpread(GunItem item)
    {
        Pair<MutableLong, MutableInt> entry = SPREAD_TRACKER_MAP.get(item);
        if(entry != null)
        {
            return (float) entry.getRight().getValue() / (float) Config.COMMON.projectileSpread.maxCount.get();
        }
        return 0F;
    }

    public static SpreadTracker get(Player player)
    {
        return TRACKER_MAP.computeIfAbsent(player, player1 -> new SpreadTracker());
    }

    @SubscribeEvent
    public static void onPlayerDisconnect(PlayerEvent.PlayerLoggedOutEvent event)
    {
        MinecraftServer server = event.getEntity().getServer();
        if(server != null)
        {
            server.execute(() -> TRACKER_MAP.remove(event.getEntity()));
        }
    }
}
