package com.mrdimka.hammercore.tile;

import java.util.Random;

import net.minecraft.util.ITickable;

import com.pengu.hammercore.net.utils.NetPropertyAbstract;

public abstract class TileSyncableTickable extends TileSyncable implements ITickable
{
	public int changes = 0;
	public int ticksExisted = 0;
	
	/** If changed in the constructor, may change random's seed. */
	protected boolean positionedRandom = false;
	
	{
		rand = null;
	}
	
	@Override
	public Random getRNG()
	{
		/** Make unique random for each position */
		if(rand == null)
			rand = new Random(positionedRandom && world != null && pos != null ? (world.getSeed() + pos.toLong() + world.provider.getDimension() * 3L) : world.rand.nextLong());
	    return rand;
	}
	
	@Override
	public final void update()
	{
		getLocation();
		
		/** Make unique random for each position */
		if(rand == null)
			rand = new Random(positionedRandom ? (world.getSeed() + pos.toLong() + world.provider.getDimension() * 3L) : world.rand.nextLong());
		
		ticksExisted++;
		tick();
		
		if(changes > 0)
		{
			changes = 0;
			sync();
		}
	}
	
	public void tick()
	{
	}
	
	@Override
	public void notifyOfChange(NetPropertyAbstract prop)
	{
		changes++;
	}
	
	@Override
	public void sendChangesToNearby()
	{
		changes++;
	}
}