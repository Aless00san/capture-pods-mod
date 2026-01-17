package net.alpaca.capturepods.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

/**
 *  This class MAPs the JSON config file
 */
@Config(name = "capturepods")
public class CapturePodsConfig implements ConfigData {

    public boolean allowPartyCapture = true;
    public boolean allowPartyRelease = true;

}

