package com.mrdimka.hammercore.event;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

import com.mrdimka.hammercore.annotations.MCFBus;
import com.mrdimka.hammercore.common.IWrenchItem;
import com.mrdimka.hammercore.common.utils.WorldUtil;
import com.mrdimka.hammercore.net.HCNetwork;
import com.mrdimka.hammercore.tile.ITileDroppable;
import com.mrdimka.hammercore.tile.TileSyncable;
import com.pengu.hammercore.common.IWrenchable;
import com.pengu.hammercore.utils.WorldLocation;

@MCFBus
public class TileHandler
{
	@SubscribeEvent
	public void breakBlock(BlockEvent.BreakEvent evt)
	{
		IBlockState state = evt.getState();
		if(state.getBlock() instanceof ITileDroppable)
			((ITileDroppable) state.getBlock()).createDrop(evt.getPlayer(), evt.getWorld(), evt.getPos());
		TileEntity te = evt.getWorld().getTileEntity(evt.getPos());
		if(te instanceof ITileDroppable)
			((ITileDroppable) te).createDrop(evt.getPlayer(), evt.getWorld(), evt.getPos());
	}
	
	@SubscribeEvent(priority = EventPriority.NORMAL)
	public void playerInteract(PlayerInteractEvent.RightClickBlock evt)
	{
		EntityPlayer player = evt.getEntityPlayer();
		if(player != null && !player.getHeldItem(evt.getHand()).isEmpty())
		{
			IWrenchItem item = WorldUtil.cast(player.getHeldItem(evt.getHand()).getItem(), IWrenchItem.class);
			if(item != null && item.canWrench(player.getHeldItem(evt.getHand())))
			{
				MinecraftForge.EVENT_BUS.post(new WrenchEvent(player, evt.getPos(), evt.getHand(), evt.getFace()));
				
				item.onWrenchUsed(player, evt.getPos(), evt.getHand());
				
				WorldLocation wl = new WorldLocation(evt.getWorld(), evt.getPos());
				
				if(wl.getBlock() instanceof IWrenchable && ((IWrenchable) wl.getBlock()).onWrenchUsed(wl, player, evt.getHand()))
				{
					HCNetwork.swingArm(player, evt.getHand());
					evt.setCanceled(true);
				}
				else if(wl.getTile() instanceof IWrenchable && ((IWrenchable) wl.getTile()).onWrenchUsed(wl, player, evt.getHand()))
				{
					HCNetwork.swingArm(player, evt.getHand());
					evt.setCanceled(true);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void place(BlockEvent.PlaceEvent evt)
	{
		TileEntity te = evt.getWorld().getTileEntity(evt.getPos());
		
		if(te == null)
		{
			Block block = evt.getPlacedBlock().getBlock();
			if(block instanceof ITileEntityProvider)
			{
				te = block.createTileEntity(evt.getWorld(), evt.getPlacedBlock());
				evt.getWorld().setTileEntity(evt.getPos(), te);
			}
		}
		
		if(te instanceof TileSyncable)
		{
			TileSyncable tile = (TileSyncable) te;
			tile.onPlacedBy(evt.getPlayer(), evt.getHand());
		}
	}
	
	private long lastCheck;
	
	@SubscribeEvent
	public void serverTick(ServerTickEvent evt)
	{
		long currentCheck = System.currentTimeMillis() / 50L;
		
		if(currentCheck - lastCheck > 40)
		{
//			 TileTesseract.revalidateTesseracts();
			lastCheck = currentCheck;
		}
	}
}