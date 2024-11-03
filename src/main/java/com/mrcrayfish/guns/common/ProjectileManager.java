package com.mrcrayfish.guns.common;

import com.mrcrayfish.guns.entity.ProjectileEntity;
import com.mrcrayfish.guns.init.ModEntities;
import com.mrcrayfish.guns.interfaces.IProjectileFactory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

/**
 * A class to manage custom projectile factories
 *
 * Author: MrCrayfish
 */
public class ProjectileManager
{
    private static ProjectileManager instance = null;

    public static ProjectileManager getInstance()
    {
        if(instance == null)
        {
            instance = new ProjectileManager();
        }
        return instance;
    }

    private final IProjectileFactory DEFAULT_FACTORY = (worldIn, entity, weapon, item, modifiedGun) -> new ProjectileEntity(ModEntities.PROJECTILE.get(), worldIn, entity, weapon, item, modifiedGun);
    private final Map<ResourceLocation, IProjectileFactory> projectileFactoryMap = new HashMap<>();
    private final Map<String, IProjectileFactory> projectileOverrideMap = new HashMap<>();

    /**
     * Registers a projectile factory for the given item. This allows for control over the entity
     * that is spawned when a weapon, that uses the given ammo, is fired.
     *
     * @param ammo    the ammo item
     * @param factory a custom projectile implementation
     */
    public void registerFactory(Item ammo, IProjectileFactory factory)
    {
        this.projectileFactoryMap.put(ForgeRegistries.ITEMS.getKey(ammo), factory);
    }

    /**
     * Registers a projectile factory that is decoupled from an item. This allows a specific entity
     * to be spawned when a weapon is fired, regardless of the ammo item loaded.
     *
     * @param ammo    the ammo item
     * @param factory a custom projectile implementation
     */
    public void registerOverride(String factoryID, IProjectileFactory factory)
    {
        this.projectileOverrideMap.put(factoryID, factory);
    }

    /**
     * Gets the projectile factory for the given resource location.
     *
     * @param id the resource id of the projectile factory (the id of the item)
     * @return the custom projectile factory or the default factory if nothing exists for the id
     */
    public IProjectileFactory getFactory(ResourceLocation id)
    {
        return this.projectileFactoryMap.getOrDefault(id, DEFAULT_FACTORY);
    }

    /**
     * Gets the projectile factory override for the given factory ID string.
     *
     * @param id the string ID of the factory override.
     * @return the custom projectile factory override for the id
     */
    public IProjectileFactory getOverride(String factoryID)
    {
        return this.projectileOverrideMap.getOrDefault(factoryID, null);
    }
}
