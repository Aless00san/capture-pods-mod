package net.alpaca.capturepods.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.shedaniel.autoconfig.AutoConfig;
import net.alpaca.capturepods.CapturePods;
import net.alpaca.capturepods.config.CapturePodsConfig;
import net.alpaca.capturepods.helpers.PartyAuthorityHelper;
import net.alpaca.capturepods.persistence.PartySettings;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import xaero.pac.common.server.parties.party.api.IServerPartyAPI;

public class CapturePodsCommands {

    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("capturepods").then(CommandManager.literal("allowPartyCapture").then(CommandManager.argument("enabled", BoolArgumentType.bool()).executes(context -> setAllowPartyCapture(context)))));
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("capturepods").then(CommandManager.literal("allowPartyRelease").then(CommandManager.argument("enabled", BoolArgumentType.bool()).executes(context -> setAllowPartyRelease(context)))));
        });
    }

    /**
     *
     * This command allows changing the flag that allows captures on a party member's chunk
     * It also checks that the user is the party owner, or it refuses the action
     *
     * @param context the context of the command
     * @return weather if the command ran successfully
     */
    private static int setAllowPartyCapture(CommandContext<ServerCommandSource> context) {
        if (CapturePods.partySettings == null) {
            return 0;
        }

        boolean enabled = BoolArgumentType.getBool(context, "enabled");

        if (!PartyAuthorityHelper.allowedToChangePartySettings(context.getSource().getPlayer())) {
            context.getSource().sendError(Text.literal("Only the party owner can change this setting"));
            return 0;
        }

        IServerPartyAPI party = PartyAuthorityHelper.getActiveParty(context.getSource().getPlayer());
        CapturePods.partySettings.setCaptureAllowed(party.getId(), enabled);

        context.getSource().sendFeedback(() ->
                Text.literal("Capture Pods: allowPartyCapture set to " + enabled), false);

        return Command.SINGLE_SUCCESS;
    }

    /**
     *
     * This command changes the flag for allowing releases on a party member's chunk
     * It also checks that the user is the party owner, or it refuses the action
     *
     * @param context the context of the command
     * @return weather if the command ran successfully
     */
    private static int setAllowPartyRelease(CommandContext<ServerCommandSource> context) {
        boolean enabled = BoolArgumentType.getBool(context, "enabled");

        if (CapturePods.partySettings == null) {
            return 0;
        }

        if (!PartyAuthorityHelper.allowedToChangePartySettings(context.getSource().getPlayer())) { //If user is not allowed to change perms
            context.getSource().sendError(Text.literal("Only the party owner can change this setting"));
            return 0;
        }

        CapturePodsConfig config = AutoConfig.getConfigHolder(CapturePodsConfig.class).getConfig();
        config.allowPartyRelease = enabled;
        AutoConfig.getConfigHolder(CapturePodsConfig.class).save();

        context.getSource().sendFeedback(() -> Text.literal("Capture Pods: allowPartyRelease set to " + enabled), false);

        IServerPartyAPI party = PartyAuthorityHelper.getActiveParty(context.getSource().getPlayer());
        CapturePods.partySettings.setReleaseAllowed(party.getId(), enabled);

        return Command.SINGLE_SUCCESS;
    }
}
