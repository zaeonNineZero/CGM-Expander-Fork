package com.mrcrayfish.guns.client;

import com.mrcrayfish.framework.api.serialize.DataObject;
import com.mrcrayfish.framework.client.resources.IDataLoader;
import com.mrcrayfish.framework.client.resources.IResourceSupplier;
import com.mrcrayfish.guns.GunMod;
import com.mrcrayfish.guns.item.IMeta;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: MrCrayfish
 */
public final class AnimationMetaLoader implements IDataLoader<AnimationMetaLoader.AnimResource>
{
   
	/**
	 * This is a modified MetaLoader designed for loading animations
	 * designed around the Common Animation System.
	 * (Legacy version)
	 */
	
	private static AnimationMetaLoader instance;

    public static AnimationMetaLoader getInstance()
    {
        if(instance == null)
        {
            instance = new AnimationMetaLoader();
        }
        return instance;
    }

    private final Object2ObjectMap<ResourceLocation, DataObject> resourceToData = Util.make(new Object2ObjectOpenCustomHashMap<>(Util.identityStrategy()), map -> map.defaultReturnValue(DataObject.EMPTY));

    private AnimationMetaLoader() {}

    public DataObject getData(ResourceLocation key)
    {
    	return this.resourceToData.get(key);
    }

	@Override
    public List<AnimResource> getResourceSuppliers()
    {
        List<AnimResource> resources = new ArrayList<>();
        ForgeRegistries.ITEMS.getValues().stream().filter(item -> item instanceof IMeta).forEach(item ->
        {
            ResourceLocation key = item.builtInRegistryHolder().key().location();
            ResourceLocation location = new ResourceLocation(key.getNamespace(), "animations/" + key.getPath() + ".cgmanim");
            resources.add(new AnimResource(key, location));
        });
        return resources;
    }

    @Override
    public void process(List<Pair<AnimResource, DataObject>> list)
    {
        this.resourceToData.clear();
        list.forEach(pair ->
        {
            DataObject object = pair.getRight();
            if(!object.isEmpty())
            {
            	AnimResource resource = pair.getLeft();
                this.resourceToData.put(resource.key(), object); 
                GunMod.LOGGER.info("LEGACY LOADER: Loaded animation " + resource.key().toString() + " at file location " + resource.location());
            }
        });
    }

    @Override
    public boolean ignoreMissing()
    {
        return true;
    }

    public record AnimResource(ResourceLocation key, ResourceLocation location) implements IResourceSupplier
    {
        @Override
        public ResourceLocation getLocation()
        {
            return this.location;
        }
    }
}
