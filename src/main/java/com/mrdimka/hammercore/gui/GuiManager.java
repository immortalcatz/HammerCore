package com.mrdimka.hammercore.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;

import com.mrdimka.hammercore.HammerCore;
import com.mrdimka.hammercore.common.utils.WorldUtil;
import com.mrdimka.hammercore.gui.container.ContainerEmpty;
import com.mrdimka.hammercore.tile.TileSyncable;

public class GuiManager implements IGuiHandler
{
	private static final List<IGuiCallback> callbacks = new ArrayList<>(64);
	private static final int lastUsedBuiltintId = 2;
	
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		if(ID == 0)
		{
			TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
			TileSyncable syncable = WorldUtil.cast(te, TileSyncable.class);
			
			if(syncable != null)
				return syncable.getServerGuiElement(player);
		}
		
		if(ID == 1)
			return new ContainerEmpty();
		
		if(callbacks.size() + lastUsedBuiltintId > ID)
		{
			IGuiCallback c = callbacks.get(ID - lastUsedBuiltintId);
			if(c != null)
				return c.getServerGuiElement(player, world, new BlockPos(x, y, z));
		}
		
		return null;
	}
	
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		if(ID == 0)
		{
			TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
			TileSyncable syncable = WorldUtil.cast(te, TileSyncable.class);
			
			if(syncable != null)
				return syncable.getClientGuiElement(player);
		}
		
		if(ID == 1)
			return new GuiCalculator();
		
		if(callbacks.size() + lastUsedBuiltintId > ID)
		{
			IGuiCallback c = callbacks.get(ID - lastUsedBuiltintId);
			if(c != null)
				return c.getClientGuiElement(player, world, new BlockPos(x, y, z));
		}
		
		return null;
	}
	
	public static void openGui(EntityPlayer player, TileSyncable tile)
	{
		if(player != null && tile != null && !player.world.isRemote && tile.hasGui())
			FMLNetworkHandler.openGui(player, HammerCore.instance, 0, player.world, tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ());
	}
	
	public static void registerGuiCallback(IGuiCallback callback)
	{
		callback.setGuiID(callbacks.size());
		callbacks.add(callback);
	}
	
	public static void openGuiCallback(int callbackID, EntityPlayer player, World world, BlockPos pos)
	{
		if(!world.isRemote)
			FMLNetworkHandler.openGui(player, HammerCore.instance, 2 + callbackID, world, pos.getX(), pos.getY(), pos.getZ());
	}
}