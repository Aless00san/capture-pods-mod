package net.alpaca.capturepods.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import me.shedaniel.autoconfig.AutoConfig;

public class ModMenuIntegraton implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
       return parent -> AutoConfig.getConfigScreen(CapturePodsConfig.class, parent).get();
    }
}
