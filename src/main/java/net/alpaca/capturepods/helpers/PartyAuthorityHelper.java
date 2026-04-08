package net.alpaca.capturepods.helpers;

import net.minecraft.server.network.ServerPlayerEntity;
import xaero.pac.common.server.api.OpenPACServerAPI;
import xaero.pac.common.server.parties.party.api.IPartyManagerAPI;
import xaero.pac.common.server.parties.party.api.IServerPartyAPI;

import java.util.UUID;

public final class PartyAuthorityHelper {
    private PartyAuthorityHelper() {
        // This class should not be instantiated
    }

    public static boolean allowedToChangePartySettings(ServerPlayerEntity player) {
        OpenPACServerAPI api = OpenPACServerAPI.get(player.getServer());

        if (api != null) {
            IPartyManagerAPI partyManager = api.getPartyManager();
            IServerPartyAPI party = partyManager.getPartyByMember(player.getUuid());

            if (party == null) { //User is not in a party
                return false;
            }

            UUID playerUUID = player.getUuid();
            UUID ownerUUID = party.getOwner().getUUID();

            return (playerUUID.equals(ownerUUID));
        }
        return false;
    }

    public static IServerPartyAPI getActiveParty(ServerPlayerEntity player) {
        OpenPACServerAPI api = OpenPACServerAPI.get(player.getServer());
        IServerPartyAPI party;
        IPartyManagerAPI partyManager = api.getPartyManager();
        party = partyManager.getPartyByMember(player.getUuid());
        return party;
    }
}
