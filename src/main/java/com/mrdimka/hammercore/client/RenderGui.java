package com.mrdimka.hammercore.client;

import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiBrewingStand;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiFurnace;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.MouseInputEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.mrdimka.hammercore.api.RequiredDeps;
import com.mrdimka.hammercore.cfg.HammerCoreConfigs;
import com.mrdimka.hammercore.client.renderer.RenderHelperImpl;
import com.mrdimka.hammercore.client.renderer.item.EnumItemRender;
import com.mrdimka.hammercore.client.renderer.item.IItemRenderer;
import com.mrdimka.hammercore.client.utils.GLImageManager;
import com.mrdimka.hammercore.client.utils.RenderUtil;
import com.mrdimka.hammercore.common.utils.IOUtils;
import com.mrdimka.hammercore.gui.GuiMissingApis;
import com.mrdimka.hammercore.gui.modbrowser.GuiModBrowserLoading;
import com.mrdimka.hammercore.gui.smooth.GuiBrewingStandSmooth;
import com.mrdimka.hammercore.gui.smooth.GuiFurnaceSmooth;
import com.mrdimka.hammercore.json.JSONArray;
import com.mrdimka.hammercore.json.JSONObject;
import com.mrdimka.hammercore.json.JSONTokener;
import com.mrdimka.hammercore.math.ExpressionEvaluator;
import com.mrdimka.hammercore.math.MathHelper;

@SideOnly(Side.CLIENT)
public class RenderGui
{
	private static final UV hammer = new UV(new ResourceLocation("hammercore", "textures/hammer.png"), 0, 0, 256, 256);
	private static final ResourceLocation main_menu_widgets = new ResourceLocation("hammercore", "textures/gui/main_menu_widgets.png");
	private static final SpecialUser user = new SpecialUser();
	private double modListHoverTip = 0;
	
	@SubscribeEvent
	public void guiRender(DrawScreenEvent.Post e)
	{
		GuiScreen gui = e.getGui();
		
		if(gui instanceof GuiMainMenu)
		{
			if(HammerCoreConfigs.client_modBrowser)
			{
				int mouseX = e.getMouseX();
				int mouseY = e.getMouseY();
				
				int xOff = gui.width / 2 - 15;
				
				boolean isHovered = mouseX >= xOff && mouseY >= 0 && mouseX < xOff + 30 && mouseY < 3;
				isHovered |= mouseX >= xOff + 3 && mouseY >= 0 && mouseX < xOff + 27 && mouseY < modListHoverTip + 9;
				
				if(isHovered) modListHoverTip = MathHelper.clip(modListHoverTip + .8D, 0, 12);
				else modListHoverTip = MathHelper.clip(modListHoverTip - .8D, 0, 12);
				
				gui.mc.getTextureManager().bindTexture(main_menu_widgets);
				
				GL11.glPushMatrix();
				GL11.glTranslated(xOff, 0, 0);
				GLRenderState.BLEND.captureState();
				GLRenderState.BLEND.on();
				
				GL11.glColor4d(1, 1, 1, 1);
				
				RenderUtil.drawTexturedModalRect(0, 0, 0, 0, 30, 4);
				RenderUtil.drawTexturedModalRect(0, 4, 0, 4, 30, modListHoverTip);
				RenderUtil.drawTexturedModalRect(0, 4 + modListHoverTip, 0, 16, 30, 5);
				
				GL11.glColor4d(1, 1, 1, modListHoverTip / 12D);
				
				RenderUtil.drawTexturedModalRect(0, 0, 30, 0, 30, 4);
				RenderUtil.drawTexturedModalRect(0, 4, 30, 4, 30, modListHoverTip);
				RenderUtil.drawTexturedModalRect(0, 4 + modListHoverTip, 30, 16, 30, 5);
				
				GL11.glColor4d(1, 1, 1, 1);
				
				float f = 1F - net.minecraft.util.math.MathHelper.abs(net.minecraft.util.math.MathHelper.sin((float)((Minecraft.getSystemTime() + 47647L) % 10000L) / 10000.0F * ((float)Math.PI * 2F)) * 0.1F);
				
				GL11.glPushMatrix();
				GL11.glTranslated(14 - (f * 8) + 8, modListHoverTip - 3 - (f * 8) + 8, 0);
				GL11.glRotated((12 - modListHoverTip) / 12 * -90, 0, 0, 1);
				GL11.glTranslated(-8, -8, 0);
				
				hammer.render(0, 0, 16 * f, 16 * f);
				GL11.glPopMatrix();
				
				GLRenderState.BLEND.reset();
				GL11.glPopMatrix();
			}
			
			user.draw();
		}
		
		if(gui instanceof GuiContainer)
		{
			GuiContainer gc = (GuiContainer) gui;
			
			int guiLeft = 0, guiTop = 0;
			try
			{
				guiLeft = ObfuscationReflectionHelper.getPrivateValue(GuiContainer.class, gc, "guiLeft", "field_147003_i");
				guiTop = ObfuscationReflectionHelper.getPrivateValue(GuiContainer.class, gc, "guiTop", "field_147009_r");
			}
			catch(Throwable err) {}
			
			Container c = gc.inventorySlots;
			for(int i = 0; i < c.inventorySlots.size(); ++i)
			{
				ItemStack stack = c.inventorySlots.get(i).getStack();
				if(stack != null)
				{
					Slot sl = c.getSlot(i);
					
					IItemRenderer renderer = RenderHelperImpl.INSTANCE.getRenderFor(stack, EnumItemRender.GUI);
					if(renderer != null)
					{
						GlStateManager.disableDepth();
						renderer.render(EnumItemRender.GUI, stack, guiLeft + sl.xPos, guiTop + sl.yPos, 0);
						GlStateManager.enableDepth();
					}
				}
			}
			
			ItemStack stack = gc.mc.player.inventory.getItemStack();
			if(stack != null)
			{
				guiLeft = e.getMouseX();
				guiTop = e.getMouseY();
				IItemRenderer renderer = RenderHelperImpl.INSTANCE.getRenderFor(stack, EnumItemRender.GUI);
				if(renderer != null) renderer.render(EnumItemRender.GUI, stack, guiLeft - 8, guiTop - 8, 0);
			}
		}
	}
	
	@SubscribeEvent
	public void mouseClick(MouseInputEvent.Pre evt)
	{
		int mouseX = Mouse.getEventX() * evt.getGui().width / evt.getGui().mc.displayWidth;
        int mouseY = evt.getGui().height - Mouse.getEventY() * evt.getGui().height / evt.getGui().mc.displayHeight - 1;
        int eventButton = Mouse.getEventButton();

        if(Mouse.getEventButtonState())
        {
        	GuiScreen gui = evt.getGui();
        	
        	if(gui instanceof GuiMainMenu)
        	{
        		int xOff = evt.getGui().width / 2 - 15;
    			
    			boolean isHovered = mouseX >= xOff && mouseY >= 0 && mouseX < xOff + 30 && mouseY < 3;
    			isHovered |= mouseX >= xOff + 3 && mouseY >= 0 && mouseX < xOff + 27 && mouseY < modListHoverTip + 9;
    			
    			if(isHovered && eventButton == 0 && HammerCoreConfigs.client_modBrowser)
    			{
    				modListHoverTip = 0;
    				Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1));
    				GuiModBrowserLoading gui0;
    				gui.mc.displayGuiScreen(gui0 = new GuiModBrowserLoading());
    				try
    				{
    					Field f0 = GuiMainMenu.class.getDeclaredFields()[5];
    					f0.setAccessible(true);
    					Field f1 = GuiModBrowserLoading.class.getDeclaredFields()[3];
    					f1.setAccessible(true);
    					f1.setInt(gui0, f0.getInt(gui));
    				}catch(Throwable err) {}
    			}
        	}
        }
	}
	
	@SubscribeEvent
	public void openGui(GuiOpenEvent evt)
	{
		GuiScreen gui = evt.getGui();
		final GuiScreen fgui = gui;
		
		if(gui instanceof GuiMainMenu)
		{
			new Thread(()->
			{
				user.download();
				user.reload(true);
			}).start();
		}
		
		if(gui instanceof GuiMainMenu && !RequiredDeps.allDepsResolved()) gui = new GuiMissingApis();
		
		smooth:
		{
			//@since 1.5.1
			if(!HammerCoreConfigs.client_smoothVanillaGuis) break smooth;
			
			if(gui instanceof GuiFurnace)
			{
				try
				{
					Field[] fs = GuiFurnace.class.getDeclaredFields();
					
					Field pinv = fs[1];
					pinv.setAccessible(true);
					
					Field furn = fs[2];
					furn.setAccessible(true);
					
					InventoryPlayer playerInv = (InventoryPlayer) pinv.get(gui);
					IInventory furnaceInv = (IInventory) furn.get(gui);
					
					gui = new GuiFurnaceSmooth(playerInv, furnaceInv);
				}
				catch(Throwable err) { err.printStackTrace(); }
			}
			
			if(gui instanceof GuiBrewingStand)
			{
				try
				{
					Field[] fs = GuiBrewingStand.class.getDeclaredFields();
					
					Field pinv = fs[2];
					pinv.setAccessible(true);
					
					Field bs = fs[3];
					bs.setAccessible(true);
					
					InventoryPlayer playerInv = (InventoryPlayer) pinv.get(gui);
					IInventory tbsInv = (IInventory) bs.get(gui);
					
					gui = new GuiBrewingStandSmooth(playerInv, tbsInv);
				}
				catch(Throwable err) {}
			}
			
			break smooth;
		}
		
		if(fgui != gui) evt.setGui(gui);
	}
	
	@SideOnly(Side.CLIENT)
	private static final class SpecialUser
	{
		private final int glImage = GL11.glGenTextures();
		private final List<IMG> images = new ArrayList<>();
		private long lastDownload = 0L;
		private final SecureRandom rand = new SecureRandom();
		
		private double x, y, width, height;
		private IMG currImg;
		
		private void download()
		{
			try
			{
				JSONArray arr = (JSONArray) new JSONTokener(new String(IOUtils.downloadData("http://pastebin.com/raw/JKDpcHL1"))).nextValue();
				
				for(int i = 0; i < arr.length(); ++i)
				{
					JSONObject obj = arr.getJSONObject(i);
					if(obj.optBoolean("enabled", false) && obj.getString("username").equals(Minecraft.getMinecraft().getSession().getUsername()))
					{
						arr = obj.getJSONArray("images");
						break;
					}
				}
				
				images.clear();
				
				for(int i = 0; i < arr.length(); ++i)
				{
					JSONObject o = arr.getJSONObject(i);
					IMG img = new IMG();
					img.img = IOUtils.downloadPicture(o.getString("url"));
					JSONObject signature = o.getJSONObject("signature");
					img.x = signature.getString("x");
					img.y = signature.getString("y");
					img.width = signature.getString("width");
					img.height = signature.getString("height");
					images.add(img);
				}
			}
			catch(Throwable err) {}
		}
		
		private boolean reload(boolean launchThread)
		{
			try
			{
				if(images.isEmpty())
				{
					currImg = null;
					x = y = width = height = 0;
					return true;
				}
				
				IMG i = currImg = images.get(rand.nextInt(images.size()));
				
				String sx = i.x, sy = i.y, sw = i.width, sh = i.height;
				
				sx = format(sx);
				sy = format(sy);
				sw = format(sw);
				sh = format(sh);
				
				x = ExpressionEvaluator.evaluateDouble(sx);
				y = ExpressionEvaluator.evaluateDouble(sy);
				width = ExpressionEvaluator.evaluateDouble(sw);
				height = ExpressionEvaluator.evaluateDouble(sh);
				
				return true;
			}
			catch(Throwable err)
			{
				if(launchThread) new Thread(()->
				{
					int i = 0;
					while(++i < 5 && !reload(false));
				}).start();
			}
			
			return false;
		}
		
		private String format(String s)
		{
			if(s == null) return "0";
			Minecraft mc = Minecraft.getMinecraft();
			ScaledResolution sr = new ScaledResolution(mc);
			GuiScreen gs = mc.currentScreen;
			
			double displacex = sr.getScaledWidth_double() / sr.getScaledWidth();
			double displacey = sr.getScaledHeight_double() / sr.getScaledHeight();
			
//			s = s.replaceAll("mc-width", (mc.displayWidth * displacex) + "");
			s = s.replaceAll("mc-width", (mc.displayWidth) + "");
			s = s.replaceAll("mc-height", (mc.displayHeight) + "");
//			s = s.replaceAll("mc-height", (mc.displayHeight * displacey) + "");
			
			return s;
		}
		
		private void draw()
		{
			if(currImg == null || currImg.img == null) return;
			
			if(System.currentTimeMillis() - lastDownload > 10000L)
			{
				GLImageManager.loadTexture(currImg.img, glImage, false);
				lastDownload = System.currentTimeMillis();
			}
			
			GlStateManager.bindTexture(glImage);
			
			GL11.glPushMatrix();
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glTranslated(x, y, 0F);
			GL11.glScaled(width, height, 1D);
			GL11.glScaled((1D / currImg.img.getWidth()), (1D / currImg.img.getHeight()), 1D);
			RenderUtil.drawTexturedModalRect(0, 0, 0, 0, 256, 256);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glPopMatrix();
		}
	}
	
	@SideOnly(Side.CLIENT)
	private static final class IMG
	{
		private BufferedImage img;
		private String x, y, width, height;
	}
}