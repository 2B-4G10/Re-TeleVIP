package com.my.televip.virtuals.messenger;

import com.my.televip.Class.ClassNames;
import com.my.televip.Class.ClassLoad;
import com.my.televip.obfuscate.AutomationResolver;

import java.io.File;

import com.my.televip.reflect.XReflect;

public class FileLoader {

    Object fileLoader;

    public FileLoader(Object fileLoader){
        this.fileLoader = fileLoader;
    }

    public File getLocalFile(ImageLocation location) {
        return (File) XReflect.callMethod(fileLoader, AutomationResolver.resolve("FileLoader", "getLocalFile", AutomationResolver.ResolverType.Method), location.imageLocation);
    }

    /** Where the client keeps the message's photo or video, downloaded or not. */
    public File getPathToMessage(Object message) {
        Class<?> tlMessage = ClassLoad.getClass(ClassNames.TL_MESSAGE);
        java.lang.reflect.Method method = XReflect.findMethodExactIfExists(fileLoader.getClass(),
                AutomationResolver.resolve("FileLoader", "getPathToMessage", AutomationResolver.ResolverType.Method),
                tlMessage);
        File path = invoke(method != null && method.getReturnType() == File.class ? method : null, message);
        if (path != null && (path.exists() || new File(path.getPath() + ".enc").exists())) return path;
        // Renamed builds may map the name to another method (Nekogram 12.10.5): try every
        // File(Message) method of FileLoader, and keep the one whose file is there.
        if (tlMessage == null) return path;
        for (java.lang.reflect.Method m : fileLoader.getClass().getDeclaredMethods()) {
            Class<?>[] p = m.getParameterTypes();
            if (m.equals(method) || m.getReturnType() != File.class || p.length != 1 || p[0] != tlMessage
                    || java.lang.reflect.Modifier.isStatic(m.getModifiers())) continue;
            m.setAccessible(true);
            File candidate = invoke(m, message);
            if (candidate != null && (candidate.exists() || new File(candidate.getPath() + ".enc").exists())) return candidate;
            if (path == null) path = candidate;
        }
        return path;
    }

    private File invoke(java.lang.reflect.Method method, Object message) {
        if (method == null) return null;
        try {
            return (File) method.invoke(fileLoader, message);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    public static FileLoader getInstance(int num) {
        return new FileLoader(XReflect.callStaticMethod(ClassLoad.getClass(ClassNames.FILE_LOADER), AutomationResolver.resolve("FileLoader", "getInstance", AutomationResolver.ResolverType.Method), num));
    }

}
