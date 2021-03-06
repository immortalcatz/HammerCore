package com.mrdimka.hammercore.common.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;

import com.mrdimka.hammercore.HammerCore;

public final class ItemCalculatron extends Item
{
	public ItemCalculatron()
	{
		setUnlocalizedName("calculatron");
		setMaxStackSize(1);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn)
	{
		if(!worldIn.isRemote)
			FMLNetworkHandler.openGui(playerIn, HammerCore.instance, 1, worldIn, 0, 0, 0);
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}
}