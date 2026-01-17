package net.alpaca.capturepods;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.alpaca.capturepods.commands.CapturePodsCommands;
import net.alpaca.capturepods.config.CapturePodsConfig;
import net.alpaca.capturepods.item.ModItems;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CapturePods implements ModInitializer {
    public static final String MOD_ID = "capture-pods";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /**
     * This method runs on mod initialization
     */
    @Override
    public void onInitialize() {
        LOGGER.info("Capture Pods Mod Initialized!");

        AutoConfig.register(CapturePodsConfig.class, GsonConfigSerializer::new);

        ModItems.registerModItems();

        CapturePodsCommands.registerCommands();
    }
}