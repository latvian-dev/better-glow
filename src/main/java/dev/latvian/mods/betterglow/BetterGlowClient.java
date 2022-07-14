package dev.latvian.mods.betterglow;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import dev.latvian.mods.betterglow.mixin.WorldRendererAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class BetterGlowClient implements ClientModInitializer {
	public static float nearSize = 4F;
	public static float farSize = 1F;
	public static float nearDistance = 8F;
	public static float farDistance = 60F;
	public static boolean noClip = true;
	public static float visibility = 1F;
	public static float quality = 6F;

	@Override
	public void onInitializeClient() {
		WorldRenderEvents.AFTER_SETUP.register(this::renderAfterSetup);

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, commandRegistryAccess) -> dispatcher.register(ClientCommandManager.literal("betterglow")
			.then(ClientCommandManager.literal("near")
				.then(ClientCommandManager.literal("size")
					.then(ClientCommandManager.argument("value", FloatArgumentType.floatArg(1F, 25F))
						.executes(ctx -> {
							nearSize = FloatArgumentType.getFloat(ctx, "value");
							return 1;
						})
					)
				)
				.then(ClientCommandManager.literal("distance")
					.then(ClientCommandManager.argument("value", FloatArgumentType.floatArg(0F, 1000F))
						.executes(ctx -> {
							nearDistance = FloatArgumentType.getFloat(ctx, "value");
							return 1;
						})
					)
				)
			)
			.then(ClientCommandManager.literal("far")
				.then(ClientCommandManager.literal("size")
					.then(ClientCommandManager.argument("value", FloatArgumentType.floatArg(1F, 25F))
						.executes(ctx -> {
							farSize = FloatArgumentType.getFloat(ctx, "value");
							return 1;
						})
					)
				)
				.then(ClientCommandManager.literal("distance")
					.then(ClientCommandManager.argument("value", FloatArgumentType.floatArg(0F, 1000F))
						.executes(ctx -> {
							farDistance = FloatArgumentType.getFloat(ctx, "value");
							return 1;
						})
					)
				)
			)
			.then(ClientCommandManager.literal("fixedSize")
				.then(ClientCommandManager.argument("value", FloatArgumentType.floatArg(1F, 25F))
					.executes(ctx -> {
						nearSize = farSize = FloatArgumentType.getFloat(ctx, "value");
						return 1;
					})
				)
			)
			.then(ClientCommandManager.literal("visibility")
				.then(ClientCommandManager.argument("percent", FloatArgumentType.floatArg(0F, 100F))
					.executes(ctx -> {
						visibility = FloatArgumentType.getFloat(ctx, "percent") / 100F;
						return 1;
					})
				)
			)
			.then(ClientCommandManager.literal("quality")
				.then(ClientCommandManager.argument("quality", FloatArgumentType.floatArg(2F, 24F))
					.executes(ctx -> {
						quality = FloatArgumentType.getFloat(ctx, "quality");
						return 1;
					})
				)
			)
			.then(ClientCommandManager.literal("noclip")
				.then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
					.executes(ctx -> {
						noClip = BoolArgumentType.getBool(ctx, "enabled");
						return 1;
					})
				)
			)
		));
	}

	public static int getAlpha(MinecraftClient client, Entity entity) {
		double distance = 1D - (MathHelper.clamp(client.gameRenderer.getCamera().getPos().distanceTo(entity.getPos()), nearDistance, farDistance) - nearDistance) / (farDistance - nearDistance);
		return (int) MathHelper.clamp(distance * 255D, 1D, 255D);
	}

	@SuppressWarnings("resource")
	private void renderAfterSetup(WorldRenderContext context) {
		var shader = context.worldRenderer().entityOutlinePostProcessor;

		if (shader == null || shader.passes.isEmpty()) {
			return;
		}

		var program = shader.passes.get(0).getProgram();
		program.getUniformByNameOrDummy("NearSize").set(Math.max(nearSize, farSize));
		program.getUniformByNameOrDummy("FarSize").set(Math.min(nearSize, farSize));
		program.getUniformByNameOrDummy("NoClip").set(noClip ? 1F : 0F);
		program.getUniformByNameOrDummy("Visibility").set(visibility);
		program.getUniformByNameOrDummy("Quality").set(quality);
	}

	public static void copyOutlineDepth(MinecraftClient client) {
		var framebuffer = ((WorldRendererAccessor) client.worldRenderer).getEntityOutlinesFramebuffer();

		if (framebuffer != null) {
			framebuffer.copyDepthFrom(client.getFramebuffer());
			client.getFramebuffer().beginWrite(false);
		}
	}
}
