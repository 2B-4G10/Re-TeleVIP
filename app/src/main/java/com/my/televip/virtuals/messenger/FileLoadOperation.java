package com.my.televip.virtuals.messenger;

import com.my.televip.obfuscate.AutomationResolver;

import com.my.televip.reflect.XReflect;

public class FileLoadOperation {

    private final Object fileOperation;

    public FileLoadOperation(Object fileOperation){ this.fileOperation = fileOperation; }

    // R8 narrows some of these to byte where the client's values fit (Nekogram 12.10.5+).

    public void setDownloadChunkSizeBig(int v){
        XReflect.setNumberField(fileOperation, AutomationResolver.resolve("FileLoadOperation", "downloadChunkSizeBig", AutomationResolver.ResolverType.Field), v);
    }

    public void setMaxDownloadRequests(int v){
        XReflect.setNumberField(fileOperation, AutomationResolver.resolve("FileLoadOperation", "maxDownloadRequests", AutomationResolver.ResolverType.Field), v);
    }

    public void setMaxDownloadRequestsBig(int v){
        XReflect.setNumberField(fileOperation, AutomationResolver.resolve("FileLoadOperation", "maxDownloadRequestsBig", AutomationResolver.ResolverType.Field), v);
    }

    public void setMaxCdnParts(int v){
        XReflect.setNumberField(fileOperation, AutomationResolver.resolve("FileLoadOperation", "maxCdnParts", AutomationResolver.ResolverType.Field), v);
    }

}
