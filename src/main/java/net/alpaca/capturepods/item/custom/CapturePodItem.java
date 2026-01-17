package net.alpaca.capturepods.item.custom;

import me.shedaniel.autoconfig.AutoConfig;
import net.alpaca.capturepods.config.CapturePodsConfig;
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
import xaero.pac.common.server.parties.party.api.IPartyManagerAPI;

import java.util.Optional;
import java.util.UUID;


public class CapturePodItem extends Item {

    public CapturePodItem(Settings settings) {
        super(settings);
    }

    /**
     * Gets the claim data for the chunk a given entity is standing
     *
     * @param user      the player entity
     * @param entity    the entity to check
     * @param serverAPI the parties and claims API
     * @return the claim state of the chunk
     */
    private static @Nullable IPlayerChunkClaimAPI getChunkClaimData(PlayerEntity user, LivingEntity entity, OpenPACServerAPI serverAPI) {
        IServerClaimsManagerAPI claimsManager = serverAPI.getServerClaimsManager();
        ServerWorld world = (ServerWorld) user.getWorld();
        Identifier dimId = world.getRegistryKey().getValue();

        // Get chunk of the entity
        ChunkPos chunkPos = new ChunkPos(entity.getBlockPos());

        // Returns the claim state for given Chunk
        return claimsManager.get(dimId, chunkPos);
    }

    /**
     * Gets the claim data for a given chunk
     *
     * @param user      the player
     * @param chunkPos  the position of the chunk
     * @param serverAPI the parties and claims API
     * @return the claim state for given Chunk
     */
    private static @Nullable IPlayerChunkClaimAPI getChunkClaimData(PlayerEntity user, ChunkPos chunkPos, OpenPACServerAPI serverAPI) {
        IServerClaimsManagerAPI claimsManager = serverAPI.getServerClaimsManager();
        ServerWorld world = (ServerWorld) user.getWorld();
        Identifier dimId = world.getRegistryKey().getValue();

        // Returns the claim state for given Chunk
        return claimsManager.get(dimId, chunkPos);
    }

    /**
     *
     * Gets the claim data for a given ItemUsageContext
     *
     * @param user      the player
     * @param context   the context of the action
     * @param serverAPI the open claims and parties API
     * @return the claim state of the affected chunk
     */
    private IPlayerChunkClaimAPI getContextClaimData(PlayerEntity user, ItemUsageContext context, OpenPACServerAPI serverAPI) {
        IServerClaimsManagerAPI claimsManager = serverAPI.getServerClaimsManager();
        ServerWorld world = (ServerWorld) user.getWorld();
        Identifier dimId = world.getRegistryKey().getValue();

        // Returns the claim state for given block
        return claimsManager.get(dimId, context.getBlockPos());
    }

    /**
     * @param stack  the capture pod item
     * @param user   the user that initiates the action
     * @param entity the entity that the action points to
     * @param hand   the hand that uses the item
     * @return the result of the action
     */
    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (!(entity instanceof AnimalEntity || entity instanceof WaterCreatureEntity || entity instanceof MerchantEntity)) {
            return ActionResult.PASS;
        }

        if (user.getWorld().isClient) {
            return ActionResult.SUCCESS;
        }

        OpenPACServerAPI serverAPI = OpenPACServerAPI.get(user.getServer());
        IPlayerChunkClaimAPI claimState = getChunkClaimData(user, user.getChunkPos(), serverAPI);

        boolean allowParty = AutoConfig.getConfigHolder(CapturePodsConfig.class).getConfig().allowPartyCapture;

        boolean sameParty = false;
        if (!allowParty && claimState != null) {
            UUID ownerId = claimState.getPlayerId();
            UUID userId = user.getUuid();

            IPartyManagerAPI partyManager = serverAPI.getPartyManager();
            sameParty = Optional.ofNullable(partyManager.getPartyByMember(ownerId)).map(party -> party.getMemberInfoStream().anyMatch(member -> member.getUUID().equals(userId))).orElse(false);

            if (!ownerId.equals(userId) && sameParty) {
                //If party members aren't allowed, and users are of the same party
                user.sendMessage(Text.literal("Your party does not allow the use of capture pods on another member's claim."), false);
                return ActionResult.FAIL;
            }
        }

        ItemStack actualStack = user.getMainHandStack();

        if (!actualStack.hasNbt()) {
            createEmptyStackNbtData(actualStack);
        }

        if (!actualStack.getNbt().getBoolean("full")) {
            NbtCompound aux = new NbtCompound();

            if (entity.saveNbt(aux)) {
                actualStack.setNbt(aux.copy());
                actualStack.getNbt().putBoolean("full", true);
                actualStack.setCustomName(entity.getName());

                entity.remove(Entity.RemovalReason.DISCARDED);
                return ActionResult.CONSUME;
            }
        }

        return ActionResult.PASS;
    }

    /**
     * @param context the context of the action
     * @return the result of the action
     */
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity user = context.getPlayer();

        if (user.getWorld().isClient) {
            return ActionResult.SUCCESS;
        }

        if (user.isSneaking() && user.getMainHandStack().hasGlint()) {
            OpenPACServerAPI serverAPI = OpenPACServerAPI.get(user.getServer());

            IPlayerChunkClaimAPI claimState = getContextClaimData(user, context, serverAPI);
            if (claimState != null) { //Somebody OWS the claim
                UUID ownerId = claimState.getPlayerId();
                UUID userId = user.getUuid();

                // If not the owner
                if (!ownerId.equals(userId)) {
                    boolean allowParty = AutoConfig.getConfigHolder(CapturePodsConfig.class).getConfig().allowPartyRelease;

                    boolean sameParty = false;
                    if (!allowParty) {
                        IPartyManagerAPI partyManager = serverAPI.getPartyManager();
                        sameParty = Optional.ofNullable(partyManager.getPartyByMember(ownerId)).map(party -> party.getMemberInfoStream().anyMatch(member -> member.getUUID().equals(userId))).orElse(false);

                        if (sameParty) {
                            //If party members aren't allowed, and users are of the same party
                            user.sendMessage(Text.literal("Your party does not allow the release of capture pods on another member's claim."), false);
                            return ActionResult.FAIL;
                        }
                    }

                }
            }

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
        return ActionResult.SUCCESS;
    }

    /**
     * Empties NBT data for a given stack
     *
     * @param stack the item to modify
     */
    private void createEmptyStackNbtData(ItemStack stack) {
        stack.setNbt(new NbtCompound());
        assert stack.getNbt() != null;
        stack.getNbt().putBoolean("full", false);
    }

    /**
     * @param stack the item stack
     * @return if the items should have glint
     */
    @Override
    public boolean hasGlint(ItemStack stack) {
        return stack.hasNbt();
    }

}
