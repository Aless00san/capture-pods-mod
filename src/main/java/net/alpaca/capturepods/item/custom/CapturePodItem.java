package net.alpaca.capturepods.item.custom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;


public class CapturePodItem extends Item {
    public CapturePodItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        ItemStack actualStack = user.getMainHandStack();
        if (entity instanceof AnimalEntity || entity instanceof WaterCreatureEntity || entity instanceof MerchantEntity) {
            if ( !actualStack.hasNbt() ) {
                createEmptyStackNbtData(user.getMainHandStack());
            }

            if (!actualStack.getNbt().getBoolean("full")) {
                NbtCompound aux = new NbtCompound();

                    if (entity.saveNbt(aux)) {
                        actualStack.setNbt(aux.copy()); //Save nbt_data from mob to item
                        actualStack.getNbt().putBoolean("full",true);
                    }
                    entity.remove(Entity.RemovalReason.DISCARDED);
            }
        }
            return super.useOnEntity(stack, user, entity, hand);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getPlayer().isInSneakingPose() && context.getPlayer().getMainHandStack().hasGlint())
            context.getPlayer().getMainHandStack().setNbt(new NbtCompound());
        return super.useOnBlock(context);
    }

    private void createEmptyStackNbtData(ItemStack stack) {
        stack.setNbt(new NbtCompound());
        stack.getNbt().putBoolean("full",false);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return stack.hasNbt();
    }

}
