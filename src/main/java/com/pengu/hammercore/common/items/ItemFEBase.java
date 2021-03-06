package com.pengu.hammercore.common.items;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import com.mrdimka.hammercore.common.capabilities.ItemCapabilityProvider;
import com.mrdimka.hammercore.energy.IPowerContainerItem;
import com.mrdimka.hammercore.energy.IPowerStorage;

public class ItemFEBase extends Item implements IPowerContainerItem
{
	protected int maxFE, maxExtract, maxReceive;
	
	/**
	 * change this if you want a different-colored durability bar for this
	 * energy item.
	 */
	protected int durabilityRGB = 0xFF2222;
	
	public ItemFEBase(int maxFE)
	{
		this.maxFE = maxFE;
		setMaxStackSize(1);
		setMaxDamage(255);
	}
	
	public int getMaxFE()
	{
		return maxFE;
	}
	
	public int getMaxExtract()
	{
		return maxExtract;
	}
	
	public int getMaxReceive()
	{
		return maxReceive;
	}
	
	@Override
	public int getRGBDurabilityForDisplay(ItemStack stack)
	{
		float damage = Math.max(0F, (float) (stack.getMaxDamage() - stack.getItemDamage()) / stack.getMaxDamage());
		
		int target = (int) (damage * 255F);
		target = (target << 16) | (target << 8) | target;
		
		return MathHelper.multiplyColor(target, durabilityRGB);
	}
	
	@Override
	public void getSubItems(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> subItems)
	{
		ItemStack stack = new ItemStack(itemIn);
		subItems.add(stack);
		setFE(stack, 0);
		
		stack = new ItemStack(itemIn);
		subItems.add(stack);
		setFE(stack, maxFE);
	}
	
	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
	{
		super.addInformation(stack, playerIn, tooltip, advanced);
		int fe = getEnergyStored(stack);
		tooltip.add(I18n.translateToLocal("info.hammercore:energy_stored") + ": " + fe);
		if(this instanceof ItemBattery)
			tooltip.add(I18n.translateToLocal("info.hammercore:changed_mode") + ": " + ((ItemBattery) this).getMode(stack));
	}
	
	@Override
	public ItemCapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt)
	{
		ItemCapabilityProvider provider = new ItemCapabilityProvider();
		provider.putCapability(CapabilityEnergy.ENERGY, new ItemEnergyHolder(stack));
		return provider;
	}
	
	private void setFE(ItemStack stack, int fe)
	{
		if(stack.getTagCompound() == null)
			stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setInteger("EnergyStored", fe);
		stack.setItemDamage((int) com.mrdimka.hammercore.math.MathHelper.clip((int) (256 - (fe / (double) maxFE) * 255D), 0, getMaxDamage()));
	}
	
	@Override
	public int receiveEnergy(ItemStack stack, int maxReceive, boolean simulate)
	{
		if(this.maxReceive <= 0)
			return 0;
		int energy = getEnergyStored(stack);
		int energyReceived = Math.min(ItemFEBase.this.maxFE - energy, Math.min(ItemFEBase.this.maxReceive, maxReceive));
		if(!simulate)
			setFE(stack, energy + energyReceived);
		return energyReceived;
	}
	
	@Override
	public int extractEnergy(ItemStack stack, int maxExtract, boolean simulate)
	{
		if(this.maxExtract <= 0)
			return 0;
		int energy = getEnergyStored(stack);
		int energyExtracted = Math.min(energy, Math.min(ItemFEBase.this.maxExtract, maxExtract));
		if(!simulate)
			setFE(stack, energy - energyExtracted);
		return energyExtracted;
	}
	
	@Override
	public int getEnergyStored(ItemStack stack)
	{
		if(stack.getTagCompound() == null)
			stack.setTagCompound(new NBTTagCompound());
		return stack.getTagCompound().getInteger("EnergyStored");
	}
	
	@Override
	public int getMaxEnergyStored(ItemStack stack)
	{
		return maxFE;
	}
	
	public class ItemEnergyHolder implements IEnergyStorage, IPowerStorage
	{
		public final ItemStack stack;
		
		public ItemEnergyHolder(ItemStack stack)
		{
			this.stack = stack;
		}
		
		@Override
		public int receiveEnergy(int maxReceive, boolean simulate)
		{
			return ItemFEBase.this.receiveEnergy(stack, maxReceive, simulate);
		}
		
		@Override
		public int extractEnergy(int maxExtract, boolean simulate)
		{
			return ItemFEBase.this.extractEnergy(stack, maxExtract, simulate);
		}
		
		@Override
		public int getEnergyStored()
		{
			return ItemFEBase.this.getEnergyStored(stack);
		}
		
		@Override
		public int getMaxEnergyStored()
		{
			return maxFE;
		}
		
		@Override
		public boolean canExtract()
		{
			return ItemFEBase.this.maxExtract > 0;
		}
		
		@Override
		public boolean canReceive()
		{
			return ItemFEBase.this.maxReceive > 0;
		}
	}
}