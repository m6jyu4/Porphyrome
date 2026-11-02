package com.m6jyu4.rome.item;

import com.m6jyu4.rome.block.ConstructableBlock;
import com.m6jyu4.rome.util.Localisation;
import net.minecraft.ChatFormatting;
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

    private static final String hammerCharging = "item.rome.hammer.charging";  // .withColor
    private static final String hammerBuild = "item.rome.hammer.build"; // .withColor

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
        int colour = percentage >= 100 ? ChatFormatting.GREEN.getColor() : ChatFormatting.GOLD.getColor();
        String iconKey = percentage >= 100 ? "item.rome.hammer.icon.ready" : "item.rome.hammer.icon.charging";
        player.displayClientMessage(
                Component.translatable(iconKey).withColor(colour)
                        .append(Component.literal(" "))
                        .append(Component.translatable(hammerCharging, percentage).withColor(colour)),
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
            displayBuildCount(livingEntity, player, buildCount);
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

    private void displayBuildCount(LivingEntity livingEntity, Player player, int buildCount){
        int colour = buildCount > 0 ? ChatFormatting.GREEN.getColor(): ChatFormatting.RED.getColor();
        Component message = Localisation.translatable(hammerBuild, buildCount, livingEntity).withColor(colour);
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
