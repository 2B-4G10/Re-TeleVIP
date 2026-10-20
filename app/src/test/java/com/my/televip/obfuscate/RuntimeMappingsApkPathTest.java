package com.my.televip.obfuscate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/** Finding the client's APK from its class loader, under each framework that can host TeleVip. */
public class RuntimeMappingsApkPathTest {

    private static ClassLoader describedAs(final String description) {
        return new ClassLoader(null) {
            @Override
            public String toString() {
                return description;
            }
        };
    }

    @Test
    public void installedClient() {
        String base = "/data/app/~~Xc2w==/org.telegram.messenger-1Q9g==/base.apk";
        ClassLoader loader = describedAs("dalvik.system.PathClassLoader[DexPathList[[zip file \""
                + base + "\", zip file \"/data/app/~~Xc2w==/org.telegram.messenger-1Q9g==/split_config.arm64_v8a.apk\"],"
                + "nativeLibraryDirectories=[/data/app/~~Xc2w==/org.telegram.messenger-1Q9g==/lib/arm64]]]");
        assertEquals(base, RuntimeMappings.apkPathOf(loader));
    }

    @Test
    public void lspatchRunsTheExtractedOriginal() {
        String origin = "/data/user/0/org.telegram.messenger/cache/lspatch/origin/1234567890.apk";
        ClassLoader loader = describedAs("dalvik.system.PathClassLoader[DexPathList[[zip file \""
                + origin + "\"],nativeLibraryDirectories=[/data/app/~~Ab==/org.telegram.messenger-Cd==/lib/arm64]]]");
        assertEquals(origin, RuntimeMappings.apkPathOf(loader));
    }

    @Test
    public void noApkOnThePath() {
        assertNull(RuntimeMappings.apkPathOf(describedAs("dalvik.system.InMemoryDexClassLoader[DexPathList[[],nativeLibraryDirectories=[]]]")));
    }
}
