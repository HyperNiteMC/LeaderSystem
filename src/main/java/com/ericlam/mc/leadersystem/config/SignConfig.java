package com.ericlam.mc.leadersystem.config;

import com.hypernite.mc.hnmc.core.config.yaml.Configuration;
import com.hypernite.mc.hnmc.core.config.yaml.Resource;

import java.util.Map;

@Resource(locate = "signs.yml")
public class SignConfig extends Configuration {
    public Map<String, SignData> signs;

    public static class SignData {
        public String item;
        public int rank;
        public String world;
        public SignVector signLocation;
        public SignVector headLocation;
    }
}
