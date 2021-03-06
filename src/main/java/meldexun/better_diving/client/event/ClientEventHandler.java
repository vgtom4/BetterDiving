package meldexun.better_diving.client.event;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import meldexun.better_diving.BetterDiving;
import meldexun.better_diving.client.util.GuiHelper;
import meldexun.better_diving.config.BetterDivingConfig;
import meldexun.better_diving.util.OxygenPlayerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BetterDiving.MOD_ID, value = Dist.CLIENT)
public class ClientEventHandler {

	@SubscribeEvent
	public static void onRenderGameOverlayEventPre(RenderGameOverlayEvent.Pre event) {
		if (!BetterDivingConfig.CLIENT_CONFIG.oxygenGuiEnabled.get()) {
			return;
		}
		if (event.getType() == ElementType.AIR) {
			event.setCanceled(true);

			Minecraft mc = Minecraft.getInstance();
			if (OxygenPlayerHelper.getOxygenRespectEquipment(mc.player) < OxygenPlayerHelper.getOxygenCapacityRespectEquipment(mc.player)) {
				renderOxygenOverlay(event.getMatrixStack());
			}
		}
	}

	private static final ResourceLocation BACKGROUND = new ResourceLocation(BetterDiving.MOD_ID, "textures/gui/oxygen/oxygen_background.png");
	private static final ResourceLocation BAR = new ResourceLocation(BetterDiving.MOD_ID, "textures/gui/oxygen/oxygen_bar.png");
	private static final ResourceLocation BUBBLES = new ResourceLocation(BetterDiving.MOD_ID, "textures/gui/oxygen/oxygen_bubbles.png");
	private static final ResourceLocation FRAME = new ResourceLocation(BetterDiving.MOD_ID, "textures/gui/oxygen/oxygen_frame.png");

	private static int tick = 0;
	private static float partialTicks = 0.0F;
	private static float prevPartialTicks = 0.0F;

	@SuppressWarnings("deprecation")
	private static void renderOxygenOverlay(MatrixStack matrixStack) {
		updatePartialTicks();
		Minecraft mc = Minecraft.getInstance();
		TextureManager textureManager = mc.getTextureManager();
		FontRenderer fontRenderer = mc.fontRenderer;

		int oxygen = (int) Math.round(OxygenPlayerHelper.getOxygenRespectEquipment(mc.player) / 20.0D / 3.0D) * 3;
		double percent = (int) (OxygenPlayerHelper.getOxygenRespectEquipmentInPercent(mc.player) * 80.0D) / 80.0D;
		int x = GuiHelper.getAnchorX(102, BetterDivingConfig.CLIENT_CONFIG.oxygenGuiAnchor.get(), BetterDivingConfig.CLIENT_CONFIG.oxygenGuiOffsetX.get());
		int y = GuiHelper.getAnchorY(21, BetterDivingConfig.CLIENT_CONFIG.oxygenGuiAnchor.get(), BetterDivingConfig.CLIENT_CONFIG.oxygenGuiOffsetY.get());
		double offset;

		GlStateManager.enableBlend();
		GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ZERO, GL11.GL_ONE);
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.00390625F);

		textureManager.bindTexture(BACKGROUND);
		GuiHelper.drawTexture(x, y, 0.0D, 0.0D, 102, 21, 1.0D, 1.0D);

		textureManager.bindTexture(BAR);
		GuiHelper.drawTexture(x + 1.0D + 80.0D * (1.0D - percent), y + 7.0D, 1.0D - percent, mc.world.getGameTime() * 9.0D / 576.0D, 80.0D * percent, 7.0D, percent, 9.0D / 576.0D);

		textureManager.bindTexture(BUBBLES);
		offset = 2.0D * (tick + partialTicks) % 128 / 128.0D;
		drawBubbles(x + 1.0D, y + 7.0D, 0.0D, offset, percent);
		offset = 2.5D * (tick + partialTicks) % 128 / 128.0D;
		drawBubbles(x + 1.0D, y + 7.0D, 20.0D, offset + 0.45D, percent);
		offset = 1.5D * (tick + partialTicks) % 128 / 128.0D;
		drawBubbles(x + 1.0D, y + 7.0D, 35.0D, offset + 0.12D, percent);
		offset = 2.0D * (tick + partialTicks) % 128 / 128.0D;
		drawBubbles(x + 1.0D, y + 7.0D, 55.0D, offset + 0.68D, percent);

		textureManager.bindTexture(FRAME);
		GuiHelper.drawTexture(x, y, 0.0D, 0.0D, 102, 21, 1.0D, 1.0D);

		String s1 = Integer.toString(oxygen);
		fontRenderer.drawStringWithShadow(matrixStack, s1, x + 91 - fontRenderer.getStringWidth(s1) / 2, y + 11, 0xFFFFFF);
		String s2 = "O\u2082";
		fontRenderer.drawStringWithShadow(matrixStack, s2, x + 91 - fontRenderer.getStringWidth(s2) / 2, y + 2, 0xFFFFFF);

		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.disableBlend();
	}

	private static void updatePartialTicks() {
		Minecraft mc = Minecraft.getInstance();
		float f = mc.getRenderPartialTicks() - prevPartialTicks;
		if (f <= 0.0F) {
			f++;
		}
		partialTicks += f;
		tick += partialTicks / 1.0F;
		partialTicks = partialTicks % 1.0F;
		prevPartialTicks = mc.getRenderPartialTicks();
	}

	private static void drawBubbles(double x, double y, double xOffset, double vOffset, double percent) {
		double width = 128.0D / 6.0D;
		double height = 128.0D / 16.0D;
		xOffset = MathHelper.clamp(xOffset, 0.0D, 80.0D - width);
		percent = MathHelper.clamp(percent * 80.0D / width - (80.0D - width - xOffset) / width, 0.0D, 1.0D);
		GuiHelper.drawTexture(x + xOffset + width * (1.0D - percent), y, 1.0D - percent, vOffset, width * percent, height, percent, 0.375D);
	}

}
