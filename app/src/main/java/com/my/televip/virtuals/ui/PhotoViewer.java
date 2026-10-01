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

    /** Calls the overload every other one delegates to, by its mapped name and arity. */
    private void callMaster(String name, int arity, Object... args) {
        for (Class<?> c = photoViewer.getClass(); c != null; c = c.getSuperclass()) {
            for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
                if (!m.getName().equals(name) || m.getParameterTypes().length != arity) continue;
                try {
                    m.setAccessible(true);
                    m.invoke(photoViewer, args);
                    return;
                } catch (Throwable t) {
                    Logger.e(t);
                    return;
                }
            }
        }
        Logger.e(new NoSuchMethodException("PhotoViewer#" + name + "/" + arity));
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
