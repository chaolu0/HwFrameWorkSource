package com.android.server.security.trustcircle.auth;

import android.content.Context;
import android.os.RemoteException;
import com.android.server.security.trustcircle.auth.IOTController.KaInfoRequest;
import com.android.server.security.trustcircle.auth.IOTController.KaInfoResponse;
import com.android.server.security.trustcircle.task.HwSecurityTaskBase;
import com.android.server.security.trustcircle.task.HwSecurityTaskBase.RetCallback;
import com.android.server.security.trustcircle.utils.AuthUtils;
import com.android.server.security.trustcircle.utils.LogHelper;
import com.android.server.security.trustcircle.utils.Status.TCIS_Result;
import huawei.android.security.IKaCallback;

public class KaRequestTask extends HwSecurityTaskBase {
    private static final String TAG = "KAuthTask";
    private Context mContext;
    private IKaCallback mKaCallback;
    private KaInfoRequest mKaInfo;

    public KaRequestTask(HwSecurityTaskBase parent, RetCallback callback, Context context, KaInfoRequest kaInfo, IKaCallback kaCallback) {
        super(parent, callback);
        this.mContext = context;
        this.mKaInfo = kaInfo;
        this.mKaCallback = kaCallback;
    }

    public int doAction() {
        try {
            KaInfoResponse retKaInfo = AuthUtils.processKaAuth(this.mContext, this.mKaInfo);
            if (retKaInfo.result == TCIS_Result.SUCCESS.value()) {
                this.mKaCallback.onKaResult(this.mKaInfo.authId, retKaInfo.result, retKaInfo.iv, retKaInfo.payload);
            } else {
                this.mKaCallback.onKaError(this.mKaInfo.authId, retKaInfo.result);
            }
            return 0;
        } catch (RemoteException e) {
            LogHelper.e(TAG, "ka task do action remote exception " + e.getClass().getName());
            return 3;
        } catch (Exception e2) {
            LogHelper.e(TAG, "ka task do action exception " + e2.getClass().getName());
            return 3;
        }
    }
}
