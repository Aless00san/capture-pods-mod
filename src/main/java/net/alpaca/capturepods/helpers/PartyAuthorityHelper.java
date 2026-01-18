package net.alpaca.capturepods.helpers;

import org.jetbrains.annotations.NonNls;

import blue.endless.jankson.annotation.NonnullByDefault;
import net.minecraft.server.network.ServerPlayerEntity;
import xaero.pac.common.server.api.OpenPACServerAPI;
import xaero.pac.common.server.parties.party.IServerParty;
import xaero.pac.common.server.parties.party.api.IPartyManagerAPI;
import xaero.pac.common.server.parties.party.api.IServerPartyAPI;
import xaero.pac.common.server.player.permission.api.IPlayerPermissionSystemAPI;

public final class PartyAuthorityHelper {
    private PartyAuthorityHelper() {
        // This class should not be instantiated
    }

    public static boolean allowedToChangePartySettings(ServerPlayerEntity player) {
        // TOOD: Implement party authority system, check if the player is the owner of
        // the party
        OpenPACServerAPI api = OpenPACServerAPI.get(player.getServer());

        if (api != null) {
            IPartyManagerAPI partyManager = api.getPartyManager();
            IServerPartyAPI party = partyManager.getPartyByMember(player.getUuid());

            if (party == null) {
                return false;
            }
        }
        return false;
    }
}
