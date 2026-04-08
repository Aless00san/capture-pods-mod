package net.alpaca.capturepods.helpers;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import org.jetbrains.annotations.Nullable;
import xaero.pac.common.claims.player.api.IPlayerChunkClaimAPI;
import xaero.pac.common.server.api.OpenPACServerAPI;
import xaero.pac.common.server.claims.api.IServerClaimsManagerAPI;

public final class ChunkHelper {

    private ChunkHelper() {
        // This class should not be instantiated
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
    public static IPlayerChunkClaimAPI getContextClaimData(PlayerEntity user, ItemUsageContext context,
            OpenPACServerAPI serverAPI) {
        IServerClaimsManagerAPI claimsManager = serverAPI.getServerClaimsManager();
        ServerWorld world = (ServerWorld) user.getWorld();
        Identifier dimId = world.getRegistryKey().getValue();

        // Returns the claim state for given block
        return claimsManager.get(dimId, context.getBlockPos());
    }

    /**
     * Gets the claim data for a given chunk
     *
     * @param user      the player
     * @param chunkPos  the position of the chunk
     * @param serverAPI the parties and claims API
     * @return the claim state for given Chunk
     */
    public static @Nullable IPlayerChunkClaimAPI getChunkClaimData(PlayerEntity user, ChunkPos chunkPos,
                                                                    OpenPACServerAPI serverAPI) {
        IServerClaimsManagerAPI claimsManager = serverAPI.getServerClaimsManager();
        ServerWorld world = (ServerWorld) user.getWorld();
        Identifier dimId = world.getRegistryKey().getValue();

        // Returns the claim state for given Chunk
        return claimsManager.get(dimId, chunkPos);
    }

}
