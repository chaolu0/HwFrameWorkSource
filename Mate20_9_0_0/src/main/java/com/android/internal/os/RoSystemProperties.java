package com.android.internal.os;

import android.os.SystemProperties;

public class RoSystemProperties {
    public static final boolean CONFIG_AVOID_GFX_ACCEL = SystemProperties.getBoolean("ro.config.avoid_gfx_accel", false);
    public static final boolean CONFIG_LOW_RAM = SystemProperties.getBoolean("ro.config.low_ram", false);
    public static final boolean CONFIG_SMALL_BATTERY = SystemProperties.getBoolean("ro.config.small_battery", false);
    public static final String CONTROL_PRIVAPP_PERMISSIONS = SystemProperties.get("ro.control_privapp_permissions");
    public static final boolean CONTROL_PRIVAPP_PERMISSIONS_DISABLE;
    public static final boolean CONTROL_PRIVAPP_PERMISSIONS_ENFORCE = "enforce".equalsIgnoreCase(CONTROL_PRIVAPP_PERMISSIONS);
    public static final boolean CONTROL_PRIVAPP_PERMISSIONS_LOG = "log".equalsIgnoreCase(CONTROL_PRIVAPP_PERMISSIONS);
    public static final boolean CRYPTO_BLOCK_ENCRYPTED = "block".equalsIgnoreCase(CRYPTO_TYPE);
    public static final boolean CRYPTO_ENCRYPTABLE;
    public static final boolean CRYPTO_ENCRYPTED = "encrypted".equalsIgnoreCase(CRYPTO_STATE);
    public static final boolean CRYPTO_FILE_ENCRYPTED = "file".equalsIgnoreCase(CRYPTO_TYPE);
    public static final String CRYPTO_STATE = SystemProperties.get("ro.crypto.state");
    public static final String CRYPTO_TYPE = SystemProperties.get("ro.crypto.type");
    public static final boolean DEBUGGABLE = (SystemProperties.getInt("ro.debuggable", 0) == 1);
    public static final int FACTORYTEST = SystemProperties.getInt("ro.factorytest", 0);
    public static final boolean FW_SYSTEM_USER_SPLIT = SystemProperties.getBoolean("ro.fw.system_user_split", false);

    static {
        boolean z = false;
        boolean z2 = (CRYPTO_STATE.isEmpty() || "unsupported".equals(CRYPTO_STATE)) ? false : true;
        CRYPTO_ENCRYPTABLE = z2;
        if (!(CONTROL_PRIVAPP_PERMISSIONS_LOG || CONTROL_PRIVAPP_PERMISSIONS_ENFORCE)) {
            z = true;
        }
        CONTROL_PRIVAPP_PERMISSIONS_DISABLE = z;
    }
}
