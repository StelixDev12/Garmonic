package dev.grit.item;

import dev.grit.registry.ModEffects;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Шина: перелом заживает за 20 секунд вместо нескольких минут и ослабевает до I уровня. */
public class SplintItem extends Item {
	public static final int SPLINTED_TICKS = 20 * 20;

	public SplintItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		StatusEffectInstance fracture = user.getStatusEffect(ModEffects.FRACTURE);
		if (fracture == null || fracture.getDuration() <= SPLINTED_TICKS && fracture.getAmplifier() == 0) {
			return TypedActionResult.fail(user.getStackInHand(hand));
		}
		return ItemUsage.consumeHeldItem(world, user, hand);
	}

	@Override
	public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
		if (!world.isClient && user.hasStatusEffect(ModEffects.FRACTURE)) {
			user.removeStatusEffect(ModEffects.FRACTURE);
			user.addStatusEffect(new StatusEffectInstance(ModEffects.FRACTURE, SPLINTED_TICKS, 0));
			world.playSound(null, user.getX(), user.getY(), user.getZ(),
					SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.PLAYERS, 1.0f, 0.8f);
		}
		if (!(user instanceof PlayerEntity player) || !player.getAbilities().creativeMode) {
			stack.decrement(1);
		}
		return stack;
	}

	@Override
	public int getMaxUseTime(ItemStack stack) {
		return 50;
	}

	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.BOW;
	}

	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		tooltip.add(Text.translatable("item.grit.splint.tooltip").formatted(Formatting.GRAY));
	}
}
