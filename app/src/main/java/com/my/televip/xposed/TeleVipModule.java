package com.my.televip.xposed;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface;

/**
 * The module's entry point (libxposed API 102), registered in
 * {@code META-INF/xposed/java_init.list}. Nothing is initialised in the constructor: the framework
 * attaches first and then calls {@link #onModuleLoaded}, which hands over to
 * {@link ModuleEntry#attach}.
 */
public class TeleVipModule extends XposedModule {

    public TeleVipModule() {
        super();
    }

    @Override
    public void onModuleLoaded(XposedModuleInterface.ModuleLoadedParam param) {
        super.onModuleLoaded(param);

        String modulePath = null;
        try {
            // Replaces the legacy initZygote() startupParam.modulePath, which the modern API
            // does not have. Needed to read assets/lang/*.json out of our own APK.
            modulePath = getModuleApplicationInfo().sourceDir;
        } catch (Throwable ignored) {
        }

        XBridge.install(new ModernBackend(this, modulePath));
        XBridge.setModulePath(modulePath);
    }

    @Override
    public void onPackageReady(XposedModuleInterface.PackageReadyParam param) {
        super.onPackageReady(param);
        if (!param.isFirstPackage()) return;
        // getClassLoader() rather than getDefaultClassLoader(): it is correct even for clients
        // shipping a custom AppComponentFactory, and it carries no minSdk 29 requirement.
        ModuleEntry.attach(param.getPackageName(), param.getClassLoader());
    }
}
