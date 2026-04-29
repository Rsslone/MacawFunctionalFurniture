package com.tommytek.mcwfuncfurn;

import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.Arrays;
import java.util.List;

// Discovered via META-INF/services/zone.rong.mixinbooter.ILateMixinLoader
public class MixinLoader implements ILateMixinLoader {

    @Override
    public List<String> getMixinConfigs() {
        return Arrays.asList("mixins.mcwfuncfurn.json", "mixins.mcwfuncfurn.client.json");
    }
}
