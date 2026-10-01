package com.my.televip.virtuals.ui;

import android.app.Activity;
import android.view.View;

import com.my.televip.Class.ClassNames;
import com.my.televip.Class.ClassLoad;
import com.my.televip.obfuscate.AutomationResolver;
import com.my.televip.virtuals.messenger.ImageReceiver;
import com.my.televip.virtuals.messenger.MessageObject;

import com.my.televip.logging.Logger;
import com.my.televip.reflect.XReflect;

public class PhotoViewer {

    Object photoViewer;

    public PhotoViewer(Object photoViewer){
        this.photoViewer = photoViewer;
    }

    public static PhotoViewer getInstance(){
        return new PhotoViewer(XReflect.callStaticMethod(ClassLoad.getClass(ClassNames.PHOTO_VIEWER), AutomationResolver.resolve("PhotoViewer", "getInstance", AutomationResolver.ResolverType.Method)));
    }

    public void setParentActivity(Activity activity){
        try {
            XReflect.callMethod(photoViewer, AutomationResolver.resolve("PhotoViewer", "setParentActivity", AutomationResolver.ResolverType.Method), activity);
            return;
        } catch (Throwable ignored) {
            // R8 inlines the one-argument overload into setParentActivity(activity, null, null).
        }
        callMaster(AutomationResolver.resolveOverload("PhotoViewer", "setParentActivityAOO", "setParentActivity"), 3, activity, null, null);
    }

    public void openPhoto(MessageObject messageObject, long l, long l2, long l3, PhotoViewerProvider provider, boolean b){
        try {
            XReflect.callMethod(photoViewer,  AutomationResolver.resolve("PhotoViewer", "openPhoto", AutomationResolver.ResolverType.Method), messageObject.getMessageObject(),l, l2, l3, provider.getPhotoViewerProvider(), b);
            return;
        } catch (Throwable ignored) {
            // R8 inlines every openPhoto overload into the sixteen-argument one they all call.
        }
        callMaster(AutomationResolver.resolveOverload("PhotoViewer", "openPhotoOOOOAAAIOOJJJZOI", "openPhoto"), 16, messageObject.getMessageObject(), null, null, null,
                null, null, null, 0, provider.getPhotoViewerProvider(), null, l, l2, l3, b, null, null);
    }

    /**
     * Calls the overload every other one delegates to, by its mapped name and arity. R8 may
     * reorder its parameters (Nekogram 12.10.5+ groups them by type), so each argument goes to
     * the first free parameter it fits, in order; nulls fill the reference parameters left over.
     */
    private void callMaster(String name, int arity, Object... args) {
        for (Class<?> c = photoViewer.getClass(); c != null; c = c.getSuperclass()) {
            for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
                if (!m.getName().equals(name) || m.getParameterTypes().length != arity) continue;
                Object[] arranged = arrange(m.getParameterTypes(), args);
                if (arranged == null) continue;
                try {
                    m.setAccessible(true);
                    m.invoke(photoViewer, arranged);
                    return;
                } catch (Throwable t) {
                    Logger.e(t);
                    return;
                }
            }
        }
        Logger.e(new NoSuchMethodException("PhotoViewer#" + name + "/" + arity));
    }

    private static Object[] arrange(Class<?>[] types, Object[] args) {
        Object[] out = new Object[types.length];
        boolean[] used = new boolean[types.length];
        for (Object arg : args) {
            if (arg == null) continue;
            int slot = -1;
            for (int i = 0; i < types.length && slot < 0; i++) {
                if (!used[i] && box(types[i]).isInstance(arg)) slot = i;
            }
            if (slot < 0) return null;
            out[slot] = arg;
            used[slot] = true;
        }
        for (int i = 0; i < types.length; i++) {
            if (!used[i] && types[i].isPrimitive()) return null;
        }
        return out;
    }

    private static Class<?> box(Class<?> type) {
        if (!type.isPrimitive()) return type;
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == boolean.class) return Boolean.class;
        if (type == byte.class) return Byte.class;
        if (type == short.class) return Short.class;
        if (type == char.class) return Character.class;
        if (type == float.class) return Float.class;
        if (type == double.class) return Double.class;
        return Void.class;
    }

    public View getGalleryButton(){
        return (View) XReflect.getObjectField(photoViewer,  AutomationResolver.resolve("PhotoViewer", "galleryButton", AutomationResolver.ResolverType.Field));
    }

    public static class PhotoViewerProvider {
        Object photoViewerProvider;

        public PhotoViewerProvider(Object photoViewer) {
            photoViewerProvider = photoViewer;
        }

        public PlaceProviderObject getPlaceForPhoto(MessageObject messageObject, Object fileLocation, int index, boolean needPreview, boolean closing) {
            return new PlaceProviderObject(XReflect.callMethod(photoViewerProvider,  AutomationResolver.resolve("PhotoViewer$PhotoViewerProvider", "getPlaceForPhoto", AutomationResolver.ResolverType.Method), messageObject.getMessageObject(), fileLocation, index, needPreview, closing));
        }

        public Object getPhotoViewerProvider() {
            return photoViewerProvider;
        }

    }

    public static class PlaceProviderObject {
        Object placeProviderObject;

        public PlaceProviderObject(Object placeProvider) {
            placeProviderObject = placeProvider;
        }

        public ImageReceiver getImageReceiver() {
            return new ImageReceiver(XReflect.getObjectField(placeProviderObject,  AutomationResolver.resolve("PhotoViewer$PlaceProviderObject", "imageReceiver", AutomationResolver.ResolverType.Field)));
        }

    }

}
