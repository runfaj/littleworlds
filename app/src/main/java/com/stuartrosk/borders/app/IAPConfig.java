package com.stuartrosk.borders.app;

import org.onepf.oms.OpenIabHelper;

import java.util.HashMap;
import java.util.Map;

public final class IAPConfig {
    //premium upgrade (non-consumable)
    public static final String SKU_PREMIUM = "upgrade";//"android.test.purchased";

    //Google Play
    public static final String GOOGLE_PLAY_KEY
            = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApZFARx3FvWcSmUXnU/a/fqL0Ld0BT+JVkYXrGu4WukDoCSb+iM9h1NHC9JIj85xQjEY7jYxjGrQO5hncxFgglC1Fv1GLNnCIsCoLFEap1ZzUUefV8szj73BCpksqz32cg3HJ8NQf2RY5gjlqXjfIMRGLzHiqrkEk9JqaxnUEvhqe0aQc2TmqZI5gtKVsXIT3QwsPA3N/AZfZrOWZpsFqh577Ll8OM2ReFYKnUQhghTQg+6xrVQ1D8PrRdWH7f09FAm7COV9bbX+uP2k9MKuC/+rEyj5s2EvnVbx5+7E4khnJ98QP3x6IVM/t/y2WyD33Fd5Rpg+e2rt+4DkH13HySQIDAQAB";
/*
    //Yandex.Store
    public static final String YANDEX_PUBLIC_KEY
            = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAs4SI/obW+q3dhsY3g5B6UggPcL5olWK8WY3tnTa2k3i2U40j" +
            "QuHRNNs8SqzdJeuoBLsKjaEsdTT0SJtEucOMZrprXMch97QtuLB4Mgu3Gs7USL6dM7NCUSoYrgOgw1Koi+ab+ZvFJkVMb9" +
            "a2EjYzR3aP0k4xjKyG2gW1rIEMMepxHm22VFjEg6YxBy+ecwRrjqDJOAPJyH6uSl8vUT8AKuG+hcCuYbNvlMdEZJo6MXJ9" +
            "vPNf/qPHwMy5G+faEprL6zR+HaPfxEqN/d8rbrW0qnr8LpXJ+nPB3/irBiMSZSqA222GC7m12sNNmNnNNlI397F3fRQSTz" +
            "VSRZt14YdPzwIDAQAB";
    //Appland
    public static final String APPLAND_PUBLIC_KEY =
            "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC5idC9c24V7a7qCJu7kdIyOZsk\n" +
                    "W0Rc7/q+K+ujEXsUaAdb5nwmlOJqpoJeCh5Fmq5A1NdF3BwkI8+GwTkH757NBZAS\n" +
                    "SdEuN0pLZmA6LopOiMIy0LoIWknM5eWMa3e41CxCEFoMv48gFIVxDNJ/KAQAX7+K\n" +
                    "ysYzIdlA3W3fBXXyGQIDAQAB";
    //SlideME
    public static final String SLIDEME_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwRiedByAoS2" +
            "DUJAkm6qqfhlhNnpNbONf8W4giGERMBAvRA7mRGKa7+vtgJiepvQ/CX0np5MBAMXcL9t9YFZ30lmp4COBdr5nilTyUdLWns" +
            "cnhYIxneJIG3rzkmnhXaDsiemOlrLC2PEJu6jcek8qurJmQ7gpP0va45MwiTHHto1lSjjvF8xYAZSrTlbIqLo1f98lxg9xs" +
            "zHI6sSXwDqDpJfS0JORtw3Rcc731QFR1rR2EOAEZo6Zdo0cD1uOQJgLkv8drU9BDMsR9ErBuGSbZQzn2FAc4Bkmq/gNGYd1" +
            "HmdFkofwVkqu/dTYWXOumKDIVqRsLQ213vuvC0lzcLaJxQIDAQAB";
            */

    public static Map<String, String> STORE_KEYS_MAP;

    public static void init() {
        STORE_KEYS_MAP = new HashMap<String, String>();
        STORE_KEYS_MAP.put(OpenIabHelper.NAME_GOOGLE, IAPConfig.GOOGLE_PLAY_KEY);
        /*STORE_KEYS_MAP.put(OpenIabHelper.NAME_YANDEX, IAPConfig.YANDEX_PUBLIC_KEY);
        STORE_KEYS_MAP.put(OpenIabHelper.NAME_APPLAND, IAPConfig.APPLAND_PUBLIC_KEY);
        STORE_KEYS_MAP.put(OpenIabHelper.NAME_SLIDEME, IAPConfig.SLIDEME_PUBLIC_KEY);*/
//        STORE_KEYS_MAP.put(OpenIabHelper.NAME_AMAZON,
//                "Unavailable. Amazon doesn't support RSA verification. So this mapping is not needed");
//        STORE_KEYS_MAP.put(OpenIabHelper.NAME_SAMSUNG,
//                "Unavailable. SamsungApps doesn't support RSA verification. So this mapping is not needed");

/*        SkuManager.getInstance()
                    //Yandex.Store
                .mapSku(SKU_PREMIUM, OpenIabHelper.NAME_YANDEX, "org.onepf.sample.trivialdrive.sku_premium")
                    //Nokia store
                .mapSku(SKU_PREMIUM, OpenIabHelper.NAME_NOKIA, "1290315")
                    //Amazon
                .mapSku(SKU_PREMIUM, OpenIabHelper.NAME_AMAZON, "org.onepf.sample.trivialdrive.sku_premium")
                    //Appland
                .mapSku(SKU_PREMIUM, OpenIabHelper.NAME_APPLAND, "appland.sku_premium")
                    //SlideME
                .mapSku(SKU_PREMIUM, OpenIabHelper.NAME_SLIDEME, "slideme.sku_premium")
                    //Samsung
                .mapSku(SKU_PREMIUM, OpenIabHelper.NAME_SAMSUNG, "000001003747/org.onepf.trivialdrivegame.sku_premium");*/
    }

    private IAPConfig() {
    }
}
