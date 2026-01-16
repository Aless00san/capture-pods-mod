package net.alpaca.capturepods.item.custom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import org.jetbrains.annotations.Nullable;
import xaero.pac.common.claims.player.api.IPlayerChunkClaimAPI;
import xaero.pac.common.server.api.OpenPACServerAPI;
import xaero.pac.common.server.claims.api.IServerClaimsManagerAPI;


public class CapturePodItem extends Item {

    public CapturePodItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        ItemStack actualStack = user.getMainHandStack();
        if (entity instanceof AnimalEntity || entity instanceof WaterCreatureEntity || entity instanceof MerchantEntity) {
            if (!user.getWorld().isClient) {
                OpenPACServerAPI serverAPI = OpenPACServerAPI.get(user.getServer());
                IPlayerChunkClaimAPI claimState = getChunkClaimAPI(user, entity, serverAPI);

                if (claimState == null) {
                    user.sendMessage(Text.literal("Chunk is not claimed"));
                }
            }

            if (!actualStack.hasNbt()) {
                createEmptyStackNbtData(user.getMainHandStack());
            }

            assert actualStack.getNbt() != null;
            if (!actualStack.getNbt().getBoolean("full")) {
                NbtCompound aux = new NbtCompound();

                if (entity.saveNbt(aux)) {
                    actualStack.setNbt(aux.copy()); //Save nbt_data from mob to item
                    actualStack.getNbt().putBoolean("full", true);
                    actualStack.setCustomName(entity.getName());
                }
                entity.remove(Entity.RemovalReason.DISCARDED);
            }
        }

        return super.useOnEntity(stack, user, entity, hand);
    }

    private static @Nullable IPlayerChunkClaimAPI getChunkClaimAPI(PlayerEntity user, LivingEntity entity, OpenPACServerAPI serverAPI) {
        IServerClaimsManagerAPI claimsManager = serverAPI.getServerClaimsManager();
        ServerWorld world = (ServerWorld) user.getWorld(); // cast if needed
        Identifier dimId = world.getRegistryKey().getValue(); // this is the correct ResourceLocation\

        // Get chunk of the entity
        ChunkPos chunkPos = new ChunkPos(entity.getBlockPos());

        // Get the claim state for given Chunk
        return claimsManager.get(dimId, chunkPos);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getPlayer().isInSneakingPose() && context.getPlayer().getMainHandStack().hasGlint()) {
            NbtCompound mobData = context.getPlayer().getMainHandStack().getNbt();
            if (mobData != null && mobData.getBoolean("full")) {
                EntityType<?> type = EntityType.get(mobData.getString("id")).orElse(null);
                if (type != null) {
                    Entity mob = type.create(context.getWorld());
                    if (mob instanceof LivingEntity) {
                        mob.readNbt(mobData);
                        mob.refreshPositionAndAngles(context.getBlockPos().getX(), context.getBlockPos().getY() + 1, context.getBlockPos().getZ(), 0, 0);
                        context.getWorld().spawnEntity(mob);
                        context.getPlayer().getMainHandStack().setNbt(new NbtCompound());
                    }
                }
            }

            context.getPlayer().getMainHandStack().setNbt(new NbtCompound());
        }
        return super.useOnBlock(context);
    }

    private void createEmptyStackNbtData(ItemStack stack) {
        stack.setNbt(new NbtCompound());
        assert stack.getNbt() != null;
        stack.getNbt().putBoolean("full", false);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return stack.hasNbt();
    }

}
