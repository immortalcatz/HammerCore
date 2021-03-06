package com.mrdimka.hammercore.api.dynlight;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import com.mrdimka.hammercore.HammerCore;

public class ProxiedDynlightGetter
{
	public static int getLightValue(IBlockState blockState, IBlockAccess world, BlockPos pos)
	{
		int vanillaValue = blockState.getLightValue(world, pos);
		int custom = HammerCore.particleProxy.getLightValue(blockState, world, pos);
		return Math.max(vanillaValue, custom);
	}
}