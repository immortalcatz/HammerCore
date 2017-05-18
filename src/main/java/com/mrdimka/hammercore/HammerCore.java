package com.mrdimka.hammercore;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.ShapedOreRecipe;

import com.mrdimka.hammercore.annotations.MCFBus;
import com.mrdimka.hammercore.api.HammerCoreAPI;
import com.mrdimka.hammercore.api.IHammerCoreAPI;
import com.mrdimka.hammercore.api.IJavaCode;
import com.mrdimka.hammercore.api.IUpdatable;
import com.mrdimka.hammercore.api.RequiredDeps;
import com.mrdimka.hammercore.api.WrappedFMLLog;
import com.mrdimka.hammercore.api.mhb.IRayRegistry;
import com.mrdimka.hammercore.api.mhb.RaytracePlugin;
import com.mrdimka.hammercore.asm.CSVFile;
import com.mrdimka.hammercore.cfg.ConfigHolder;
import com.mrdimka.hammercore.cfg.HCModConfigurations;
import com.mrdimka.hammercore.cfg.IConfigReloadListener;
import com.mrdimka.hammercore.command.CommandBuildStructure;
import com.mrdimka.hammercore.command.CommandPosToLong;
import com.mrdimka.hammercore.command.CommandTPX;
import com.mrdimka.hammercore.common.capabilities.CapabilityEJ;
import com.mrdimka.hammercore.common.utils.AnnotatedInstanceUtil;
import com.mrdimka.hammercore.common.utils.HammerCoreUtils;
import com.mrdimka.hammercore.common.utils.IOUtils;
import com.mrdimka.hammercore.common.utils.WrappedLog;
import com.mrdimka.hammercore.event.AddCalculatronRecipeEvent;
import com.mrdimka.hammercore.event.GetAllRequiredApisEvent;
import com.mrdimka.hammercore.ext.TeslaAPI;
import com.mrdimka.hammercore.fluiddict.FluidDictionary;
import com.mrdimka.hammercore.gui.GuiManager;
import com.mrdimka.hammercore.init.ModBlocks;
import com.mrdimka.hammercore.init.ModItems;
import com.mrdimka.hammercore.init.SimpleRegistration;
import com.mrdimka.hammercore.net.HCNetwork;
import com.mrdimka.hammercore.proxy.AudioProxy_Common;
import com.mrdimka.hammercore.proxy.BookProxy_Common;
import com.mrdimka.hammercore.proxy.LightProxy_Common;
import com.mrdimka.hammercore.proxy.ParticleProxy_Common;
import com.mrdimka.hammercore.proxy.PipelineProxy_Common;
import com.mrdimka.hammercore.proxy.RenderProxy_Common;
import com.mrdimka.hammercore.recipeAPI.BrewingRecipe;
import com.mrdimka.hammercore.recipeAPI.IRecipePlugin;
import com.mrdimka.hammercore.recipeAPI.RecipePlugin;
import com.mrdimka.hammercore.recipeAPI.registry.GlobalRecipeScript;
import com.mrdimka.hammercore.recipeAPI.registry.RecipeTypeRegistry;
import com.mrdimka.hammercore.recipeAPI.registry.SimpleRecipeScript;
import com.mrdimka.hammercore.structure.StructureAPI;
import com.mrdimka.hammercore.world.WorldGenHammerCore;

/**
 * The core of Hammer Core.
 * <br><span style="text-decoration: underline;"><em>This really sounds weird :/</em></span>
 **/
@Mod(modid = "hammercore", version = "@VERSION@", name = "Hammer Core")
public class HammerCore
{
	public static final List<String> initHCChannels = new ArrayList<>();
	public static final boolean IS_OBFUSCATED_MC = false;
	
	/**
	 * Render proxy for HC used to handle complicated rendering codes in a simple way.
	 */
	@SidedProxy(modId = "hammercore", clientSide = "com.mrdimka.hammercore.proxy.RenderProxy_Client", serverSide = "com.mrdimka.hammercore.proxy.RenderProxy_Common")
	public static RenderProxy_Common renderProxy;
	
//	/**
//	 * All sources compiled from 'javacode' dir
//	 */
//	public static ClassLoader javaLoader;
	
	/**
	 * Audio proxy for HC used to interact with audio in any way
	 */
	@SidedProxy(modId = "hammercore", clientSide = "com.mrdimka.hammercore.proxy.AudioProxy_Client", serverSide = "com.mrdimka.hammercore.proxy.AudioProxy_Common")
	public static AudioProxy_Common audioProxy;
	
	/**
	 * Particle proxy for HC used to interact with particles from both sides.
	 */
	@SidedProxy(modId = "hammercore", clientSide = "com.mrdimka.hammercore.proxy.ParticleProxy_Client", serverSide = "com.mrdimka.hammercore.proxy.ParticleProxy_Common")
	public static ParticleProxy_Common particleProxy;
	
	@SidedProxy(modId = "hammercore", clientSide = "com.mrdimka.hammercore.proxy.LightProxy_Client", serverSide = "com.mrdimka.hammercore.proxy.LightProxy_Common")
	public static LightProxy_Common lightProxy;
	
	@SidedProxy(modId = "hammercore", clientSide = "com.mrdimka.hammercore.proxy.BookProxy_Client", serverSide = "com.mrdimka.hammercore.proxy.BookProxy_Common")
	public static BookProxy_Common bookProxy;
	
	@SidedProxy(modId = "hammercore", clientSide = "com.mrdimka.hammercore.proxy.PipelineProxy_Client", serverSide = "com.mrdimka.hammercore.proxy.PipelineProxy_Common")
	public static PipelineProxy_Common pipelineProxy;
	
	/**
	 * An instance of {@link HammerCore} class
	 **/
	@Instance("hammercore")
	public static HammerCore instance;
	
	/** Creative tab of HammerCore */
	public static final CreativeTabs tab = HammerCoreUtils.createDynamicCreativeTab("hammercore", 60);
	
	public static final Map<IHammerCoreAPI, HammerCoreAPI> APIS = new HashMap<>();
	
	public static final Set<IJavaCode> COMPILED_CODES = new HashSet<>();
	
	public static final WrappedLog LOG = new WrappedLog("Hammer Core");
	
	public static final CSVFile FIELD_CSV, METHODS_CSV;
	
	private List<IRayRegistry> raytracePlugins;
	private List<IRecipePlugin> recipePlugins;
	private List<ConfigHolder> configListeners;
	
	static
	{
		CSVFile f = null, m = null;
		
		f = new CSVFile(HammerCore.class.getResourceAsStream("/fields.csv"));
		m = new CSVFile(HammerCore.class.getResourceAsStream("/methods.csv"));
		
		FIELD_CSV = f;
		METHODS_CSV = m;
	}
	
	/**
	 * This method is used to construct proxies
	 */
	@EventHandler
	public void construct(FMLConstructionEvent e)
	{
		renderProxy.construct();
		audioProxy.construct();
		
		if(!FluidRegistry.isUniversalBucketEnabled())
			FluidRegistry.enableUniversalBucket();
		
		new FluidDictionary();
		
		initHCChannels.add("particles");
		
//		File javacode = new File(".", "javacode");
//		if(!javacode.isDirectory()) javacode.mkdir();
//		try
//		{
//			Map<String, byte[]> classes = JavaCodeLoader.compileRoot(javacode);
//			javaLoader = JavaCodeLoader.toLoader(classes);
//			for(String clas : classes.keySet())
//			{
//				try
//				{
////					GameRegistry.makeItemStack(itemName, meta, stackSize, nbtString)
//					Class cls = javaLoader.loadClass(clas);
//					IJavaCode code = null;
//					if(IJavaCode.class.isAssignableFrom(cls)) code = (IJavaCode) cls.newInstance();
//					else code = new IJavaCode.IJavaCode_IMPL(cls.newInstance());
//					COMPILED_CODES.add(code);
//					LOG.info("Added new JavaCode: " + code + " for " + clas);
//				}
//				catch(ClassNotFoundException cnfe)
//				{
//					LOG.error("Error: unexpected class " + clas + ". Perharps it has different package?");
//				}
//				catch(Throwable err) { err.printStackTrace(); }
//			}
//		} catch(Exception e1) { e1.printStackTrace(); }
	}
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent e)
	{
		CapabilityEJ.register();
		
		List<IHammerCoreAPI> apis = AnnotatedInstanceUtil.getInstances(e.getAsmData(), HammerCoreAPI.class, IHammerCoreAPI.class);
		List<Object> toRegister = AnnotatedInstanceUtil.getInstances(e.getAsmData(), MCFBus.class, Object.class);
		
		configListeners = new ArrayList<>();
		
		for(IConfigReloadListener listener : AnnotatedInstanceUtil.getInstances(e.getAsmData(), HCModConfigurations.class, IConfigReloadListener.class))
		{
			ConfigHolder h = new ConfigHolder(listener, new Configuration(listener.getSuggestedConfigurationFile()));
			h.reload();
			configListeners.add(h);
			LOG.info("Added \"" + h.getClass().getName() + "\" to Hammer Core Simple Configs.");
		}
		
		raytracePlugins = AnnotatedInstanceUtil.getInstances(e.getAsmData(), RaytracePlugin.class, IRayRegistry.class);
		recipePlugins = AnnotatedInstanceUtil.getInstances(e.getAsmData(), RecipePlugin.class, IRecipePlugin.class);
		
		for(IJavaCode code : COMPILED_CODES) //Add compiled codes
			code.addMCFObjects(toRegister);
		
		toRegister.add(this);
		
		for(Object o : toRegister)
		{
			MinecraftForge.EVENT_BUS.register(o);
			LOG.info("Added \"" + o + "\" to MCF Event Bus.");
		}
		
		LOG.info("Added " + toRegister.size() + " object to MCF Event Bus.");
		
		{
			GetAllRequiredApisEvent evt = new GetAllRequiredApisEvent();
			MinecraftForge.EVENT_BUS.post(evt);
			RequiredDeps.addRequests(evt);
		}
		
		for(IHammerCoreAPI api : apis)
		{
			HammerCoreAPI apia = api.getClass().getAnnotation(HammerCoreAPI.class);
			if(apia != null)
			{
				WrappedFMLLog log = new WrappedFMLLog(apia.name());
				api.init(log, apia.version());
				APIS.put(api, apia);
			}
		}
		
		new ModBlocks();
		SimpleRegistration.registerFieldItemsFrom(ModItems.class, "hammercore", HammerCore.tab);
		
		ModMetadata meta = e.getModMetadata();
		meta.autogenerated = false;
		meta.version = "@VERSION@";
		meta.authorList = Arrays.asList("APengu");
		
		for(IJavaCode code : COMPILED_CODES)
			code.preInit();
	}
	
	@EventHandler
	public void init(FMLInitializationEvent e)
	{
		renderProxy.init();
		bookProxy.init();
		HCNetwork.clinit();
		
		for(IJavaCode code : COMPILED_CODES)
			code.init();
		
		NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiManager());
		
		AddCalculatronRecipeEvent evt = new AddCalculatronRecipeEvent();
		evt.setRecipe(new ShapedOreRecipe(ModItems.calculatron, "igi", "rlr", "idi", 'g', "blockGlass", 'i', "ingotIron", 'r', "dustRedstone", 'd', "ingotGold", 'l', "dyeLime"));
		if(!MinecraftForge.EVENT_BUS.post(evt)) GameRegistry.addRecipe(evt.getRecipe());
		
		BrewingRecipeRegistry.addRecipe(BrewingRecipe.INSTANCE);
		
		GameRegistry.registerWorldGenerator(new WorldGenHammerCore(), 0);
		
		StructureAPI.registerSpawnableStructure(new ResourceLocation("hammercore", "well"));
	}
	
	public static final RecipeTypeRegistry registry = new RecipeTypeRegistry();
	private GlobalRecipeScript recipeScript;
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent e)
	{
		for(IJavaCode code : COMPILED_CODES) code.postInit();
		for(IRecipePlugin plugin : recipePlugins)
		{
			LOG.info("Registering recipe plugin: " + plugin.getClass().getName() + " ...");
			long start = System.currentTimeMillis();
			plugin.registerTypes(registry);
			LOG.info("Registered recipe  plugin: " + plugin.getClass().getName() + " in " + (System.currentTimeMillis() - start) + " ms");
		}
	}
	
	public static final List<IUpdatable> updatables = new ArrayList<>(4);
	
	@EventHandler
	public void serverStarting(FMLServerStartingEvent e)
	{
		e.registerServerCommand(new CommandPosToLong());
		e.registerServerCommand(new CommandTPX());
		e.registerServerCommand(new CommandBuildStructure());
		
		File hc_recipes_global = new File("hc-recipes");
		MinecraftServer server = e.getServer();
		File worldFolder = new File((server.isDedicatedServer() ? "" : "saves" + File.separator) + server.getFolderName(), "hc-recipes");
		worldFolder.mkdirs();
		hc_recipes_global.mkdirs();
		
		if(recipeScript != null) recipeScript.remove();
		List<SimpleRecipeScript> scripts = new ArrayList<>();
		scripts.addAll(Arrays.asList(parse(worldFolder).scripts));
		scripts.addAll(Arrays.asList(parse(hc_recipes_global).scripts));
		recipeScript = new GlobalRecipeScript(scripts.toArray(new SimpleRecipeScript[scripts.size()]));
		GRCProvider.reloadScript();
		
		reloadRaytracePlugins();
	}
	
	@SubscribeEvent
	public void serverTick(ServerTickEvent evt)
	{
		if(evt.side == Side.SERVER) for(int i = 0; i < updatables.size(); ++i)
		{
			try
			{
				IUpdatable upd = updatables.get(i);
				upd.update();
				if(!upd.isAlive()) updatables.remove(i);
			}
			catch(Throwable err) {}
		}
	}
	
	@SubscribeEvent
	public void configReloaded(ConfigChangedEvent evt)
	{
		String mid = evt.getModID();
		for(ConfigHolder holder : configListeners)
			if(holder.listener.getModid().equals(mid))
				holder.reload();
	}
	
	public void reloadRaytracePlugins()
	{
		TeslaAPI.refreshTeslaClassData();
		
		RayCubeRegistry.instance.cubes.clear();
		RayCubeRegistry.instance.mgrs.clear();
		for(IRayRegistry reg : raytracePlugins)
		{
			LOG.info("Registering raytrace plugin: " + reg.getClass().getName() + " ...");
			long start = System.currentTimeMillis();
			reg.registerCubes(RayCubeRegistry.instance);
			LOG.info("Registered raytrace  plugin: " + reg.getClass().getName() + " in " + (System.currentTimeMillis() - start) + " ms");
		}
	}
	
	@EventHandler
	public void serverStop(FMLServerStoppingEvent evt)
	{
		if(recipeScript != null) recipeScript.remove();
		recipeScript = null;
	}
	
	private GlobalRecipeScript parse(File path)
	{
		if(path.isDirectory())
		{
			List<SimpleRecipeScript> jsons = new ArrayList<>();
			for(File json : path.listFiles(new FileFilter()
			{
				@Override
				public boolean accept(File pathname)
				{
					return pathname.isFile() && pathname.getName().endsWith(".json");
				}
			}))
			{
				try
				{
					jsons.add(registry.parse(new String(IOUtils.pipeOut(new FileInputStream(json)))));
				}
				catch(Throwable err)
				{
					LOG.bigWarn("Failed to parse HammerCoreRecipeJson File:");
					err.printStackTrace();
				}
			}
			
			return new GlobalRecipeScript(jsons.toArray(new SimpleRecipeScript[jsons.size()]));
		}else if(path.isFile())
		{
			try
			{
				return new GlobalRecipeScript(registry.parse(new String(IOUtils.pipeOut(new FileInputStream(path)))));
			}
			catch(Throwable err)
			{
				LOG.bigWarn("Failed to parse HammerCoreRecipeJson File:");
				err.printStackTrace();
			}
		}
		return new GlobalRecipeScript();
	}
	
	public static class GRCProvider
	{
		public static int getScriptCount()
		{
			return instance.recipeScript.scripts.length;
		}
		
		public static void setScriptCount(int amt)
		{
			if(amt == 0) if(instance.recipeScript != null) { instance.recipeScript.remove(); instance.recipeScript = null; return; }
			
			if(instance.recipeScript == null) instance.recipeScript = new GlobalRecipeScript();
			instance.recipeScript.remove();
			SimpleRecipeScript[] old = instance.recipeScript.scripts;
			if(old.length == amt) return;
			instance.recipeScript.scripts = new SimpleRecipeScript[amt];
			for(int i = 0; i < Math.min(old.length, amt); ++i) instance.recipeScript.scripts[i] = old[i];
		}
		
		public static NBTTagList getScript(int id)
		{
			if(instance.recipeScript == null) return new NBTTagList();
			return id >= instance.recipeScript.scripts.length && instance.recipeScript.scripts[id].makeTag != null ? null : instance.recipeScript.scripts[id].makeTag.copy();
		}
		
		public static void setScript(int id, NBTTagList list)
		{
			if(instance.recipeScript == null) instance.recipeScript = new GlobalRecipeScript();
			instance.recipeScript.remove();
			setScriptCount(Math.max(instance.recipeScript.scripts.length, id + 1));
			instance.recipeScript.scripts[id] = registry.parse(list);
		}
		
		public static void reloadScript()
		{
			if(instance.recipeScript != null)
			{
				instance.recipeScript.remove();
				instance.recipeScript.add();
			}
		}
	}
}