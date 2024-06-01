package com.mrcrayfish.guns.init;

import com.mrcrayfish.framework.api.event.FrameworkEvent;
import com.mrcrayfish.framework.api.sync.Serializers;
import com.mrcrayfish.framework.api.sync.SyncedClassKey;
import com.mrcrayfish.framework.api.sync.SyncedDataKey;
import com.mrcrayfish.guns.Reference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

/**
 * Author: MrCrayfish
 */
public class ModSyncedDataKeys
{
    public static final SyncedDataKey<Player, Boolean> AIMING = SyncedDataKey.builder(SyncedClassKey.PLAYER, Serializers.BOOLEAN)
            .id(new ResourceLocation(Reference.MOD_ID, "aiming"))
            .defaultValueSupplier(() -> false)
            .resetOnDeath()
            .build();

    public static final SyncedDataKey<Player, Integer> BURSTCOUNT = SyncedDataKey.builder(SyncedClassKey.PLAYER, Serializers.INTEGER)
            .id(new ResourceLocation(Reference.MOD_ID, "burstcount"))
            .defaultValueSupplier(() -> 0)
            .resetOnDeath()
            .build();

    public static final SyncedDataKey<Player, Boolean> ONBURSTCOOLDOWN = SyncedDataKey.builder(SyncedClassKey.PLAYER, Serializers.BOOLEAN)
            .id(new ResourceLocation(Reference.MOD_ID, "onburstcooldown"))
            .defaultValueSupplier(() -> false)
            .resetOnDeath()
            .build();

    public static final SyncedDataKey<Player, Integer> RAMPUPSHOT = SyncedDataKey.builder(SyncedClassKey.PLAYER, Serializers.INTEGER)
            .id(new ResourceLocation(Reference.MOD_ID, "rampupshot"))
            .defaultValueSupplier(() -> 0)
            .resetOnDeath()
            .build();

    public static final SyncedDataKey<Player, Boolean> RELOADING = SyncedDataKey.builder(SyncedClassKey.PLAYER, Serializers.BOOLEAN)
            .id(new ResourceLocation(Reference.MOD_ID, "reloading"))
            .defaultValueSupplier(() -> false)
            .resetOnDeath()
            .build();

    public static final SyncedDataKey<Player, Boolean> SHOOTING = SyncedDataKey.builder(SyncedClassKey.PLAYER, Serializers.BOOLEAN)
            .id(new ResourceLocation(Reference.MOD_ID, "shooting"))
            .defaultValueSupplier(() -> false)
            .resetOnDeath()
            .build();

    public static final SyncedDataKey<Player, Integer> SWITCHTIME = SyncedDataKey.builder(SyncedClassKey.PLAYER, Serializers.INTEGER)
            .id(new ResourceLocation(Reference.MOD_ID, "switchtime"))
            .defaultValueSupplier(() -> 0)
            .resetOnDeath()
            .build();
}
