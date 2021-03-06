package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.os.Handler;
import com.android.internal.telephony.CommandsInterface;

public class HwFullNetworkDefaultStateFactory {
    private static HwFullNetworkDefaultStateBase mDefaultStateBaseInstance;
    private static final Object mLock = new Object();

    public static HwFullNetworkDefaultStateBase getHwFullNetworkDefaultState(Context c, CommandsInterface[] ci, Handler h) {
        HwFullNetworkDefaultStateBase hwFullNetworkDefaultStateBase;
        synchronized (mLock) {
            if (mDefaultStateBaseInstance == null) {
                if (HwFullNetworkConfig.isHisiPlatform()) {
                    if (HwFullNetworkConfig.IS_FAST_SWITCH_SIMSLOT) {
                        mDefaultStateBaseInstance = new HwFullNetworkDefaultStateHisi2_0(c, ci, h);
                    } else if (HwFullNetworkConfig.IS_FULL_NETWORK_SUPPORTED_IN_HISI) {
                        mDefaultStateBaseInstance = new HwFullNetworkDefaultStateHisi1_0(c, ci, h);
                    }
                } else if (HwFullNetworkConfig.IS_QCRIL_CROSS_MAPPING || HwFullNetworkConfig.IS_QCOM_DUAL_LTE_STACK) {
                    mDefaultStateBaseInstance = new HwFullNetworkDefaultStateQcom2_0(c, ci, h);
                }
            }
            hwFullNetworkDefaultStateBase = mDefaultStateBaseInstance;
        }
        return hwFullNetworkDefaultStateBase;
    }
}
