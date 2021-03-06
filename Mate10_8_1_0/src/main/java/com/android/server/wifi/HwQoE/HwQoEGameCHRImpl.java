package com.android.server.wifi.HwQoE;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.android.server.wifi.wifipro.WifiProStatisticsManager;

public class HwQoEGameCHRImpl {
    public static final int AP_TYPE_24G_BT_COEXIST = 2;
    public static final int AP_TYPE_24G_ONLY = 1;
    public static final int AP_TYPE_5G_ONLY = 3;
    private static final String CHR_BROADCAST_PERMISSION = "com.huawei.android.permission.GET_CHR_DATA";
    private static final int DS_KOG_LATENCY_100 = 100;
    private static final int DS_KOG_LATENCY_200 = 200;
    private static final int DS_KOG_LATENCY_300 = 300;
    private static final int DS_KOG_LATENCY_400 = 400;
    private static final int DS_KOG_RTT_STATIC_REPORT = 2;
    private static final int GAME_BAD_MAX_TCP_RTT = 460;
    private static final int GAME_GENERAL_MAX_ARP_RTT = 100;
    private static final int GAME_GENERAL_MAX_RTT = 200;
    private static final int GAME_GENERAL_MAX_TCP_RTT = 100;
    private static final int GAME_POOR_MAX_ARP_RTT = 200;
    private static final int GAME_POOR_MAX_RTT = 460;
    private static final int GAME_POOR_MAX_TCP_RTT = 200;
    private static final int GAME_SMOOTH_MAX_ARP_RTT = 30;
    private static final int GAME_SMOOTH_MAX_RTT = 100;
    private static final int GAME_SMOOTH_MAX_TCP_RTT = 50;
    private static final int GAME_UNKOWN_RTT = 0;
    private static final String INTENT_DS_KOG_RTT_REPORT = "com.android.intent.action.kog_rtt_report";
    private static final String TAG = "HiDATA_GameCHRImpl";
    private boolean mAllowStatistics;
    private int mApType;
    private long mArpRttGeneralDuration;
    private long mArpRttGeneralStartTime;
    private long mArpRttPoorDuration;
    private long mArpRttPoorStartTime;
    private long mArpRttSmoothDuration;
    private long mArpRttSmoothStartTime;
    private short mBTScan24GCounter;
    private Context mContext;
    private int mDsKOGRtt100Count = 0;
    private int mDsKOGRtt200Count = 0;
    private int mDsKOGRtt300Count = 0;
    private int mDsKOGRtt400Count = 0;
    private int mDsKOGRttOver400Count = 0;
    private int mDsKOGTotalRttCount = 0;
    private int mDsKOGTotalRttSum = 0;
    private long mGameRTTGeneralStartTime;
    private long mGameRTTPoorStartTime;
    private long mGameRTTSmoothStartTime;
    private HwWifiGameNetChrInfo mHwWifiGameNetChrInfo = new HwWifiGameNetChrInfo();
    private int mLastArpRttLevel;
    private int mLastGameNetworkLevel;
    private int mLastTcpRttLevel;
    private BluetoothAdapter mLocalBluetoothAdapter;
    private long mNetworkGeneralDuration;
    private long mNetworkPoorDuration;
    private long mNetworkSmoothDuration;
    private long mTcpRttBadDuration;
    private long mTcpRttBadStartTime;
    private long mTcpRttGeneralDuration;
    private long mTcpRttGeneralStartTime;
    private long mTcpRttPoorDuration;
    private long mTcpRttPoorStartTime;
    private long mTcpRttSmoothDuration;
    private long mTcpRttSmoothStartTime;
    private int mTencentTmgpGameType;
    private short mWifiArpFailCounter;
    private short mWifiDisCounter;
    private WifiProStatisticsManager mWifiProStatisticsManager;
    private short mWifiRoamingCounter;
    private short mWifiScanCounter;
    private long mlastGameWarStartTime;

    public HwQoEGameCHRImpl(Context context) {
        this.mContext = context;
    }

    private void initialGameCHR() {
        this.mGameRTTSmoothStartTime = 0;
        this.mGameRTTGeneralStartTime = 0;
        this.mGameRTTPoorStartTime = 0;
        this.mNetworkSmoothDuration = 0;
        this.mNetworkGeneralDuration = 0;
        this.mNetworkPoorDuration = 0;
        this.mArpRttSmoothStartTime = 0;
        this.mArpRttGeneralStartTime = 0;
        this.mArpRttPoorStartTime = 0;
        this.mArpRttSmoothDuration = 0;
        this.mArpRttGeneralDuration = 0;
        this.mArpRttPoorDuration = 0;
        this.mTcpRttSmoothStartTime = 0;
        this.mTcpRttGeneralStartTime = 0;
        this.mTcpRttPoorStartTime = 0;
        this.mTcpRttSmoothDuration = 0;
        this.mTcpRttGeneralDuration = 0;
        this.mTcpRttPoorDuration = 0;
        this.mDsKOGRtt100Count = 0;
        this.mDsKOGRtt200Count = 0;
        this.mDsKOGRtt300Count = 0;
        this.mDsKOGRtt400Count = 0;
        this.mDsKOGRttOver400Count = 0;
        this.mDsKOGTotalRttSum = 0;
        this.mDsKOGTotalRttCount = 0;
        this.mLastGameNetworkLevel = 0;
        this.mLastArpRttLevel = 0;
        this.mWifiRoamingCounter = (short) 0;
        this.mWifiArpFailCounter = (short) 0;
        this.mApType = 1;
        this.mWifiDisCounter = (short) 0;
        this.mAllowStatistics = false;
        this.mWifiScanCounter = (short) 0;
        this.mBTScan24GCounter = (short) 0;
        this.mHwWifiGameNetChrInfo.clean();
        if (this.mWifiProStatisticsManager == null) {
            this.mWifiProStatisticsManager = WifiProStatisticsManager.getInstance();
        }
    }

    public void updateTencentTmgpGameRttChanged(long rtt) {
        if (this.mTencentTmgpGameType != 1) {
            logD("Game-rtt, TencentTmgpGameType is not KOG_INWAR");
            return;
        }
        logD("Game-rtt:" + rtt + " ms, mLastGameNetworkLevel:" + this.mLastGameNetworkLevel);
        if (rtt < 100) {
            if (this.mLastGameNetworkLevel != 100) {
                this.mGameRTTSmoothStartTime = System.currentTimeMillis();
            }
            if (this.mLastGameNetworkLevel == 200 && this.mGameRTTGeneralStartTime != 0) {
                this.mNetworkGeneralDuration += System.currentTimeMillis() - this.mGameRTTGeneralStartTime;
                this.mGameRTTGeneralStartTime = 0;
            } else if (this.mLastGameNetworkLevel == 460 && this.mGameRTTPoorStartTime != 0) {
                this.mNetworkPoorDuration += System.currentTimeMillis() - this.mGameRTTPoorStartTime;
                this.mGameRTTPoorStartTime = 0;
            } else if (this.mLastGameNetworkLevel == 0) {
                this.mNetworkSmoothDuration = this.mGameRTTSmoothStartTime - this.mlastGameWarStartTime;
            }
            this.mLastGameNetworkLevel = 100;
        } else if (rtt < 200) {
            if (this.mLastGameNetworkLevel != 200) {
                this.mGameRTTGeneralStartTime = System.currentTimeMillis();
            }
            if (this.mLastGameNetworkLevel == 100 && this.mGameRTTSmoothStartTime != 0) {
                this.mNetworkSmoothDuration += System.currentTimeMillis() - this.mGameRTTSmoothStartTime;
                this.mGameRTTSmoothStartTime = 0;
            } else if (this.mLastGameNetworkLevel == 460 && this.mGameRTTPoorStartTime != 0) {
                this.mNetworkPoorDuration += System.currentTimeMillis() - this.mGameRTTPoorStartTime;
                this.mGameRTTPoorStartTime = 0;
            } else if (this.mLastGameNetworkLevel == 0) {
                this.mNetworkGeneralDuration = this.mGameRTTGeneralStartTime - this.mlastGameWarStartTime;
            }
            this.mLastGameNetworkLevel = 200;
        } else {
            if (this.mLastGameNetworkLevel != 460) {
                this.mGameRTTPoorStartTime = System.currentTimeMillis();
            }
            if (this.mLastGameNetworkLevel == 100 && this.mGameRTTSmoothStartTime != 0) {
                this.mNetworkSmoothDuration += System.currentTimeMillis() - this.mGameRTTSmoothStartTime;
                this.mGameRTTSmoothStartTime = 0;
            } else if (this.mLastGameNetworkLevel == 200 && this.mGameRTTGeneralStartTime != 0) {
                this.mNetworkGeneralDuration += System.currentTimeMillis() - this.mGameRTTGeneralStartTime;
                this.mGameRTTGeneralStartTime = 0;
            } else if (this.mLastGameNetworkLevel == 0) {
                this.mNetworkPoorDuration = this.mGameRTTPoorStartTime - this.mlastGameWarStartTime;
            }
            this.mLastGameNetworkLevel = 460;
        }
    }

    public void updateArpRttChanged(int rtt) {
        logD("Arp-rtt:" + rtt + " ms, mLastArpRttLevel:" + this.mLastArpRttLevel);
        if (this.mTencentTmgpGameType != 1) {
            logD("Arp-rtt:, TencentTmgpGameType is not KOG_INWAR");
            return;
        }
        if (rtt < 30) {
            if (this.mLastArpRttLevel != 30) {
                this.mArpRttSmoothStartTime = System.currentTimeMillis();
            }
            if (this.mLastArpRttLevel == 100 && this.mArpRttGeneralStartTime != 0) {
                this.mArpRttGeneralDuration += System.currentTimeMillis() - this.mArpRttGeneralStartTime;
                this.mArpRttGeneralStartTime = 0;
            } else if (this.mLastArpRttLevel == 200 && this.mArpRttPoorStartTime != 0) {
                this.mArpRttPoorDuration += System.currentTimeMillis() - this.mArpRttPoorStartTime;
                this.mArpRttPoorStartTime = 0;
            } else if (this.mLastArpRttLevel == 0) {
                this.mArpRttSmoothDuration = this.mArpRttSmoothStartTime - this.mlastGameWarStartTime;
            }
            this.mLastArpRttLevel = 30;
        } else if (rtt < 100) {
            if (this.mLastArpRttLevel != 100) {
                this.mArpRttGeneralStartTime = System.currentTimeMillis();
            }
            if (this.mLastArpRttLevel == 30 && this.mArpRttSmoothStartTime != 0) {
                this.mArpRttSmoothDuration += System.currentTimeMillis() - this.mArpRttSmoothStartTime;
                this.mArpRttSmoothStartTime = 0;
            } else if (this.mLastArpRttLevel == 200 && this.mArpRttPoorStartTime != 0) {
                this.mArpRttPoorDuration += System.currentTimeMillis() - this.mArpRttPoorStartTime;
                this.mArpRttPoorStartTime = 0;
            } else if (this.mLastArpRttLevel == 0) {
                this.mArpRttGeneralDuration = this.mArpRttGeneralStartTime - this.mlastGameWarStartTime;
            }
            this.mLastArpRttLevel = 100;
        } else {
            if (this.mLastArpRttLevel != 200) {
                this.mArpRttPoorStartTime = System.currentTimeMillis();
            }
            if (this.mLastArpRttLevel == 30 && this.mArpRttSmoothStartTime != 0) {
                this.mArpRttSmoothDuration += System.currentTimeMillis() - this.mArpRttSmoothStartTime;
                this.mArpRttSmoothStartTime = 0;
            } else if (this.mLastArpRttLevel == 100 && this.mArpRttGeneralStartTime != 0) {
                this.mArpRttGeneralDuration += System.currentTimeMillis() - this.mArpRttGeneralStartTime;
                this.mArpRttGeneralStartTime = 0;
            } else if (this.mLastArpRttLevel == 0) {
                this.mArpRttPoorDuration = this.mArpRttPoorStartTime - this.mlastGameWarStartTime;
            }
            this.mLastArpRttLevel = 200;
        }
    }

    public void updateTencentTmgpGameStateChanged(int state, int network) {
        logD("updateTencentTmgpGameStateChanged new state: " + state + ", network:" + network);
        this.mTencentTmgpGameType = state;
        if (this.mTencentTmgpGameType == 0 && this.mAllowStatistics) {
            this.mAllowStatistics = false;
            if (this.mLastGameNetworkLevel == 460 && this.mGameRTTPoorStartTime != 0) {
                this.mNetworkPoorDuration += System.currentTimeMillis() - this.mGameRTTPoorStartTime;
            } else if (this.mLastGameNetworkLevel == 100 && this.mGameRTTSmoothStartTime != 0) {
                this.mNetworkSmoothDuration += System.currentTimeMillis() - this.mGameRTTSmoothStartTime;
            } else if (this.mLastGameNetworkLevel == 200 && this.mGameRTTGeneralStartTime != 0) {
                this.mNetworkGeneralDuration += System.currentTimeMillis() - this.mGameRTTGeneralStartTime;
            }
            this.mNetworkPoorDuration /= 1000;
            this.mNetworkSmoothDuration /= 1000;
            this.mNetworkGeneralDuration /= 1000;
            if (1 == network) {
                if (this.mLastArpRttLevel == 200 && this.mArpRttPoorStartTime != 0) {
                    this.mArpRttPoorDuration += System.currentTimeMillis() - this.mArpRttPoorStartTime;
                } else if (this.mLastArpRttLevel == 30 && this.mArpRttSmoothStartTime != 0) {
                    this.mArpRttSmoothDuration += System.currentTimeMillis() - this.mArpRttSmoothStartTime;
                } else if (this.mLastArpRttLevel == 100 && this.mArpRttGeneralStartTime != 0) {
                    this.mArpRttGeneralDuration += System.currentTimeMillis() - this.mArpRttGeneralStartTime;
                }
                this.mArpRttPoorDuration /= 1000;
                this.mArpRttSmoothDuration /= 1000;
                this.mArpRttGeneralDuration /= 1000;
            }
            if (this.mLastTcpRttLevel == 200 && this.mTcpRttPoorStartTime != 0) {
                this.mTcpRttPoorDuration += System.currentTimeMillis() - this.mTcpRttPoorStartTime;
            } else if (this.mLastTcpRttLevel == 50 && this.mTcpRttSmoothStartTime != 0) {
                this.mTcpRttSmoothDuration += System.currentTimeMillis() - this.mTcpRttSmoothStartTime;
            } else if (this.mLastTcpRttLevel == 100 && this.mTcpRttGeneralStartTime != 0) {
                this.mTcpRttGeneralDuration += System.currentTimeMillis() - this.mTcpRttGeneralStartTime;
            } else if (this.mLastTcpRttLevel == 460 && this.mTcpRttBadStartTime != 0) {
                this.mTcpRttBadDuration += System.currentTimeMillis() - this.mTcpRttBadStartTime;
            }
            this.mTcpRttPoorDuration /= 1000;
            this.mTcpRttSmoothDuration /= 1000;
            this.mTcpRttGeneralDuration /= 1000;
            this.mTcpRttBadDuration /= 1000;
            reportCHR(network);
        } else if (this.mTencentTmgpGameType == 1) {
            initialGameCHR();
            this.mAllowStatistics = true;
            this.mlastGameWarStartTime = System.currentTimeMillis();
        }
    }

    public void updateTencentTmgpGameRttCounter(long rtt) {
        if (rtt > 0 && rtt <= 100) {
            this.mDsKOGRtt100Count++;
        } else if (rtt <= 200) {
            this.mDsKOGRtt200Count++;
        } else if (rtt <= 300) {
            this.mDsKOGRtt300Count++;
        } else if (rtt <= 400) {
            this.mDsKOGRtt400Count++;
        } else {
            this.mDsKOGRttOver400Count++;
        }
        this.mDsKOGTotalRttSum = (int) (((long) this.mDsKOGTotalRttSum) + rtt);
        this.mDsKOGTotalRttCount++;
    }

    private void sendIntentDsKOGTCPRttStatic(Context context) {
        if (context == null) {
            logD("sendIntentDsKOGTCPRttStatic context is null");
            return;
        }
        try {
            Intent chrIntent = new Intent(INTENT_DS_KOG_RTT_REPORT);
            Bundle extras = new Bundle();
            extras.putInt("ReportType", 2);
            extras.putInt("mTcpRttSmoothDuration", (int) this.mTcpRttSmoothDuration);
            extras.putInt("mTcpRttGeneralDuration", (int) this.mTcpRttGeneralDuration);
            extras.putInt("mTcpRttPoorDuration", (int) this.mTcpRttPoorDuration);
            extras.putInt("mTcpRttBadDuration", (int) this.mTcpRttBadDuration);
            extras.putInt("mNetworkSmoothDuration", (int) this.mNetworkSmoothDuration);
            extras.putInt("mNetworkGeneralDuration", (int) this.mNetworkGeneralDuration);
            extras.putInt("mNetworkPoorDuration", (int) this.mNetworkPoorDuration);
            extras.putInt("mDsKOGRtt100Count", this.mDsKOGRtt100Count);
            extras.putInt("mDsKOGRtt200Count", this.mDsKOGRtt200Count);
            extras.putInt("mDsKOGRtt300Count", this.mDsKOGRtt300Count);
            extras.putInt("mDsKOGRtt400Count", this.mDsKOGRtt400Count);
            extras.putInt("mDsKOGRttOver400Count", this.mDsKOGRttOver400Count);
            extras.putInt("mDsKOGTotalRttSum", this.mDsKOGTotalRttSum);
            extras.putInt("mDsKOGTotalRttCount", this.mDsKOGTotalRttCount);
            chrIntent.putExtras(extras);
            context.sendBroadcast(chrIntent, CHR_BROADCAST_PERMISSION);
            logD(" sendIntentDsKOGTCPRttStatic mTcpRttSmoothDuration = " + this.mTcpRttSmoothDuration + " mTcpRttGeneralDuration = " + this.mTcpRttGeneralDuration + " mTcpRttPoorDuration = " + this.mTcpRttPoorDuration + " mTcpRttBadDuration = " + this.mTcpRttBadDuration + " mNetworkSmoothDuration = " + this.mNetworkSmoothDuration + " mNetworkGeneralDuration = " + this.mNetworkGeneralDuration + " mNetworkPoorDuration = " + this.mNetworkPoorDuration + " mDsKOGRtt100Count = " + this.mDsKOGRtt100Count + " mDsKOGRtt200Count = " + this.mDsKOGRtt200Count + " mDsKOGRtt300Count = " + this.mDsKOGRtt300Count + " mDsKOGRtt400Count = " + this.mDsKOGRtt400Count + " mDsKOGRttOver400Count = " + this.mDsKOGRttOver400Count + " mDsKOGTotalRttSum = " + this.mDsKOGTotalRttSum + " mDsKOGTotalRttCount = " + this.mDsKOGTotalRttCount);
        } catch (RuntimeException e) {
            logD("sendIntentDsKOGTCPRttStatic get state RuntimeException");
        } catch (Exception e2) {
            logD("sendIntentDsKOGTCPRttStatic get state Exception ");
        }
    }

    private void reportCHR(int network) {
        if (1 == network && this.mHwWifiGameNetChrInfo != null) {
            chrInfoDump();
            this.mHwWifiGameNetChrInfo.setArpRttGeneralDuration(this.mArpRttGeneralDuration);
            this.mHwWifiGameNetChrInfo.setArpRttPoorDuration(this.mArpRttPoorDuration);
            this.mHwWifiGameNetChrInfo.setArpRttSmoothDuration(this.mArpRttSmoothDuration);
            this.mHwWifiGameNetChrInfo.setNetworkGeneralDuration(this.mNetworkGeneralDuration);
            this.mHwWifiGameNetChrInfo.setNetworkPoorDuration(this.mNetworkPoorDuration);
            this.mHwWifiGameNetChrInfo.setNetworkSmoothDuration(this.mNetworkSmoothDuration);
            this.mHwWifiGameNetChrInfo.setTcpRttBadDuration(this.mTcpRttBadDuration);
            this.mHwWifiGameNetChrInfo.setTcpRttGeneralDuration(this.mTcpRttGeneralDuration);
            this.mHwWifiGameNetChrInfo.setTcpRttPoorDuration(this.mTcpRttPoorDuration);
            this.mHwWifiGameNetChrInfo.setTcpRttSmoothDuration(this.mTcpRttSmoothDuration);
            this.mHwWifiGameNetChrInfo.setWifiDisCounter(this.mWifiDisCounter);
            this.mHwWifiGameNetChrInfo.setWifiRoamingCounter(this.mWifiRoamingCounter);
            this.mHwWifiGameNetChrInfo.setWifiScanCounter(this.mWifiScanCounter);
            this.mHwWifiGameNetChrInfo.setBTScan24GCounter(this.mBTScan24GCounter);
            if (2 == this.mApType) {
                this.mHwWifiGameNetChrInfo.setAP24gBTCoexist(true);
            }
            if (3 == this.mApType) {
                this.mHwWifiGameNetChrInfo.setAP5gOnly(true);
                this.mWifiProStatisticsManager.update5gGameCHR(this.mHwWifiGameNetChrInfo);
            } else if (1 == this.mApType || 2 == this.mApType) {
                this.mHwWifiGameNetChrInfo.setAP5gOnly(false);
                this.mWifiProStatisticsManager.update24gGameCHR(this.mHwWifiGameNetChrInfo);
            }
        } else if (network == 0) {
            sendIntentDsKOGTCPRttStatic(this.mContext);
        }
    }

    public void updateWifiFrequency(boolean is5G) {
        if (is5G) {
            this.mApType = 3;
        } else if (getBluetoothConneced()) {
            this.mApType = 2;
        } else {
            this.mApType = 1;
        }
        logD("updateWifiFrequency: mApType:" + this.mApType);
    }

    public void updateWifiRoaming() {
        if (this.mTencentTmgpGameType == 1) {
            this.mWifiRoamingCounter = (short) (this.mWifiRoamingCounter + 1);
        }
    }

    public void updateWiFiScanCounter() {
        if (this.mTencentTmgpGameType == 1) {
            this.mWifiScanCounter = (short) (this.mWifiScanCounter + 1);
        }
    }

    public void updateBTScanCounter() {
        if (this.mTencentTmgpGameType == 1 && this.mApType != 3) {
            this.mBTScan24GCounter = (short) (this.mBTScan24GCounter + 1);
        }
    }

    public void updateWiFiDisCounter() {
        if (this.mTencentTmgpGameType == 1) {
            this.mWifiDisCounter = (short) (this.mWifiDisCounter + 1);
        }
    }

    public void updateArpResult(boolean success, int arpRtt) {
        if (!success) {
            this.mWifiArpFailCounter = (short) (this.mWifiArpFailCounter + 1);
        } else if (this.mTencentTmgpGameType == 1) {
            updateArpRttChanged(arpRtt);
        }
    }

    public void updateWifiTcpRtt(long tcpRttSegs, long tcpRtt, long duration) {
        if (this.mTencentTmgpGameType != 1) {
            logD("Tcp-rtt, TencentTmgpGameType is not KOG_INWAR");
            return;
        }
        logD("updateWifiTcpRtt Tcp-rtt: " + tcpRtt + " ms, duration: " + duration + "ms, tcpRttSegs:" + tcpRttSegs + " Segs" + " , mLastTcpRttLevel:" + this.mLastTcpRttLevel);
        if (tcpRtt < 50) {
            if (this.mLastTcpRttLevel != 50) {
                this.mTcpRttSmoothStartTime = System.currentTimeMillis();
            }
            if (this.mLastTcpRttLevel == 100 && this.mTcpRttGeneralStartTime != 0) {
                this.mTcpRttGeneralDuration += System.currentTimeMillis() - this.mTcpRttGeneralStartTime;
                this.mTcpRttGeneralStartTime = 0;
            } else if (this.mLastTcpRttLevel == 200 && this.mTcpRttPoorStartTime != 0) {
                this.mTcpRttPoorDuration += System.currentTimeMillis() - this.mTcpRttPoorStartTime;
                this.mTcpRttPoorStartTime = 0;
            } else if (this.mLastTcpRttLevel == 0) {
                this.mTcpRttSmoothDuration = this.mTcpRttSmoothStartTime - this.mlastGameWarStartTime;
            } else if (this.mLastTcpRttLevel == 460 && this.mTcpRttBadStartTime != 0) {
                this.mTcpRttBadDuration += System.currentTimeMillis() - this.mTcpRttBadStartTime;
                this.mTcpRttBadStartTime = 0;
            }
            this.mLastTcpRttLevel = 50;
        } else if (tcpRtt < 100) {
            if (this.mLastTcpRttLevel != 100) {
                this.mTcpRttGeneralStartTime = System.currentTimeMillis();
            }
            if (this.mLastTcpRttLevel == 50 && this.mTcpRttSmoothStartTime != 0) {
                this.mTcpRttSmoothDuration += System.currentTimeMillis() - this.mTcpRttSmoothStartTime;
                this.mTcpRttSmoothStartTime = 0;
            } else if (this.mLastTcpRttLevel == 200 && this.mTcpRttPoorStartTime != 0) {
                this.mTcpRttPoorDuration += System.currentTimeMillis() - this.mTcpRttPoorStartTime;
                this.mTcpRttPoorStartTime = 0;
            } else if (this.mLastTcpRttLevel == 0) {
                this.mTcpRttGeneralDuration = this.mTcpRttGeneralStartTime - this.mlastGameWarStartTime;
            } else if (this.mLastTcpRttLevel == 460 && this.mTcpRttBadStartTime != 0) {
                this.mTcpRttBadDuration += System.currentTimeMillis() - this.mTcpRttBadStartTime;
                this.mTcpRttBadStartTime = 0;
            }
            this.mLastTcpRttLevel = 100;
        } else if (tcpRtt < 200) {
            statisticsTcpPoorRtt();
        } else {
            statisticsTcpBadRtt();
        }
    }

    private void statisticsTcpPoorRtt() {
        if (this.mLastTcpRttLevel != 200) {
            this.mTcpRttPoorStartTime = System.currentTimeMillis();
        }
        if (this.mLastTcpRttLevel == 50 && this.mTcpRttSmoothStartTime != 0) {
            this.mTcpRttSmoothDuration += System.currentTimeMillis() - this.mTcpRttSmoothStartTime;
            this.mTcpRttSmoothStartTime = 0;
        } else if (this.mLastTcpRttLevel == 100 && this.mTcpRttGeneralStartTime != 0) {
            this.mTcpRttGeneralDuration += System.currentTimeMillis() - this.mTcpRttGeneralStartTime;
            this.mTcpRttGeneralStartTime = 0;
        } else if (this.mLastTcpRttLevel == 0) {
            this.mTcpRttPoorDuration = this.mTcpRttPoorStartTime - this.mlastGameWarStartTime;
        } else if (this.mLastTcpRttLevel == 460 && this.mTcpRttBadStartTime != 0) {
            this.mTcpRttBadDuration += System.currentTimeMillis() - this.mTcpRttBadStartTime;
            this.mTcpRttBadStartTime = 0;
        }
        this.mLastTcpRttLevel = 200;
    }

    private void statisticsTcpBadRtt() {
        if (this.mLastTcpRttLevel != 460) {
            this.mTcpRttBadStartTime = System.currentTimeMillis();
        }
        if (this.mLastTcpRttLevel == 50 && this.mTcpRttSmoothStartTime != 0) {
            this.mTcpRttSmoothDuration += System.currentTimeMillis() - this.mTcpRttSmoothStartTime;
            this.mTcpRttSmoothStartTime = 0;
        } else if (this.mLastTcpRttLevel == 200 && this.mTcpRttPoorStartTime != 0) {
            this.mTcpRttPoorDuration += System.currentTimeMillis() - this.mTcpRttPoorStartTime;
            this.mTcpRttPoorStartTime = 0;
        } else if (this.mLastTcpRttLevel == 0) {
            this.mTcpRttBadDuration = this.mTcpRttBadStartTime - this.mlastGameWarStartTime;
        } else if (this.mLastTcpRttLevel == 100 && this.mTcpRttGeneralStartTime != 0) {
            this.mTcpRttGeneralDuration += System.currentTimeMillis() - this.mTcpRttGeneralStartTime;
            this.mTcpRttGeneralStartTime = 0;
        }
        this.mLastTcpRttLevel = 460;
    }

    private boolean getBluetoothConneced() {
        if (this.mLocalBluetoothAdapter == null) {
            this.mLocalBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        if (this.mLocalBluetoothAdapter == null || this.mLocalBluetoothAdapter.getConnectionState() != 2) {
            return false;
        }
        return true;
    }

    private void chrInfoDump() {
        StringBuffer buffer = new StringBuffer("Game CHR Info : ");
        buffer.append("ApType: ");
        buffer.append(this.mApType);
        if (1 == this.mApType) {
            buffer.append("_AP_TYPE_24G_ONLY");
        } else if (2 == this.mApType) {
            buffer.append("_AP_TYPE_24G_BT_COEXIST");
        } else if (3 == this.mApType) {
            buffer.append("_AP_TYPE_5G_ONLY");
        }
        buffer.append(", GameRttSmoothDuration: ").append(this.mNetworkSmoothDuration);
        buffer.append(" s, GameRttGeneralDuration: ").append(this.mNetworkGeneralDuration);
        buffer.append(" s, GameRttPoorDuration: ").append(this.mNetworkPoorDuration);
        buffer.append(" s, ArpRttSmoothDuration: ").append(this.mArpRttSmoothDuration);
        buffer.append(" s, ArpRttGeneralDuration: ").append(this.mArpRttGeneralDuration);
        buffer.append(" s, ArpRttPoorDuration: ").append(this.mArpRttPoorDuration);
        buffer.append(" s, TcpRttSmoothDuration: ").append(this.mTcpRttSmoothDuration);
        buffer.append(" s, TcpRttGeneralDuration: ").append(this.mTcpRttGeneralDuration);
        buffer.append(" s, TcpRttPoorDuration: ").append(this.mTcpRttPoorDuration);
        buffer.append(" s, TcpRttBadDuration: ").append(this.mTcpRttBadDuration);
        buffer.append(" s, WifiDisCounter: ").append(this.mWifiDisCounter);
        buffer.append(" , WifiScanCounter: ").append(this.mWifiScanCounter);
        buffer.append(" , WifiRoamingCounter: ").append(this.mWifiRoamingCounter);
        logD(buffer.toString());
    }

    private void logD(String info) {
        Log.d(TAG, info);
    }
}
