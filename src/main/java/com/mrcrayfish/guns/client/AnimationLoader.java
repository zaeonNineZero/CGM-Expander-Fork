package com.mrcrayfish.guns.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mrcrayfish.framework.api.serialize.DataObject;
import com.mrcrayfish.framework.client.resources.IDataLoader;
import com.mrcrayfish.framework.client.resources.IResourceSupplier;
import com.mrcrayfish.guns.GunMod;
import com.mrcrayfish.guns.item.IMeta;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: MrCrayfish
 */
public final class AnimationLoader implements IDataLoader<AnimationLoader.AnimResource>
{
   
	/**
	 * This is a modified MetaLoader designed for loading animations
	 * designed around the Common Animation System.
	 */
	
	private static AnimationLoader instance;
    private static final String EXTENSION = ".cgmanim";
    //private static final Gson GSON = new GsonBuilder().create();
	static boolean doTryingMetaLoadMessage=true;

    public static AnimationLoader getInstance()
    {
        if(instance == null)
        {
            instance = new AnimationLoader();
        }
        return instance;
    }

    private final Object2ObjectMap<String, DataObject> resourceToData = Util.make(new Object2ObjectOpenCustomHashMap<>(Util.identityStrategy()), map -> map.defaultReturnValue(DataObject.EMPTY));
    

    private AnimationLoader() {}

    public DataObject getData(ResourceLocation key)
    {
    	String newKey = new ResourceLocation(key.getNamespace(), key.getPath()).toString();
    	if (doTryingMetaLoadMessage)
    	{
    		GunMod.LOGGER.info("Searching for animation " + newKey + "; HashMap contains the following keys: " + resourceToData);
    		doTryingMetaLoadMessage=false;
    	}
    	return this.resourceToData.get(newKey);
    }

	@Override
    public List<AnimResource> getResourceSuppliers()
    {
		List<AnimResource> resources = new ArrayList<>();
		ResourceManager manager = Minecraft.getInstance().getResourceManager();
		for (var resource: manager.listResources("animations", location -> location.getPath().endsWith(EXTENSION)).entrySet()) {
            try(var input = resource.getValue().open()) {
            	/*JsonElement element;
            	try (InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                    element = GsonHelper.fromJson(GSON, reader, JsonElement.class);
                }*/
            	ResourceLocation key = resource.getKey();
                ResourceLocation location = new ResourceLocation(key.getNamespace(), key.getPath());
                String identifier = key.getNamespace() + ":" + convertToName(key.getPath().toString());
                resources.add(new AnimResource(identifier, location));
                //GunMod.LOGGER.info("Added animation resource " + identifier + " with resource location " + location);
            } catch (IOException e) {
            	GunMod.LOGGER.info("Failed to load animation file " + resource.getKey());
				e.printStackTrace();
			}
        };
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
                this.resourceToData.put(resource.identifier(), object); 
                GunMod.LOGGER.info("Loaded animation " + resource.identifier() + " at file location " + resource.location());
                //GunMod.LOGGER.info("Successfully paired data for animation resource " + resource.key());
            }
        });
    }

    @Override
    public boolean ignoreMissing()
    {
        return true;
    }

    /**
     * Helper function to convert resource name to string
     */
    public static String convertToName(String arg) {
        String output = arg.replace("animations/", "");
        output = output.replace(EXTENSION, "");
        return output;
    }

    public record AnimResource(String identifier, ResourceLocation location) implements IResourceSupplier
    {
        @Override
        public ResourceLocation getLocation()
        {
            return this.location;
        }
    }
}
