package net.alpaca.capturepods.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.shedaniel.autoconfig.AutoConfig;
import net.alpaca.capturepods.config.CapturePodsConfig;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

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
     * This command changes the flag for allowing captures on a party member's chunk
     *
     * @param context the context of the command
     * @return weather if the command ran successfully
     */
    private static int setAllowPartyCapture(CommandContext<ServerCommandSource> context) {
        boolean enabled = BoolArgumentType.getBool(context, "enabled");

        CapturePodsConfig config = AutoConfig.getConfigHolder(CapturePodsConfig.class).getConfig();
        config.allowPartyCapture = enabled;
        AutoConfig.getConfigHolder(CapturePodsConfig.class).save();

        context.getSource().sendFeedback(() -> Text.literal("Capture Pods: allowPartyCapture set to " + enabled), false);

        return Command.SINGLE_SUCCESS;
    }

    /**
     *
     * This command changes the flag for allowing releases on a party member's chunk
     *
     * @param context the context of the command
     * @return weather if the command ran successfully
     */
    private static int setAllowPartyRelease(CommandContext<ServerCommandSource> context) {
        boolean enabled = BoolArgumentType.getBool(context, "enabled");

        CapturePodsConfig config = AutoConfig.getConfigHolder(CapturePodsConfig.class).getConfig();
        config.allowPartyRelease = enabled;
        AutoConfig.getConfigHolder(CapturePodsConfig.class).save();

        context.getSource().sendFeedback(() -> Text.literal("Capture Pods: allowPartyRelease set to " + enabled), false);

        return Command.SINGLE_SUCCESS;
    }
}
