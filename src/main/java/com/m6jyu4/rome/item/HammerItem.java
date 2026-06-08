package com.m6jyu4.rome.item;

import com.m6jyu4.rome.block.ConstructableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class HammerItem extends Item {
    private static final int COOLDOWN_TICKS = 20;
    private static final int CHARGE_DURATION_TICKS = 20;

    public HammerItem(Properties properties) { super(properties); }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player)) return;

        if (player.level().isClientSide) {
            int percentage = calculatePercentage(livingEntity, stack, remainingUseDuration);
            displayHammerPercentage(player, percentage);
        }
    }

    private int calculatePercentage(LivingEntity livingEntity, ItemStack stack, int remainingUseDuration){
        int ticksUsed  = this.getUseDuration(stack, livingEntity) - remainingUseDuration;
        return Math.min(100, (int) ((ticksUsed / (float) CHARGE_DURATION_TICKS) * 100));
    }

    private void displayHammerPercentage(Player player, int percentage){
        int color = percentage >= 100 ? 0x00FF00 : 0xFFAA00;
        String iconKey = percentage >= 100 ? "item.rome.hammer.icon.ready" : "item.rome.hammer.icon.charging";
        player.displayClientMessage(
                Component.translatable(iconKey).withColor(color)
                        .append(Component.literal(" "))
                        .append(Component.translatable("item.rome.hammer.charging", percentage).withColor(color)),
                true
        );
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (!(livingEntity instanceof Player player))    return;

        int ticksUsed = this.getUseDuration(stack, livingEntity) - timeCharged;

        if (ticksUsed > CHARGE_DURATION_TICKS) {
            int buildCount = constructBuilding(serverLevel, player);
            displayBuildCount(player, buildCount);
            playHammerParticle(serverLevel, player, buildCount);
            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        }
    }

    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    private int constructBuilding(ServerLevel level, Player player){
        BlockPos playerPos = player.blockPosition();
        int radius = 1;
        int buildCount = 0;

        for (BlockPos pos : BlockPos.betweenClosed(
                playerPos.offset(-radius, -radius, -radius),
                playerPos.offset(radius, radius, radius)
        )) {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof ConstructableBlock constructableBlock) {
                if (constructableBlock.onHammerSwing(level, pos, state)) buildCount++;
            }
        }
        return buildCount;
    }

    private void displayBuildCount(Player player, int buildCount){
        Component message;
        if (buildCount >= 2) message = Component.translatable("item.rome.hammer.yes_build_plural", buildCount).withColor(0x55FF55);
        else if (buildCount == 1) message = Component.translatable("item.rome.hammer.yes_build_single", buildCount).withColor(0x55FF55);
        else message = Component.translatable("item.rome.hammer.no_build").withColor(0xFF5555);

        player.displayClientMessage(message, true);
    }

    private void playHammerParticle(ServerLevel serverLevel, Player player, int buildCount){
        serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK,
                player.getX(), player.getY() + 1, player.getZ(),
                1, 0, 0, 0, 0);
        if (buildCount >= 1) {
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.VILLAGER_WORK_FLETCHER, SoundSource.BLOCKS,
                    0.3F, 1.0F);
        } else {
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS,
                    0.15F, 1.0F);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);

        if (player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(stack);

        player.startUsingItem(usedHand);
        return InteractionResultHolder.consume(stack);
    }
}
