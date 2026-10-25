package com.my.televip.features;

import com.my.televip.Class.ClassNames;
import com.my.televip.Class.ClassLoad;
import com.my.televip.Configs.ConfigManager;
import com.my.televip.base.AbstractMethodHook;
import com.my.televip.hooks.HMethod;
import com.my.televip.logging.Logger;
import com.my.televip.obfuscate.AutomationResolver;
import com.my.televip.virtuals.messenger.FileLoadOperation;
import com.my.televip.virtuals.messenger.UserConfig;

public class DownloadSpeed {

    public static boolean isEnable = false;

    public static void init(){
        try {
            if (!isEnable) {
                isEnable = true;

                if (ClassLoad.getClass(ClassNames.FILE_LOAD_OPERATION) != null) {
                    HMethod.hookMethod(ClassLoad.getClass(ClassNames.FILE_LOAD_OPERATION), AutomationResolver.resolve("FileLoadOperation", "updateParams", AutomationResolver.ResolverType.Method), new AbstractMethodHook() {
                        @Override
                        protected void afterMethod(MethodHookParam param) {
                            // Telegram limits download speed on its servers for accounts without
                            // Premium, and answers more parallel requests with waits: measured on
                            // Nekogram, a 67 MB file took 23-24 s this way against 19-21 s without.
                            // So the larger parts are only used where the account really has Premium.
                            if (ConfigManager.downloadSpeed.isEnable() && UserConfig.hasRealPremium()) {
                                FileLoadOperation fileLoadOperation = new FileLoadOperation(param.thisObject);

                                int downloadChunkSizeBig = 1024 * 512;
                                int maxDownloadRequests = 8;

                                long defaultMaxFileSize = 1024L * 1024L * 2000L;

                                int maxCdnParts = (int) (defaultMaxFileSize / downloadChunkSizeBig);

                                fileLoadOperation.setDownloadChunkSizeBig(downloadChunkSizeBig);
                                fileLoadOperation.setMaxDownloadRequests(maxDownloadRequests);
                                fileLoadOperation.setMaxDownloadRequestsBig(maxDownloadRequests);
                                fileLoadOperation.setMaxCdnParts(maxCdnParts);
                                param.setResult(null);
                            }

                        }
                    });
                }
            }
        } catch (Throwable t){
            Logger.e(t);
        }
    }

}
