package net.alpaca.capturepods.helpers;

import net.minecraft.server.network.ServerPlayerEntity;
import xaero.pac.common.server.api.OpenPACServerAPI;
import xaero.pac.common.server.parties.party.api.IPartyManagerAPI;
import xaero.pac.common.server.parties.party.api.IServerPartyAPI;

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
