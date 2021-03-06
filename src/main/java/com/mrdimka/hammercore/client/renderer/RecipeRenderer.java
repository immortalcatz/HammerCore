package com.mrdimka.hammercore.client.renderer;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import org.lwjgl.opengl.GL11;

public class RecipeRenderer
{
	public static void selectAndRender(IRecipe recipe)
	{
		if(recipe instanceof ShapedOreRecipe)
			render((ShapedOreRecipe) recipe);
		if(recipe instanceof ShapelessOreRecipe)
			render((ShapelessOreRecipe) recipe);
		if(recipe instanceof ShapedRecipes)
			render((ShapedRecipes) recipe);
		if(recipe instanceof ShapelessRecipes)
			render((ShapelessRecipes) recipe);
	}
	
	public static void render(ShapedOreRecipe recipe)
	{
		Object[] items = recipe.getInput();
		renderPattern(items);
	}
	
	public static void render(ShapedRecipes recipe)
	{
		Object[] items = recipe.recipeItems;
		renderPattern(items);
	}
	
	public static void render(ShapelessOreRecipe recipe)
	{
		renderPattern(recipe.getInput().toArray());
	}
	
	public static void render(ShapelessRecipes recipe)
	{
		renderPattern(recipe.recipeItems.toArray());
	}
	
	public static void renderPattern(Object[] recipe)
	{
		GL11.glPushMatrix();
		
		if(recipe.length <= 4)
			for(int i = 0; i < Math.min(recipe.length, 4); ++i)
			{
				int x = i % 2;
				int y = i / 2;
				Object o = recipe[i];
				
				GL11.glPushMatrix();
				GlStateManager.disableLighting();
				RenderHelper.enableGUIStandardItemLighting();
				if(o instanceof ItemStack)
					Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI((ItemStack) o, x * 18, y * 18);
				else if(o instanceof ItemStack[])
				{
					ItemStack[] ss = (ItemStack[]) o;
					Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(ss[getInRange(500, 0, ss.length)], x * 18, y * 18);
				} else if(o instanceof List && ((List) o).size() > 0 && ((List) o).get(0) instanceof ItemStack)
				{
					List<ItemStack> ss = (List<ItemStack>) o;
					Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(ss.get(getInRange(500, 0, ss.size())), x * 18, y * 18);
				}
				GL11.glPopMatrix();
			}
		
		if(recipe.length >= 9)
			for(int i = 0; i < Math.min(recipe.length, 9); ++i)
			{
				int x = i % 3;
				int y = i / 3;
				Object o = recipe[i];
				
				GL11.glPushMatrix();
				GlStateManager.disableLighting();
				RenderHelper.enableGUIStandardItemLighting();
				if(o instanceof ItemStack)
					Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI((ItemStack) o, x * 18, y * 18);
				else if(o instanceof ItemStack[])
				{
					ItemStack[] ss = (ItemStack[]) o;
					Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(ss[getInRange(500, 0, ss.length)], x * 18, y * 18);
				} else if(o instanceof List && ((List) o).size() > 0 && ((List) o).get(0) instanceof ItemStack)
				{
					List<ItemStack> ss = (List<ItemStack>) o;
					Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(ss.get(getInRange(500, 0, ss.size())), x * 18, y * 18);
				}
				GL11.glPopMatrix();
			}
		
		GL11.glPopMatrix();
	}
	
	public static int getInRange(int delayMillis, int min, int max)
	{
		int sys = (int) (System.currentTimeMillis() % ((long) (max * delayMillis))) / delayMillis;
		return sys;
	}
}