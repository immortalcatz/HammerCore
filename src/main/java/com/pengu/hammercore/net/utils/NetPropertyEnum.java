package com.pengu.hammercore.net.utils;

import javax.annotation.Nonnull;

import net.minecraft.nbt.NBTTagCompound;

public class NetPropertyEnum<T extends Enum> extends NetPropertyAbstract<T>
{
	public NetPropertyEnum(IPropertyChangeHandler handler)
	{
		super(handler);
	}
	
	public NetPropertyEnum(IPropertyChangeHandler handler, T initialValue)
	{
		super(handler, initialValue);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		if(value != null)
		{
			nbt.setInteger("Val", value.ordinal());
			nbt.setString("EClass", value.getClass().getName());
		}
		return nbt;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		if(nbt.hasKey("EClass") && nbt.hasKey("Val"))
		{
			try
			{
				Class<T> c = (Class<T>) Class.forName(nbt.getString("EClass"));
				T[] values = (T[]) c.getMethod("values").invoke(null);
				value = values[nbt.getInteger("Val")];
			}
			catch(Throwable err) {}
		}
	}
}