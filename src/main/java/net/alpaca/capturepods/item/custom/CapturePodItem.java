package net.alpaca.capturepods.item.custom;

import net.alpaca.capturepods.CapturePods;
import net.alpaca.capturepods.helpers.ChunkHelper;
import net.alpaca.capturepods.helpers.PartyAuthorityHelper;
import net.alpaca.capturepods.persistence.PartySettings;
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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import xaero.pac.common.claims.player.api.IPlayerChunkClaimAPI;
import xaero.pac.common.server.api.OpenPACServerAPI;
import xaero.pac.common.server.parties.party.api.IPartyManagerAPI;
import xaero.pac.common.server.parties.party.api.IServerPartyAPI;

import java.util.Optional;
import java.util.UUID;

/**
 * The capture pod item class
 * Handles the interactions with the capture pod item
 */
public class CapturePodItem extends Item {

    public CapturePodItem(Settings settings) {
        super(settings);
    }

    /**
     *
     * Handles the interaction with the capture pod item on entities
     *
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

        if (CapturePods.partySettings == null) {
            return ActionResult.PASS; //PartySettings still starting
        }

        PartySettings partySettings = CapturePods.partySettings; //Get the reference after we make sure is running on the server

        OpenPACServerAPI serverAPI = OpenPACServerAPI.get(user.getServer());
        IPlayerChunkClaimAPI claimState = ChunkHelper.getChunkClaimData(user, user.getChunkPos(), serverAPI);

        //boolean allowParty = AutoConfig.getConfigHolder(CapturePodsConfig.class).getConfig().allowPartyCapture;

        IServerPartyAPI activeParty = PartyAuthorityHelper.getActiveParty((ServerPlayerEntity) user);
        boolean allowParty = activeParty != null && partySettings.isCaptureAllowed(activeParty.getId());

        boolean sameParty = false;
        if (!allowParty && claimState != null) {
            UUID ownerId = claimState.getPlayerId();
            UUID userId = user.getUuid();

            IPartyManagerAPI partyManager = serverAPI.getPartyManager();
            sameParty = Optional.ofNullable(partyManager.getPartyByMember(ownerId)).map(party -> party.getMemberInfoStream().anyMatch(member -> member.getUUID().equals(userId))).orElse(false);

            if (!ownerId.equals(userId) && sameParty) {
                // If party members aren't allowed, and users are of the same party
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
     *
     * Handles the interaction with the capture pod item on blocks
     *
     * @param context the context of the action
     * @return the result of the action
     */
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity user = context.getPlayer();

        if (user.getWorld().isClient) {
            return ActionResult.SUCCESS;
        }

        if (CapturePods.partySettings == null) {
            return ActionResult.PASS; //PartySettings still starting
        }

        PartySettings partySettings = CapturePods.partySettings; //Get the reference after we make sure is running on the server

        if (user.isSneaking() && user.getMainHandStack().hasGlint()) {
            OpenPACServerAPI serverAPI = OpenPACServerAPI.get(user.getServer());

            IPlayerChunkClaimAPI claimState = ChunkHelper.getContextClaimData(user, context, serverAPI);
            if (claimState != null) { // Somebody OWS the claim
                UUID ownerId = claimState.getPlayerId();
                UUID userId = user.getUuid();

                // If not the owner
                if (!ownerId.equals(userId)) {
                    //boolean allowParty = AutoConfig.getConfigHolder(CapturePodsConfig.class).getConfig().allowPartyRelease;

                    IServerPartyAPI activeParty = PartyAuthorityHelper.getActiveParty((ServerPlayerEntity) user);
                    boolean allowParty = activeParty != null && partySettings.isReleaseAllowed(activeParty.getId());

                    boolean sameParty = false;
                    if (!allowParty) {
                        IPartyManagerAPI partyManager = serverAPI.getPartyManager();
                        sameParty = Optional.ofNullable(partyManager.getPartyByMember(ownerId)).map(party -> party.getMemberInfoStream().anyMatch(member -> member.getUUID().equals(userId))).orElse(false);

                        if (sameParty) {
                            // If party members aren't allowed, and users are of the same party
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
