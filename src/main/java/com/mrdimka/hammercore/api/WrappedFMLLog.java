package com.mrdimka.hammercore.api;

import net.minecraftforge.fml.common.FMLLog;

import org.apache.logging.log4j.Level;

/**
 * Implementation for {@link ILog}. Used in
 * {@link IHammerCoreAPI#init(ILog, String)}
 */
public class WrappedFMLLog implements ILog
{
	public final String owner;
	
	public WrappedFMLLog(String owner)
	{
		this.owner = owner;
	}
	
	@Override
	public void info(String message, Object... params)
	{
		FMLLog.log("HammerCore|" + owner, Level.INFO, message, params);
	}
	
	@Override
	public void warn(String message, Object... params)
	{
		FMLLog.log("HammerCore|" + owner, Level.WARN, message, params);
	}
	
	@Override
	public void error(String message, Object... params)
	{
		FMLLog.log("HammerCore|" + owner, Level.ERROR, message, params);
	}
	
	@Override
	public void finer(String message, Object... params)
	{
		FMLLog.log("HammerCore|" + owner, Level.TRACE, message, params);
	}
}