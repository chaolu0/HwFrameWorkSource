package com.android.server.webkit;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.util.Log;
import android.util.Slog;
import android.webkit.UserPackage;
import android.webkit.WebViewFactory;
import android.webkit.WebViewProviderInfo;
import android.webkit.WebViewProviderResponse;
import com.android.server.os.HwBootFail;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

class WebViewUpdater {
    private static final String TAG = WebViewUpdater.class.getSimpleName();
    private static final int VALIDITY_INCORRECT_SDK_VERSION = 1;
    private static final int VALIDITY_INCORRECT_SIGNATURE = 3;
    private static final int VALIDITY_INCORRECT_VERSION_CODE = 2;
    private static final int VALIDITY_NO_LIBRARY_FLAG = 4;
    private static final int VALIDITY_OK = 0;
    private static final int WAIT_TIMEOUT_MS = 1000;
    private int NUMBER_OF_RELROS_UNKNOWN = HwBootFail.STAGE_BOOT_SUCCESS;
    private boolean mAnyWebViewInstalled = false;
    private Context mContext;
    private PackageInfo mCurrentWebViewPackage = null;
    private final Object mLock = new Object();
    private long mMinimumVersionCode = -1;
    private int mNumRelroCreationsFinished = 0;
    private int mNumRelroCreationsStarted = 0;
    private SystemInterface mSystemInterface;
    private boolean mWebViewPackageDirty = false;

    private static class ProviderAndPackageInfo {
        public final PackageInfo packageInfo;
        public final WebViewProviderInfo provider;

        public ProviderAndPackageInfo(WebViewProviderInfo provider, PackageInfo packageInfo) {
            this.provider = provider;
            this.packageInfo = packageInfo;
        }
    }

    private static class WebViewPackageMissingException extends Exception {
        public WebViewPackageMissingException(String message) {
            super(message);
        }

        public WebViewPackageMissingException(Exception e) {
            super(e);
        }
    }

    WebViewUpdater(Context context, SystemInterface systemInterface) {
        this.mContext = context;
        this.mSystemInterface = systemInterface;
    }

    /* JADX WARNING: Removed duplicated region for block: B:37:0x006d A:{Catch:{ WebViewPackageMissingException -> 0x0073 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void packageStateChanged(String packageName, int changedState) {
        boolean updateWebView;
        WebViewProviderInfo[] webViewPackages = this.mSystemInterface.getWebViewPackages();
        int length = webViewPackages.length;
        boolean z = false;
        String oldProviderName = null;
        while (oldProviderName < length) {
            WebViewProviderInfo provider = webViewPackages[oldProviderName];
            if (provider.packageName.equals(packageName)) {
                updateWebView = false;
                boolean removedOrChangedOldPackage = false;
                oldProviderName = null;
                synchronized (this.mLock) {
                    PackageInfo newPackage = findPreferredWebViewPackage();
                    if (this.mCurrentWebViewPackage != null) {
                        oldProviderName = this.mCurrentWebViewPackage.packageName;
                        if (changedState == 0 && newPackage.packageName.equals(oldProviderName)) {
                            return;
                        }
                        try {
                            if (newPackage.packageName.equals(oldProviderName) && newPackage.lastUpdateTime == this.mCurrentWebViewPackage.lastUpdateTime) {
                                return;
                            }
                        } catch (WebViewPackageMissingException e) {
                            this.mCurrentWebViewPackage = null;
                            String str = TAG;
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("Could not find valid WebView package to create relro with ");
                            stringBuilder.append(e);
                            Slog.e(str, stringBuilder.toString());
                        }
                    }
                    if (!(provider.packageName.equals(newPackage.packageName) || provider.packageName.equals(oldProviderName))) {
                        if (this.mCurrentWebViewPackage != null) {
                            updateWebView = z;
                            removedOrChangedOldPackage = provider.packageName.equals(oldProviderName);
                            if (updateWebView) {
                                onWebViewProviderChanged(newPackage);
                            }
                        }
                    }
                    z = true;
                    updateWebView = z;
                    removedOrChangedOldPackage = provider.packageName.equals(oldProviderName);
                    if (updateWebView) {
                    }
                }
            } else {
                oldProviderName++;
            }
        }
        return;
        if (!(!updateWebView || removedOrChangedOldPackage || oldProviderName == null)) {
            this.mSystemInterface.killPackageDependents(oldProviderName);
        }
    }

    void prepareWebViewInSystemServer() {
        try {
            synchronized (this.mLock) {
                this.mCurrentWebViewPackage = findPreferredWebViewPackage();
                this.mSystemInterface.updateUserSetting(this.mContext, this.mCurrentWebViewPackage.packageName);
                onWebViewProviderChanged(this.mCurrentWebViewPackage);
            }
        } catch (Throwable t) {
            Slog.e(TAG, "error preparing webview provider from system server", t);
        }
    }

    String changeProviderAndSetting(String newProviderName) {
        PackageInfo newPackage = updateCurrentWebViewPackage(newProviderName);
        if (newPackage == null) {
            return BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        }
        return newPackage.packageName;
    }

    PackageInfo updateCurrentWebViewPackage(String newProviderName) {
        PackageInfo oldPackage;
        PackageInfo newPackage;
        boolean providerChanged;
        synchronized (this.mLock) {
            oldPackage = this.mCurrentWebViewPackage;
            if (newProviderName != null) {
                this.mSystemInterface.updateUserSetting(this.mContext, newProviderName);
            }
            try {
                newPackage = findPreferredWebViewPackage();
                boolean z = oldPackage == null || !newPackage.packageName.equals(oldPackage.packageName);
                providerChanged = z;
                if (providerChanged) {
                    onWebViewProviderChanged(newPackage);
                }
            } catch (WebViewPackageMissingException e) {
                this.mCurrentWebViewPackage = null;
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Couldn't find WebView package to use ");
                stringBuilder.append(e);
                Slog.e(str, stringBuilder.toString());
                return null;
            }
        }
        if (providerChanged && oldPackage != null) {
            this.mSystemInterface.killPackageDependents(oldPackage.packageName);
        }
        return newPackage;
    }

    private void onWebViewProviderChanged(PackageInfo newPackage) {
        synchronized (this.mLock) {
            this.mAnyWebViewInstalled = true;
            if (this.mNumRelroCreationsStarted == this.mNumRelroCreationsFinished) {
                this.mCurrentWebViewPackage = newPackage;
                this.mNumRelroCreationsStarted = this.NUMBER_OF_RELROS_UNKNOWN;
                this.mNumRelroCreationsFinished = 0;
                this.mNumRelroCreationsStarted = this.mSystemInterface.onWebViewProviderChanged(newPackage);
                checkIfRelrosDoneLocked();
            } else {
                this.mWebViewPackageDirty = true;
            }
        }
    }

    WebViewProviderInfo[] getValidWebViewPackages() {
        ProviderAndPackageInfo[] providersAndPackageInfos = getValidWebViewPackagesAndInfos();
        WebViewProviderInfo[] providers = new WebViewProviderInfo[providersAndPackageInfos.length];
        for (int n = 0; n < providersAndPackageInfos.length; n++) {
            providers[n] = providersAndPackageInfos[n].provider;
        }
        return providers;
    }

    private ProviderAndPackageInfo[] getValidWebViewPackagesAndInfos() {
        WebViewProviderInfo[] allProviders = this.mSystemInterface.getWebViewPackages();
        List<ProviderAndPackageInfo> providers = new ArrayList();
        for (int n = 0; n < allProviders.length; n++) {
            try {
                PackageInfo packageInfo = this.mSystemInterface.getPackageInfoForProvider(allProviders[n]);
                if (isValidProvider(allProviders[n], packageInfo)) {
                    providers.add(new ProviderAndPackageInfo(allProviders[n], packageInfo));
                    if (allProviders[n] != null) {
                        String str = TAG;
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("getValidWebViewPackagesAndInfos = : ");
                        stringBuilder.append(allProviders[n].packageName);
                        Log.i(str, stringBuilder.toString());
                    }
                }
            } catch (NameNotFoundException e) {
            }
        }
        return (ProviderAndPackageInfo[]) providers.toArray(new ProviderAndPackageInfo[providers.size()]);
    }

    private PackageInfo findPreferredWebViewPackage() throws WebViewPackageMissingException {
        ProviderAndPackageInfo[] providers = getValidWebViewPackagesAndInfos();
        String userChosenProvider = this.mSystemInterface.getUserChosenWebViewProvider(this.mContext);
        for (ProviderAndPackageInfo providerAndPackage : providers) {
            if (providerAndPackage.provider.packageName.equals(userChosenProvider) && isInstalledAndEnabledForAllUsers(this.mSystemInterface.getPackageInfoForProviderAllUsers(this.mContext, providerAndPackage.provider))) {
                return providerAndPackage.packageInfo;
            }
        }
        for (ProviderAndPackageInfo providerAndPackage2 : providers) {
            if (providerAndPackage2.provider.availableByDefault && isInstalledAndEnabledForAllUsers(this.mSystemInterface.getPackageInfoForProviderAllUsers(this.mContext, providerAndPackage2.provider))) {
                return providerAndPackage2.packageInfo;
            }
        }
        this.mAnyWebViewInstalled = false;
        throw new WebViewPackageMissingException("Could not find a loadable WebView package");
    }

    static boolean isInstalledAndEnabledForAllUsers(List<UserPackage> userPackages) {
        for (UserPackage userPackage : userPackages) {
            if (!userPackage.isInstalledPackage() || !userPackage.isEnabledPackage()) {
                return false;
            }
        }
        return true;
    }

    void notifyRelroCreationCompleted() {
        synchronized (this.mLock) {
            this.mNumRelroCreationsFinished++;
            checkIfRelrosDoneLocked();
        }
    }

    WebViewProviderResponse waitForAndGetProvider() {
        boolean webViewReady;
        PackageInfo webViewPackage;
        long timeoutTimeMs = (System.nanoTime() / 1000000) + 1000;
        int webViewStatus = 0;
        synchronized (this.mLock) {
            webViewReady = webViewIsReadyLocked();
            while (!webViewReady) {
                long timeNowMs = System.nanoTime() / 1000000;
                if (timeNowMs >= timeoutTimeMs) {
                    break;
                }
                try {
                    this.mLock.wait(timeoutTimeMs - timeNowMs);
                } catch (InterruptedException e) {
                }
                webViewReady = webViewIsReadyLocked();
            }
            webViewPackage = this.mCurrentWebViewPackage;
            if (!webViewReady) {
                if (this.mAnyWebViewInstalled) {
                    webViewStatus = 3;
                    String str = TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Timed out waiting for relro creation, relros started ");
                    stringBuilder.append(this.mNumRelroCreationsStarted);
                    stringBuilder.append(" relros finished ");
                    stringBuilder.append(this.mNumRelroCreationsFinished);
                    stringBuilder.append(" package dirty? ");
                    stringBuilder.append(this.mWebViewPackageDirty);
                    Slog.e(str, stringBuilder.toString());
                } else {
                    webViewStatus = 4;
                }
            }
        }
        if (!webViewReady) {
            Slog.w(TAG, "creating relro file timed out");
        }
        return new WebViewProviderResponse(webViewPackage, webViewStatus);
    }

    PackageInfo getCurrentWebViewPackage() {
        PackageInfo packageInfo;
        synchronized (this.mLock) {
            packageInfo = this.mCurrentWebViewPackage;
        }
        return packageInfo;
    }

    private boolean webViewIsReadyLocked() {
        return !this.mWebViewPackageDirty && this.mNumRelroCreationsStarted == this.mNumRelroCreationsFinished && this.mAnyWebViewInstalled;
    }

    private void checkIfRelrosDoneLocked() {
        if (this.mNumRelroCreationsStarted != this.mNumRelroCreationsFinished) {
            return;
        }
        if (this.mWebViewPackageDirty) {
            this.mWebViewPackageDirty = false;
            try {
                onWebViewProviderChanged(findPreferredWebViewPackage());
                return;
            } catch (WebViewPackageMissingException e) {
                this.mCurrentWebViewPackage = null;
                return;
            }
        }
        this.mLock.notifyAll();
    }

    boolean isValidProvider(WebViewProviderInfo configInfo, PackageInfo packageInfo) {
        return validityResult(configInfo, packageInfo) == 0;
    }

    private int validityResult(WebViewProviderInfo configInfo, PackageInfo packageInfo) {
        if (!UserPackage.hasCorrectTargetSdkVersion(packageInfo)) {
            return 1;
        }
        if (!versionCodeGE(packageInfo.getLongVersionCode(), getMinimumVersionCode()) && !this.mSystemInterface.systemIsDebuggable()) {
            return 2;
        }
        if (!providerHasValidSignature(configInfo, packageInfo, this.mSystemInterface)) {
            return 3;
        }
        if (WebViewFactory.getWebViewLibrary(packageInfo.applicationInfo) == null) {
            return 4;
        }
        return 0;
    }

    private static boolean versionCodeGE(long versionCode1, long versionCode2) {
        return versionCode1 / 100000 >= versionCode2 / 100000;
    }

    private long getMinimumVersionCode() {
        if (this.mMinimumVersionCode > 0) {
            return this.mMinimumVersionCode;
        }
        long minimumVersionCode = -1;
        for (WebViewProviderInfo provider : this.mSystemInterface.getWebViewPackages()) {
            if (provider.availableByDefault && !provider.isFallback) {
                try {
                    long versionCode = this.mSystemInterface.getFactoryPackageVersion(provider.packageName);
                    if (minimumVersionCode < 0 || versionCode < minimumVersionCode) {
                        minimumVersionCode = versionCode;
                    }
                } catch (NameNotFoundException e) {
                }
            }
        }
        this.mMinimumVersionCode = minimumVersionCode;
        return this.mMinimumVersionCode;
    }

    private static boolean providerHasValidSignature(WebViewProviderInfo provider, PackageInfo packageInfo, SystemInterface systemInterface) {
        if (systemInterface.systemIsDebuggable()) {
            return true;
        }
        if (provider.signatures == null || provider.signatures.length == 0) {
            return packageInfo.applicationInfo.isSystemApp();
        }
        if (packageInfo.signatures.length != 1) {
            return false;
        }
        for (Signature signature : provider.signatures) {
            if (signature.equals(packageInfo.signatures[0])) {
                return true;
            }
        }
        return false;
    }

    void dumpState(PrintWriter pw) {
        synchronized (this.mLock) {
            if (this.mCurrentWebViewPackage == null) {
                pw.println("  Current WebView package is null");
            } else {
                pw.println(String.format("  Current WebView package (name, version): (%s, %s)", new Object[]{this.mCurrentWebViewPackage.packageName, this.mCurrentWebViewPackage.versionName}));
            }
            pw.println(String.format("  Minimum WebView version code: %d", new Object[]{Long.valueOf(this.mMinimumVersionCode)}));
            pw.println(String.format("  Number of relros started: %d", new Object[]{Integer.valueOf(this.mNumRelroCreationsStarted)}));
            pw.println(String.format("  Number of relros finished: %d", new Object[]{Integer.valueOf(this.mNumRelroCreationsFinished)}));
            pw.println(String.format("  WebView package dirty: %b", new Object[]{Boolean.valueOf(this.mWebViewPackageDirty)}));
            pw.println(String.format("  Any WebView package installed: %b", new Object[]{Boolean.valueOf(this.mAnyWebViewInstalled)}));
            try {
                PackageInfo preferredWebViewPackage = findPreferredWebViewPackage();
                pw.println(String.format("  Preferred WebView package (name, version): (%s, %s)", new Object[]{preferredWebViewPackage.packageName, preferredWebViewPackage.versionName}));
            } catch (WebViewPackageMissingException e) {
                pw.println(String.format("  Preferred WebView package: none", new Object[0]));
            }
            dumpAllPackageInformationLocked(pw);
        }
    }

    private void dumpAllPackageInformationLocked(PrintWriter pw) {
        PrintWriter printWriter = pw;
        WebViewProviderInfo[] allProviders = this.mSystemInterface.getWebViewPackages();
        printWriter.println("  WebView packages:");
        for (WebViewProviderInfo provider : allProviders) {
            PackageInfo systemUserPackageInfo = ((UserPackage) this.mSystemInterface.getPackageInfoForProviderAllUsers(this.mContext, provider).get(0)).getPackageInfo();
            if (systemUserPackageInfo == null) {
                printWriter.println(String.format("    %s is NOT installed.", new Object[]{provider.packageName}));
            } else {
                int validity = validityResult(provider, systemUserPackageInfo);
                String packageDetails = String.format("versionName: %s, versionCode: %d, targetSdkVersion: %d", new Object[]{systemUserPackageInfo.versionName, Long.valueOf(systemUserPackageInfo.getLongVersionCode()), Integer.valueOf(systemUserPackageInfo.applicationInfo.targetSdkVersion)});
                if (validity == 0) {
                    boolean installedForAllUsers = isInstalledAndEnabledForAllUsers(this.mSystemInterface.getPackageInfoForProviderAllUsers(this.mContext, provider));
                    String str = "    Valid package %s (%s) is %s installed/enabled for all users";
                    Object[] objArr = new Object[3];
                    objArr[0] = systemUserPackageInfo.packageName;
                    objArr[1] = packageDetails;
                    objArr[2] = installedForAllUsers ? BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS : "NOT";
                    printWriter.println(String.format(str, objArr));
                } else {
                    printWriter.println(String.format("    Invalid package %s (%s), reason: %s", new Object[]{systemUserPackageInfo.packageName, packageDetails, getInvalidityReason(validity)}));
                }
            }
        }
    }

    private static String getInvalidityReason(int invalidityReason) {
        switch (invalidityReason) {
            case 1:
                return "SDK version too low";
            case 2:
                return "Version code too low";
            case 3:
                return "Incorrect signature";
            case 4:
                return "No WebView-library manifest flag";
            default:
                return "Unexcepted validity-reason";
        }
    }
}
