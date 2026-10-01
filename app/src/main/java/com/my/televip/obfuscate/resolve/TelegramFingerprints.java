package com.my.televip.obfuscate.resolve;

import static com.my.televip.obfuscate.resolve.Body.*;
import static com.my.televip.obfuscate.resolve.Classes.*;
import static com.my.televip.obfuscate.resolve.Symbol.cls;
import static com.my.televip.obfuscate.resolve.Symbol.field;
import static com.my.televip.obfuscate.resolve.Symbol.method;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Everything TeleVip needs from a Telegram-based client, described so it can be found in a build
 * that renamed it.
 *
 * <p>The vocabulary - which classes, which member keys - is exactly the one the static per-client
 * tables use, so the result slots into {@code ResolverRegistry} in their place. Each symbol is
 * looked up by its real name first; the fingerprint is only consulted when the build renamed it.
 * The fingerprints describe Telegram's code, not one fork's, so they apply to any client built on
 * it; a fork only needs its own table where it has diverged.</p>
 *
 * <p>Change {@link #VERSION} whenever a fingerprint changes, so mappings cached on devices from an
 * older description are thrown away rather than trusted.</p>
 */
public final class TelegramFingerprints {

    public static final int VERSION = 8;

    private TelegramFingerprints() {
    }

    /**
     * Call sites name some overloaded methods by their plain name; this is the overload they mean,
     * as a member key.
     */
    private static final Map<String, String> METHOD_KEYS = new HashMap<>();

    static {
        METHOD_KEYS.put("Browser#openUrl", "openUrlCS");
        METHOD_KEYS.put("MessagesController#storyEntitiesAllowed2", "storyEntitiesAllowedO");
        METHOD_KEYS.put("StoriesController#hasStories2", "hasStoriesJ");
        METHOD_KEYS.put("PhotoViewer#setIsAboutToSwitchToIndex", "setIsAboutToSwitchToIndexIZZZ");
        METHOD_KEYS.put("MessagesStorage#markMessagesAsDeleted", "markMessagesAsDeletedJAZZII");
        METHOD_KEYS.put("MessagesController#deleteMessages", "deleteMessagesAAOJZIZJOIZI");
        METHOD_KEYS.put("MessageObject#getDialogId", "getDialogIdO");
        METHOD_KEYS.put("TextSettingsCell#setTextAndValue", "setTextAndValueCCZZ");
        METHOD_KEYS.put("MessagesStorage#putMessages", "putMessagesOJIIZIJ");
        METHOD_KEYS.put("MessagesController#isChatNoForwards", "isChatNoForwardsO");
        METHOD_KEYS.put("ChatActivity#updatePinnedMessageView", "updatePinnedMessageViewZI");
        METHOD_KEYS.put("PhotoViewer#openPhoto", "openPhotoOJJJOZ");
        METHOD_KEYS.put("SettingsActivity$SettingCell$Factory#of", "ofIIIICC");
        METHOD_KEYS.put("HeaderCell#setText", "setTextC");
        METHOD_KEYS.put("DispatchQueue#postRunnable", "postRunnableR");
        METHOD_KEYS.put("SQLitePreparedStatement#bindByteBuffer", "bindByteBufferIO");
        METHOD_KEYS.put("SQLitePreparedStatement#bindLong", "bindLongIJ");
        METHOD_KEYS.put("LongSparseArray#get", "getJ");
        METHOD_KEYS.put("AlertDialog$Builder#setView", "setViewV");
        METHOD_KEYS.put("FileLoader#getPathToMessage", "getPathToMessageO");
    }

    /** The member key a call site means by {@code owner#name}: the name itself unless overloaded. */
    public static String methodKey(String owner, String name) {
        String key = METHOD_KEYS.get(owner + "#" + name);
        return key == null ? name : key;
    }

    /** Owner simple names as the call sites write them, to full original names. */
    public static Map<String, String> owners() {
        Map<String, String> o = new HashMap<>();
        o.put("ActionBar", "org.telegram.ui.ActionBar.ActionBar");
        o.put("ActionBar$ActionBarMenuOnItemClick", "org.telegram.ui.ActionBar.ActionBar$ActionBarMenuOnItemClick");
        o.put("ActionBarMenuItem", "org.telegram.ui.ActionBar.ActionBarMenuItem");
        o.put("AlertDialog", "org.telegram.ui.ActionBar.AlertDialog");
        o.put("AlertDialog$Builder", "org.telegram.ui.ActionBar.AlertDialog$Builder");
        o.put("AlertDialog$OnButtonClickListener", "org.telegram.ui.ActionBar.AlertDialog$OnButtonClickListener");
        o.put("AndroidUtilities", "org.telegram.messenger.AndroidUtilities");
        o.put("ApplicationLoader", "org.telegram.messenger.ApplicationLoader");
        o.put("BaseController", "org.telegram.messenger.BaseController");
        o.put("BaseFragment", "org.telegram.ui.ActionBar.BaseFragment");
        o.put("Browser", "org.telegram.messenger.browser.Browser");
        o.put("ChatActivity", "org.telegram.ui.ChatActivity");
        o.put("ChatActivity$ChatMessageCellDelegate", "org.telegram.ui.ChatActivity$ChatMessageCellDelegate");
        o.put("ChatMessageCell", "org.telegram.ui.Cells.ChatMessageCell");
        o.put("ChatMessageCell$MessageAccessibilityNodeProvider", "org.telegram.ui.Cells.ChatMessageCell$MessageAccessibilityNodeProvider");
        o.put("ConnectionsManager", "org.telegram.tgnet.ConnectionsManager");
        o.put("DispatchQueue", "org.telegram.messenger.DispatchQueue");
        o.put("DrawerLayoutAdapter", "org.telegram.ui.Adapters.DrawerLayoutAdapter");
        o.put("DrawerLayoutContainer", "org.telegram.ui.ActionBar.DrawerLayoutContainer");
        o.put("FastDateFormat", "org.telegram.messenger.time.FastDateFormat");
        o.put("FileLoadOperation", "org.telegram.messenger.FileLoadOperation");
        o.put("FileLoader", "org.telegram.messenger.FileLoader");
        o.put("HeaderCell", "org.telegram.ui.Cells.HeaderCell");
        o.put("ImageReceiver", "org.telegram.messenger.ImageReceiver");
        o.put("LaunchActivity", "org.telegram.ui.LaunchActivity");
        o.put("LocaleController", "org.telegram.messenger.LocaleController");
        o.put("LongSparseArray", "androidx.collection.LongSparseArray");
        o.put("MessageObject", "org.telegram.messenger.MessageObject");
        o.put("MessagesController", "org.telegram.messenger.MessagesController");
        o.put("MessagesStorage", "org.telegram.messenger.MessagesStorage");
        o.put("NativeByteBuffer", "org.telegram.tgnet.NativeByteBuffer");
        o.put("NotificationCenter", "org.telegram.messenger.NotificationCenter");
        o.put("NotificationsController", "org.telegram.messenger.NotificationsController");
        o.put("PeerStoriesView$StoryItemHolder", "org.telegram.ui.Stories.PeerStoriesView$StoryItemHolder");
        o.put("PhotoViewer", "org.telegram.ui.PhotoViewer");
        o.put("PhotoViewer$PhotoViewerProvider", "org.telegram.ui.PhotoViewer$PhotoViewerProvider");
        o.put("PhotoViewer$PlaceProviderObject", "org.telegram.ui.PhotoViewer$PlaceProviderObject");
        o.put("ProfileActivity", "org.telegram.ui.ProfileActivity");
        o.put("QuickAckDelegate", "org.telegram.tgnet.QuickAckDelegate");
        o.put("RequestDelegate", "org.telegram.tgnet.RequestDelegate");
        o.put("RequestDelegateTimestamp", "org.telegram.tgnet.RequestDelegateTimestamp");
        o.put("SQLiteCursor", "org.telegram.SQLite.SQLiteCursor");
        o.put("SQLiteDatabase", "org.telegram.SQLite.SQLiteDatabase");
        o.put("SQLitePreparedStatement", "org.telegram.SQLite.SQLitePreparedStatement");
        o.put("SecretMediaViewer", "org.telegram.ui.SecretMediaViewer");
        o.put("SettingsActivity", "org.telegram.ui.SettingsActivity");
        o.put("SettingsActivity$SettingCell", "org.telegram.ui.SettingsActivity$SettingCell");
        o.put("SettingsActivity$SettingCell$Factory", "org.telegram.ui.SettingsActivity$SettingCell$Factory");
        o.put("ShadowSectionCell", "org.telegram.ui.Cells.ShadowSectionCell");
        o.put("SharedConfig", "org.telegram.messenger.SharedConfig");
        o.put("SimpleTextView", "org.telegram.ui.ActionBar.SimpleTextView");
        o.put("StoriesController", "org.telegram.ui.Stories.StoriesController");
        o.put("TLObject", "org.telegram.tgnet.TLObject");
        o.put("TLRPC$Chat", "org.telegram.tgnet.TLRPC$Chat");
        o.put("TLRPC$EncryptedChat", "org.telegram.tgnet.TLRPC$EncryptedChat");
        o.put("TLRPC$InputPeer", "org.telegram.tgnet.TLRPC$InputPeer");
        o.put("TLRPC$Message", "org.telegram.tgnet.TLRPC$Message");
        o.put("TLRPC$Peer", "org.telegram.tgnet.TLRPC$Peer");
        o.put("TLRPC$TL_channels_readHistory", "org.telegram.tgnet.TLRPC$TL_channels_readHistory");
        o.put("TLRPC$TL_channels_readMessageContents", "org.telegram.tgnet.TLRPC$TL_channels_readMessageContents");
        o.put("TLRPC$TL_inputPeerChannel", "org.telegram.tgnet.TLRPC$TL_inputPeerChannel");
        o.put("TLRPC$TL_messages_affectedMessages", "org.telegram.tgnet.TLRPC$TL_messages_affectedMessages");
        o.put("TLRPC$TL_messages_readDiscussion", "org.telegram.tgnet.TLRPC$TL_messages_readDiscussion");
        o.put("TLRPC$TL_messages_readEncryptedHistory", "org.telegram.tgnet.TLRPC$TL_messages_readEncryptedHistory");
        o.put("TLRPC$TL_messages_readHistory", "org.telegram.tgnet.TLRPC$TL_messages_readHistory");
        o.put("TLRPC$TL_messages_readMessageContents", "org.telegram.tgnet.TLRPC$TL_messages_readMessageContents");
        o.put("TLRPC$TL_messages_sendMedia", "org.telegram.tgnet.TLRPC$TL_messages_sendMedia");
        o.put("TLRPC$TL_messages_sendMessage", "org.telegram.tgnet.TLRPC$TL_messages_sendMessage");
        o.put("TLRPC$TL_messages_sendMultiMedia", "org.telegram.tgnet.TLRPC$TL_messages_sendMultiMedia");
        o.put("TLRPC$TL_messages_sendPaidReaction", "org.telegram.tgnet.TLRPC$TL_messages_sendPaidReaction");
        o.put("TLRPC$TL_messages_sendReaction", "org.telegram.tgnet.TLRPC$TL_messages_sendReaction");
        o.put("TLRPC$TL_messages_setEncryptedTyping", "org.telegram.tgnet.TLRPC$TL_messages_setEncryptedTyping");
        o.put("TLRPC$TL_messages_setTyping", "org.telegram.tgnet.TLRPC$TL_messages_setTyping");
        o.put("TLRPC$User", "org.telegram.tgnet.TLRPC$User");
        o.put("TLRPC$messages_Messages", "org.telegram.tgnet.TLRPC$messages_Messages");
        o.put("TL_account$updateStatus", "org.telegram.tgnet.tl.TL_account$updateStatus");
        o.put("TL_stories$TL_stories_incrementStoryViews", "org.telegram.tgnet.tl.TL_stories$TL_stories_incrementStoryViews");
        o.put("TL_stories$TL_stories_readStories", "org.telegram.tgnet.tl.TL_stories$TL_stories_readStories");
        o.put("TL_update$TL_updateDeleteChannelMessages", "org.telegram.tgnet.tl.TL_update$TL_updateDeleteChannelMessages");
        o.put("TL_update$TL_updateDeleteMessages", "org.telegram.tgnet.tl.TL_update$TL_updateDeleteMessages");
        o.put("TextCheckCell", "org.telegram.ui.Cells.TextCheckCell");
        o.put("TextSettingsCell", "org.telegram.ui.Cells.TextSettingsCell");
        o.put("Theme", "org.telegram.ui.ActionBar.Theme");
        o.put("Theme$ResourcesProvider", "org.telegram.ui.ActionBar.Theme$ResourcesProvider");
        o.put("UItem", "org.telegram.ui.Components.UItem");
        o.put("UItem$UItemFactory", "org.telegram.ui.Components.UItem$UItemFactory");
        o.put("UniversalAdapter", "org.telegram.ui.Components.UniversalAdapter");
        o.put("UserConfig", "org.telegram.messenger.UserConfig");
        o.put("Utilities", "org.telegram.messenger.Utilities");
        o.put("WriteToSocketDelegate", "org.telegram.tgnet.WriteToSocketDelegate");
        return Collections.unmodifiableMap(o);
    }

    public static List<Symbol> all() {
        List<Symbol> s = new ArrayList<>();
        messenger(s);
        keptInRecentBuilds(s);
        classes(s);
        settings(s);
        actionBar(s);
        alertDialog(s);
        cells(s);
        chat(s);
        profile(s);
        photoViewer(s);
        stories(s);
        misc(s);
        return s;
    }

    /**
     * Core classes official Telegram keeps but some forks (Cherrygram) rename, found by strings
     * only they load.
     */
    private static void messenger(List<Symbol> s) {
        s.add(cls("org.telegram.messenger.AndroidUtilities")
                .from(declaringStrings("Could not get typeface '", "sms listener registered")));
        s.add(cls("org.telegram.messenger.ApplicationLoader")
                .from(declaringStrings("app initied", "screen state = ")));
        s.add(cls("org.telegram.messenger.FileLoadOperation")
                .from(declaringStrings("unable to rename temp = ", "setIsPreloadVideoOperation ")));
        s.add(cls("org.telegram.messenger.FileLoader")
                .from(declaringStrings("create load operation fileName=", "fileUploadQueue")));
        s.add(cls("org.telegram.messenger.LocaleController")
                .from(declaringStrings("LOC_ERR: formatDateChat", "formatterBannedUntil24H")));
        s.add(cls("org.telegram.messenger.MessageObject")
                .from(declaringStrings("EventLogEditedGroupTitle", "ActionUserScoredInGame")));
        s.add(cls("org.telegram.messenger.MessagesController")
                .from(declaringStrings("inapp_update_check_delay", "saved_gifs_limit_default")));
        s.add(cls("org.telegram.messenger.MessagesStorage")
                .from(declaringStrings("Try create new database = ", "DELETE FROM stickers_v2")));
        s.add(cls("org.telegram.messenger.NotificationCenter")
                .from(declaringStrings("addObserver allowed only from MAIN thread", "postNotificationName allowed only from MAIN thread")));
        s.add(cls("org.telegram.messenger.NotificationsController")
                .from(declaringStrings("showExtraNotifications: [", "resetNotificationSound")));
        s.add(cls("org.telegram.messenger.SharedConfig")
                .from(declaringStrings("devicePerformanceClass", "forceDisableTabletMode")));
        s.add(cls("org.telegram.messenger.UserConfig")
                .from(declaringStrings("2dialogsLoadOffsetChatId", "sharingMyLocationUntil")));
        s.add(cls("org.telegram.messenger.time.FastDateFormat")
                .from(declaringStrings("FastDateFormat[")));
        s.add(cls("org.telegram.tgnet.TLRPC$Message")
                .from(declaringStrings("legacy_layer", "poll_with_media=")));
    }

    /**
     * Symbols official Telegram keeps the real names of. Forks that rename them (Cherrygram) are
     * resolved by the fingerprints; the few still without one show up as missing in the hook
     * health report on such a fork rather than being guessed at.
     */
    private static void keptInRecentBuilds(List<Symbol> s) {
        // SQLite's natives keep their names in every build, so the wrappers are known by them.
        String cursor = "org.telegram.SQLite.SQLiteCursor", statement = "org.telegram.SQLite.SQLitePreparedStatement";
        // ConnectionsManager's natives call back into it by name, so its members keep theirs too.
        String connections = "org.telegram.tgnet.ConnectionsManager";
        // A TL object writes its own constructor id; its abstract parent only compares against it.
        Symbol.ClassFact serializes = hasMethod(false, "void", "org.telegram.tgnet.OutputSerializedData");
        String msg = "org.telegram.tgnet.TLRPC$Message", document = "org.telegram.tgnet.TLRPC$Document";
        // Utilities' queues are its only Thread-typed fields.
        s.add(cls("org.telegram.messenger.DispatchQueue")
                .from(fieldTypesOf("org.telegram.messenger.Utilities"))
                .where(extendsType("java.lang.Thread")));
        s.add(cls("org.telegram.tgnet.QuickAckDelegate").from(paramTypeOf(connections, "sendRequestInternal", 3))
                .where(isInterface()));
        s.add(cls("org.telegram.tgnet.RequestDelegateTimestamp")
                .from(paramTypeOf(connections, "sendRequestInternal", 2)).where(isInterface()));
        s.add(cls("org.telegram.tgnet.TLObject")
                .from(superclassOf("org.telegram.tgnet.TLRPC$TL_messages_readHistory")));
        s.add(cls("org.telegram.tgnet.TLRPC$Chat").from(superclassOf("org.telegram.tgnet.TLRPC$TL_chat")));
        s.add(cls("org.telegram.tgnet.TLRPC$EncryptedChat")
                .from(superclassOf("org.telegram.tgnet.TLRPC$TL_encryptedChat")));
        s.add(cls("org.telegram.tgnet.TLRPC$InputPeer")
                .from(superclassOf("org.telegram.tgnet.TLRPC$TL_inputPeerChannel")));
        s.add(cls("org.telegram.tgnet.TLRPC$Peer").from(superclassOf("org.telegram.tgnet.TLRPC$TL_peerUser")));
        s.add(cls("org.telegram.tgnet.TLRPC$TL_channels_readHistory"));
        s.add(cls("org.telegram.tgnet.TLRPC$TL_channels_readMessageContents"));
        s.add(cls("org.telegram.tgnet.TLRPC$TL_inputPeerChannel"));
        s.add(cls("org.telegram.tgnet.TLRPC$TL_messages_affectedMessages"));
        s.add(cls("org.telegram.tgnet.TLRPC$TL_messages_readDiscussion"));
        s.add(cls("org.telegram.tgnet.TLRPC$TL_messages_readEncryptedHistory"));
        s.add(cls("org.telegram.tgnet.TLRPC$TL_messages_readHistory"));
        s.add(cls("org.telegram.tgnet.TLRPC$TL_messages_readMessageContents"));
        s.add(cls("org.telegram.tgnet.TLRPC$TL_messages_sendMedia"));
        s.add(cls("org.telegram.tgnet.TLRPC$TL_messages_sendMessage"));
        s.add(cls("org.telegram.tgnet.TLRPC$TL_messages_sendMultiMedia"));
        s.add(cls("org.telegram.tgnet.TLRPC$TL_messages_sendPaidReaction"));
        s.add(cls("org.telegram.tgnet.TLRPC$TL_messages_sendReaction"));
        s.add(cls("org.telegram.tgnet.TLRPC$TL_messages_setEncryptedTyping"));
        s.add(cls("org.telegram.tgnet.TLRPC$TL_messages_setTyping"));
        s.add(cls("org.telegram.tgnet.TLRPC$User").from(superclassOf("org.telegram.tgnet.TLRPC$TL_user")));
        s.add(cls("org.telegram.tgnet.TLRPC$messages_Messages")
                .from(superclassOf("org.telegram.tgnet.TLRPC$TL_messages_messages")));
        s.add(cls("org.telegram.tgnet.TLRPC$InputChannel")
                .from(superclassOf("org.telegram.tgnet.TLRPC$TL_inputChannel")));
        s.add(cls("org.telegram.tgnet.OutputSerializedData")
                .from(paramTypeWhere("org.telegram.tgnet.TLObject", "void", (String) null)).where(isInterface()));
        s.add(cls("org.telegram.tgnet.TLRPC$Dialog").from(superclassOf("org.telegram.tgnet.TLRPC$TL_dialog")));
        s.add(cls("org.telegram.tgnet.TLRPC$Document").from(superclassOf("org.telegram.tgnet.TLRPC$TL_document")));
        s.add(cls("org.telegram.tgnet.TLRPC$DocumentAttribute")
                .from(superclassOf("org.telegram.tgnet.TLRPC$TL_documentAttributeAudio")));
        s.add(cls("org.telegram.tgnet.InputSerializedData")
                .from(paramTypeWhere("org.telegram.tgnet.TLRPC$Message", "org.telegram.tgnet.TLRPC$Message",
                        null, "int", "boolean")));
        s.add(cls("org.telegram.tgnet.WriteToSocketDelegate").from(paramTypeOf(connections, "sendRequestInternal", 4))
                .where(isInterface()));
        s.add(cls("org.telegram.tgnet.tl.TL_account$updateStatus").from(declaringConstant(1713919532))
                .where(serializes));
        // The requests that fetch ads: messages.getSponsoredMessages, contacts.getSponsoredPeers and
        // help.getPromoData.
        s.add(cls("org.telegram.tgnet.TLRPC$TL_messages_getSponsoredMessages").from(declaringConstant(0x3d6ce850))
                .where(serializes));
        s.add(cls("org.telegram.tgnet.TLRPC$TL_contacts_getSponsoredPeers").from(declaringConstant(0xb6c8c393))
                .where(serializes));
        s.add(cls("org.telegram.tgnet.TLRPC$TL_help_getPromoData").from(declaringConstant(0xc0977421))
                .where(serializes));
        s.add(cls("org.telegram.tgnet.tl.TL_stories$TL_stories_incrementStoryViews")
                .from(declaringConstant(-1308456197)).where(serializes));
        s.add(cls("org.telegram.tgnet.tl.TL_stories$TL_stories_readStories").from(declaringConstant(-1521034552))
                .where(serializes));
        s.add(cls("org.telegram.tgnet.tl.TL_update$TL_updateDeleteChannelMessages")
                .from(declaringConstant(-1020437742)).where(serializes));
        s.add(cls("org.telegram.tgnet.tl.TL_update$TL_updateDeleteMessages").from(declaringConstant(-1576161051))
                .where(serializes));
        s.add(field("ApplicationLoader", "applicationContext").isStatic(true)
                .type("android.content.Context").onlyOneOfType());
        s.add(field("FileLoadOperation", "downloadChunkSizeBig").type("int")
                .writtenBy("FileLoadOperation#updateParams", 0));
        s.add(field("FileLoadOperation", "maxCdnParts").type("int").writtenBy("FileLoadOperation#updateParams", 3));
        s.add(field("FileLoadOperation", "maxDownloadRequests").type("int")
                .writtenBy("FileLoadOperation#updateParams", 1));
        s.add(field("FileLoadOperation", "maxDownloadRequestsBig").type("int")
                .writtenBy("FileLoadOperation#updateParams", 2));
        s.add(field("LocaleController", "currentLocale"));
        s.add(field("LocaleController", "isRTL").keyedBy("LocaleController#recreateFormatters", "iw_"));
        s.add(field("MessagesController", "dialogMessagesByIds"));
        s.add(field("NotificationCenter", "messagesDeleted"));
        s.add(field("NotificationCenter", "tlSchemeParseException"));
        s.add(field("UserConfig", "clientUserId").type("long").writtenBy("UserConfig#setCurrentUser", 0));
        s.add(field("UserConfig", "selectedAccount").keyedBy("UserConfig#loadConfig", "selectedAccount"));
        s.add(field("Utilities", "stageQueue").keyedBy("Utilities#<clinit>", "stageQueue"));
        s.add(field("TLRPC$InputPeer", "channel_id"));
        s.add(field("TLRPC$InputPeer", "chat_id"));
        s.add(field("TLRPC$InputPeer", "user_id"));
        s.add(field("TLRPC$Message", "flags"));
        s.add(field("TLRPC$Message", "from_id"));
        s.add(field("TLRPC$Message", "id"));
        s.add(field("TLRPC$Message", "message"));
        s.add(field("TLRPC$Message", "ttl"));
        s.add(field("TLRPC$Peer", "channel_id"));
        s.add(field("TLRPC$Peer", "chat_id"));
        s.add(field("TLRPC$Peer", "user_id"));
        s.add(field("TLRPC$TL_channels_readHistory", "channel"));
        s.add(field("TLRPC$TL_channels_readHistory", "max_id"));
        s.add(field("TLRPC$TL_messages_affectedMessages", "pts"));
        s.add(field("TLRPC$TL_messages_affectedMessages", "pts_count"));
        s.add(field("TLRPC$TL_messages_readHistory", "max_id"));
        s.add(field("TLRPC$TL_messages_readHistory", "peer"));
        s.add(field("TLRPC$TL_messages_readDiscussion", "peer"));
        s.add(field("TLRPC$TL_messages_sendMedia", "peer"));
        s.add(field("TLRPC$TL_messages_sendMessage", "peer"));
        s.add(field("TLRPC$TL_messages_sendMultiMedia", "peer"));
        s.add(field("TLRPC$TL_messages_sendPaidReaction", "peer"));
        s.add(field("TLRPC$TL_messages_sendReaction", "peer"));
        s.add(field("TLRPC$User", "phone"));
        s.add(field("TLRPC$messages_Messages", "messages"));
        s.add(field("TL_account$updateStatus", "offline").type("boolean").onlyOneOfType());
        s.add(field("TL_update$TL_updateDeleteChannelMessages", "channel_id").type("long").onlyOneOfType());
        s.add(field("TL_update$TL_updateDeleteChannelMessages", "messages")
                .type("java.util.ArrayList").onlyOneOfType());
        s.add(field("TL_update$TL_updateDeleteMessages", "messages").type("java.util.ArrayList").onlyOneOfType());
        s.add(field("MessagesController", "dialogMessage"));
        s.add(method("SQLiteCursor", "byteBufferValue").sig("org.telegram.tgnet.NativeByteBuffer", "int"));
        s.add(method("SQLiteCursor", "dispose").sig("void").where(callsSymbol("SQLitePreparedStatement#dispose")));
        s.add(method("SQLiteCursor", "intValue").sig("int", "int").where(callsNamed(cursor, "columnIntValue")));
        s.add(method("SQLiteCursor", "longValue").sig("long", "int").where(callsNamed(cursor, "columnLongValue")));
        s.add(method("SQLiteCursor", "next").sig("boolean").where(string("sqlite busy")));
        s.add(method("SQLiteDatabase", "executeFast").sig(statement, "java.lang.String"));
        s.add(method("SQLiteDatabase", "queryFinalized").sig(cursor, "java.lang.String", "java.lang.Object[]"));
        s.add(method("SQLitePreparedStatement", "bindByteBufferIB").named("bindByteBuffer")
                .sig("void", "int", "java.nio.ByteBuffer").where(callsNamed(statement, "bindByteBuffer")));
        s.add(method("SQLitePreparedStatement", "bindByteBufferIO").named("bindByteBuffer")
                .sig("void", "int", "org.telegram.tgnet.NativeByteBuffer").where(callsNamed(statement, "bindByteBuffer")));
        s.add(method("SQLitePreparedStatement", "bindByteBufferJIBI").named("bindByteBuffer"));
        s.add(method("SQLitePreparedStatement", "bindInteger").sig("void", "int", "int")
                .where(callsNamed(statement, "bindInt")));
        s.add(method("SQLitePreparedStatement", "bindLongIJ").named("bindLong")
                .sig("void", "int", "long").where(callsNamed(statement, "bindLong")));
        s.add(method("SQLitePreparedStatement", "bindLongJIJ").named("bindLong"));
        s.add(method("SQLitePreparedStatement", "finalizeQuery").sig("void").where(string("sqlite query ")));
        s.add(method("SQLitePreparedStatement", "dispose").sig("void")
                .where(callsSymbol("SQLitePreparedStatement#finalizeQuery"), callCount(1)));
        s.add(method("SQLitePreparedStatement", "requery").sig("void").where(callsNamed(statement, "reset")));
        s.add(method("SQLitePreparedStatement", "step").sig("int").where(callsNamed(statement, "step")));
        s.add(method("SQLitePreparedStatement", "stepJ").named("step"));
        s.add(method("AndroidUtilities", "isTabletInternal").sig("boolean")
                .where(writesFieldOfType("org.telegram.messenger.AndroidUtilities", "java.lang.Boolean"), callsSibling("boolean")));
        s.add(method("DispatchQueue", "postRunnableR").named("postRunnable").sig("boolean", "java.lang.Runnable")
                .where(callsSibling("boolean", "java.lang.Runnable", "long")));
        s.add(method("DispatchQueue", "postRunnableRJ").named("postRunnable")
                .sig("boolean", "java.lang.Runnable", "long"));
        s.add(method("FileLoadOperation", "updateParams").sig("void")
                .where(constant(2097152000)));
        s.add(method("FileLoader", "getInstance").sig("org.telegram.messenger.FileLoader", "int"));
        s.add(method("FileLoader", "getPathToMessageO").named("getPathToMessage")
                .sig("java.io.File", "org.telegram.tgnet.TLRPC$Message"));
        s.add(method("FileLoader", "getPathToMessageOZ").named("getPathToMessage")
                .sig("java.io.File", "org.telegram.tgnet.TLRPC$Message", "boolean"));
        s.add(method("FileLoader", "getPathToMessageOZZ").named("getPathToMessage")
                .sig("java.io.File", "org.telegram.tgnet.TLRPC$Message", "boolean", "boolean"));
        s.add(method("ImageReceiver", "getImageLocation").sig("org.telegram.messenger.ImageLocation")
                .where(touchesField("org.telegram.messenger.ImageReceiver", "currentImageLocation"), callCount(0)));
        s.add(method("LocaleController", "formatShortNumber").sig("java.lang.String", "int", "int[]"));
        s.add(method("LocaleController", "formatYearMont").sig("java.lang.String", "long", "boolean")
                .where(string("LOC_ERR"), string(" "), not(constant(5))));
        s.add(method("LocaleController", "getInstance").sig("org.telegram.messenger.LocaleController"));
        s.add(method("MessageObject", "canForwardMessage").sig("boolean")
                .where(refersTo("org.telegram.tgnet.TLRPC$TL_message_secret"), touchesField("org.telegram.tgnet.TLRPC$Message", "noforwards")));
        s.add(method("MessageObject", "getDialogId").sig("long")
                .where(callsSymbol("MessageObject#getDialogIdO"), callCount(1)));
        s.add(method("MessageObject", "getDialogIdO").named("getDialogId")
                .sig("long", "org.telegram.tgnet.TLRPC$Message")
                .where(callsSibling("boolean", "org.telegram.tgnet.TLRPC$Message")));
        s.add(method("MessageObject", "isMusic").sig("boolean")
                .where(callsSymbol("MessageObject#isMusicMessage")));
        s.add(method("MessageObject", "isSecret").sig("boolean")
                .where(refersTo("org.telegram.tgnet.TLRPC$TL_message_secret"), callCount(0)));
        s.add(method("MessageObject", "isVoice").sig("boolean")
                .where(callsSymbol("MessageObject#isVoiceMessage"), callCount(1)));
        s.add(method("MessagesController", "checkPromoInfoInternal").sig("void", "boolean")
                .where(string("proxy_enabled")));
        s.add(method("MessagesController", "deleteMessagesAAOJIZI").named("deleteMessages")
                .sig("void", "java.util.ArrayList", "java.util.ArrayList", "org.telegram.tgnet.TLRPC$EncryptedChat", "long", "int", "boolean", "int"));
        s.add(method("MessagesController", "deleteMessagesAAOJIZIZ").named("deleteMessages")
                .sig("void", "java.util.ArrayList", "java.util.ArrayList", "org.telegram.tgnet.TLRPC$EncryptedChat", "long", "int", "boolean", "int", "boolean"));
        s.add(method("MessagesController", "deleteMessagesAAOJZIZJOI").named("deleteMessages")
                .sig("void", "java.util.ArrayList", "java.util.ArrayList", "org.telegram.tgnet.TLRPC$EncryptedChat", "long", "boolean", "int", "boolean", "long", "org.telegram.tgnet.TLObject", "int"));
        s.add(method("MessagesController", "deleteMessagesAAOJZIZJOIZI").named("deleteMessages")
                .sig("void", "java.util.ArrayList", "java.util.ArrayList", "org.telegram.tgnet.TLRPC$EncryptedChat", "long", "boolean", "int", "boolean", "long", "org.telegram.tgnet.TLObject", "int", "boolean", "int"));
        // The constructor opens the Notifications, mainconfig and emoji preferences, in that order.
        s.add(method("MessagesController", "<init>").sig("void", "int"));
        s.add(field("MessagesController", "notificationsPreferences").type("android.content.SharedPreferences")
                .writtenBy("MessagesController#<init>", 0));
        s.add(field("MessagesController", "mainPreferences").type("android.content.SharedPreferences")
                .writtenBy("MessagesController#<init>", 1));
        s.add(method("MessagesController", "getGlobalMainSettings").isStatic(true)
                .sig("android.content.SharedPreferences")
                .where(touchesFieldSymbol("MessagesController.mainPreferences")));
        s.add(method("MessagesController", "getNotificationsSettings").sig("android.content.SharedPreferences", "int")
                .where(touchesFieldSymbol("MessagesController.notificationsPreferences")));
        // Queues the "delete after viewing" task: a 16-byte buffer tagged 102.
        s.add(method("MessagesController", "createDeleteShowOnceTask").sig("long", "long", "int").where(constant(102)));
        s.add(method("MessagesController", "markMessageAsRead2")
                .sig("void", "long", "int", "org.telegram.tgnet.TLRPC$InputChannel", "int", "long", "boolean"));
        s.add(method("MessagesController", "getInputChannelJ").named("getInputChannel")
                .sig("org.telegram.tgnet.TLRPC$InputChannel", "long"));
        s.add(method("MessagesController", "getInputChannelO").named("getInputChannel")
                .sig("org.telegram.tgnet.TLRPC$InputChannel", "org.telegram.tgnet.TLRPC$Chat"));
        s.add(method("MessagesController", "getInputChannelO2").named("getInputChannel")
                .sig("org.telegram.tgnet.TLRPC$InputChannel", "org.telegram.tgnet.TLRPC$InputPeer"));
        s.add(method("MessagesController", "getInstance").sig("org.telegram.messenger.MessagesController", "int"));
        s.add(method("MessagesController", "isChatNoForwardsJ").named("isChatNoForwards").sig("boolean", "long")
                .where(callsSymbol("MessagesController#isChatNoForwardsO")));
        s.add(method("MessagesController", "isChatNoForwardsO").named("isChatNoForwards")
                .sig("boolean", "org.telegram.tgnet.TLRPC$Chat"));
        s.add(method("MessagesController", "processNewDifferenceParams").sig("void", "int", "int", "int", "int")
                .where(string(" pts_count = ")));
        s.add(method("MessagesController", "removePromoDialog").sig("void")
                .where(notSynthetic(), touchesField("org.telegram.tgnet.TLRPC$Chat", "restricted"),
                        writesFieldOfType("org.telegram.messenger.MessagesController", "org.telegram.tgnet.TLRPC$Dialog"),
                        callsSibling("void", "org.telegram.tgnet.TLRPC$Dialog")));
        s.add(method("MessagesController", "storiesEnabled").sig("boolean")
                .where(touchesFieldSymbol("MessagesController.storiesPosting")));
        s.add(method("MessagesController", "storyEntitiesAllowed").sig("boolean")
                .where(touchesFieldSymbol("MessagesController.storiesEntities")));
        s.add(method("MessagesController", "storyEntitiesAllowedO").named("storyEntitiesAllowed")
                .sig("boolean", "org.telegram.tgnet.TLRPC$User")
                .where(string("premium"), touchesField("org.telegram.tgnet.TLRPC$User", "premium")));
        s.add(method("MessagesStorage", "getDatabase").sig("org.telegram.SQLite.SQLiteDatabase"));
        s.add(method("MessagesStorage", "getInstance").sig("org.telegram.messenger.MessagesStorage", "int"));
        s.add(method("MessagesStorage", "getStorageQueue").sig("org.telegram.messenger.DispatchQueue"));
        s.add(method("MessagesStorage", "markMessagesAsDeletedJAZZII").named("markMessagesAsDeleted")
                .sig("java.util.ArrayList", "long", "java.util.ArrayList", "boolean", "boolean", "int", "int"));
        s.add(method("MessagesStorage", "markMessagesAsDeletedJIZZ").named("markMessagesAsDeleted")
                .sig("java.util.ArrayList", "long", "int", "boolean", "boolean"));
        s.add(method("MessagesStorage", "putMessagesAZZZIIJ").named("putMessages")
                .sig("void", "java.util.ArrayList", "boolean", "boolean", "boolean", "int", "int", "long"));
        s.add(method("MessagesStorage", "putMessagesAZZZIZIJ").named("putMessages")
                .sig("void", "java.util.ArrayList", "boolean", "boolean", "boolean", "int", "boolean", "int", "long"));
        s.add(method("MessagesStorage", "putMessagesOJIIZIJ").named("putMessages")
                .sig("void", "org.telegram.tgnet.TLRPC$messages_Messages", "long", "int", "int", "boolean", "int", "long"));
        s.add(method("NotificationCenter", "postNotificationName").sig("void", "int", "java.lang.Object[]")
                .where(callsSibling("void", "int", "boolean", "java.lang.Object[]")));
        s.add(method("NotificationsController", "removeDeletedMessagesFromNotifications")
                .sig("void", "androidx.collection.LongSparseArray", "boolean"));
        s.add(method("SharedConfig", "isAppUpdateAvailable").sig("boolean")
                .where(calls("android.content.pm.PackageManager", "android.content.pm.PackageInfo", "java.lang.String", "int")));
        s.add(method("SharedConfig", "setNewAppVersionAvailable")
                .sig("boolean", "org.telegram.tgnet.TLRPC$TL_help_appUpdate"));
        s.add(method("UserConfig", "getClientUserId").sig("long")
                .where(touchesFieldSymbol("UserConfig.currentUser"), callCount(0)));
        s.add(method("UserConfig", "getCurrentUser").sig("org.telegram.tgnet.TLRPC$User"));
        s.add(method("UserConfig", "isPremium").sig("boolean")
                .where(touchesFieldSymbol("UserConfig.currentUser"), touchesField("org.telegram.tgnet.TLRPC$User", "premium")));
        s.add(method("FastDateFormat", "formatD").named("format").sig("java.lang.String", "java.util.Date"));
        s.add(method("FastDateFormat", "formatJ").named("format").sig("java.lang.String", "long"));
        s.add(method("FastDateFormat", "formatOSF").named("format")
                .sig("java.lang.StringBuffer", "java.lang.Object", "java.lang.StringBuffer", "java.text.FieldPosition"));
        s.add(method("TLRPC$Message", "TLdeserialize")
                .sig("org.telegram.tgnet.TLRPC$Message", "org.telegram.tgnet.InputSerializedData", "int", "boolean"));
        s.add(method("TLRPC$Message", "readAttachPath").sig("void", "org.telegram.tgnet.InputSerializedData", "long"));
        s.add(field("UserConfig", "currentUser").type("org.telegram.tgnet.TLRPC$User").onlyOneOfType());
        s.add(method("UserConfig", "setCurrentUser").sig("void", "org.telegram.tgnet.TLRPC$User")
                .where(callsSibling("void", "org.telegram.tgnet.TLRPC$User", "org.telegram.tgnet.TLRPC$User")));
        s.add(method("UserConfig", "loadConfig").sig("void")
                .where(string("selectedAccount"), string("2dialogsLoadOffsetId")));
        s.add(method("Utilities", "<clinit>").sig("void"));
        s.add(method("LocaleController", "recreateFormatters").sig("void").where(string("iw_")));
        s.add(field("MessagesController", "storiesPosting").keyedBy("MessagesController#<init>", "storiesPosting"));
        s.add(field("MessagesController", "storiesEntities").keyedBy("MessagesController#<init>", "storiesEntities"));
        s.add(method("MessageObject", "isVoiceDocument").sig("boolean", document)
                .where(touchesField("org.telegram.tgnet.TLRPC$DocumentAttribute", "voice"), callCount(2)));
        s.add(method("MessageObject", "isMusicDocument").sig("boolean", document).where(string("audio/flac")));
        s.add(method("MessageObject", "isVoiceMessage").sig("boolean", msg)
                .where(callsSymbol("MessageObject#isVoiceDocument")));
        s.add(method("MessageObject", "isMusicMessage").sig("boolean", msg)
                .where(callsSymbol("MessageObject#isMusicDocument")));
        s.add(method("FileLoader", "getLocalFile").sig("java.io.File", "org.telegram.messenger.ImageLocation"));
        s.add(cls("org.telegram.messenger.BaseController").from(superclassOf("org.telegram.messenger.MessagesController")));
        s.add(method("BaseController", "getUserConfig").sig("org.telegram.messenger.UserConfig"));
        s.add(method("BaseFragment", "getUserConfig").sig("org.telegram.messenger.UserConfig"));
        s.add(method("ChatActivity", "createView"));
        s.add(method("ChatActivity", "isSwipeBackEnabled"));
        s.add(method("ProfileActivity", "isSwipeBackEnabled"));
    }

    // ------------------------------------------------------------------ classes

    private static void classes(List<Symbol> s) {
        // Official Telegram builds rename these too; each loads strings no other class does.
        s.add(cls("org.telegram.ui.ChatActivity")
                .from(declaringStrings("WelcomeMessagesLimit", "processLoadedDiscussionMessage reset history",
                        "AwaitingEncryption")));
        s.add(cls("org.telegram.ui.SettingsActivity")
                .from(declaringStrings("disable shadows in settings", "enable debug view metrics")));

        // Helpers: not used by call sites directly, but other fingerprints anchor on them.
        s.add(cls("org.telegram.ui.ActionBar.BaseFragment")
                .from(superclassOf("org.telegram.ui.ChatActivity")));
        s.add(cls("org.telegram.ui.ActionBar.Theme$ResourcesProvider")
                .from(fieldTypeOf("org.telegram.ui.ActionBar.BaseFragment", "resourceProvider"))
                .where(isInterface()));
        s.add(cls("org.telegram.ui.ActionBar.AlertDialog")
                .from(fieldTypesOf("org.telegram.ui.ActionBar.AlertDialog$Builder"))
                .where(extendsType("android.app.Dialog")));

        s.add(cls("androidx.collection.LongSparseArray")
                .from(fieldTypeOf("org.telegram.messenger.MessagesController", "dialogMessage"),
                        fieldTypesOf("org.telegram.messenger.MessagesController"))
                .where(implementsType("java.lang.Cloneable"), hasField(false, "long[]"),
                        hasField(false, "java.lang.Object[]")));
        s.add(cls("org.telegram.messenger.browser.Browser")
                .from(declaringStrings("com.duckduckgo.mobile.android", "vivaldi-browser")));
        s.add(cls("org.telegram.ui.ActionBar.Theme")
                .from(declaringStrings("autoNightScheduleByLocation", "autoNightLastSunCheckDay")));
        s.add(cls("org.telegram.ui.Stories.StoriesController")
                .from(returnTypeOf("org.telegram.messenger.MessagesController", "getStoriesController"),
                        declaringStrings("stories_stealth_mode", "last_stories_state_hidden")));
    }

    // ----------------------------------------------------------------- settings

    private static void settings(List<Symbol> s) {
        String uitem = "org.telegram.ui.Components.UItem";
        // UItem's API is a large set of static factories (asHeader, asCheck, asButton, ...);
        // nothing else a settings screen touches looks like that.
        s.add(cls(uitem)
                .from(paramTypesOf("org.telegram.ui.SettingsActivity"))
                .where(staticFactories(15)));
        s.add(cls("org.telegram.ui.SettingsActivity$SettingCell$Factory")
                .from(declaringMethod(uitem, "int", "int", "int", "int", "java.lang.CharSequence", "java.lang.CharSequence", "java.lang.CharSequence")));
        s.add(cls("org.telegram.ui.Components.UItem$UItemFactory")
                .from(superclassOf("org.telegram.ui.SettingsActivity$SettingCell$Factory")));
        s.add(method("SettingsActivity$SettingCell$Factory", "createView"));
        s.add(method("SettingsActivity$SettingCell$Factory", "bindView"));
        s.add(cls("org.telegram.ui.SettingsActivity$SettingCell")
                .from(instantiatedBySymbol("SettingsActivity$SettingCell$Factory#createView"))
                .where(extendsType("android.widget.LinearLayout"), hasField(false, "android.widget.ImageView")));
        s.add(cls("org.telegram.ui.Components.UniversalAdapter")
                .from(paramTypeWhere("org.telegram.ui.SettingsActivity", "void", "java.util.ArrayList", null),
                        paramTypesOf("org.telegram.ui.SettingsActivity$SettingCell$Factory"))
                .where(hasMethod(false, uitem, "int"),
                        isNot(inherits("androidx.recyclerview.widget.RecyclerView"))));

        // Telegram 12.10.5 has both static, taking the fragment first and without the parameters
        // they never read: fillItems(activity, list) and onClick(activity, item). The hooks find
        // the list and the item by type.
        s.add(method("SettingsActivity", "fillItems")
                .sig("void", "java.util.ArrayList", "org.telegram.ui.Components.UniversalAdapter").reads(0)
                .staticized().where(calls(uitem, uitem, "int")));
        s.add(method("SettingsActivity", "onClick")
                .sig("void", uitem, "android.view.View", "int", "float", "float").reads(0).staticized());
        s.add(method("SettingsActivity$SettingCell", "set")
                .sig("void", "int", "int", "int", "java.lang.CharSequence", "java.lang.CharSequence", "java.lang.CharSequence"));
        // Factory.of assigns item.id, iconResId, text, subtext, textValue in that order.
        String of7 = "SettingsActivity$SettingCell$Factory#ofIIIICCC";
        s.add(field("UItem", "id").type("int").writtenBy(of7, 0));
        s.add(field("UItem", "text").type("java.lang.CharSequence").writtenBy(of7, 0));
        s.add(field("UItem", "subtext").type("java.lang.CharSequence").writtenBy(of7, 1));
        s.add(field("SettingsActivity$SettingCell", "iconView")
                .type("android.widget.ImageView").onlyOneOfType());
        s.add(method("SettingsActivity$SettingCell$Factory", "ofIIIIC").named("of").isStatic(true)
                .sig(uitem, "int", "int", "int", "int", "java.lang.CharSequence"));
        s.add(method("SettingsActivity$SettingCell$Factory", "ofIIIICC").named("of").isStatic(true)
                .sig(uitem, "int", "int", "int", "int", "java.lang.CharSequence", "java.lang.CharSequence"));
        s.add(method("SettingsActivity$SettingCell$Factory", "ofIIIICCC").named("of").isStatic(true)
                .sig(uitem, "int", "int", "int", "int", "java.lang.CharSequence", "java.lang.CharSequence", "java.lang.CharSequence"));
    }

    // ---------------------------------------------------------------- action bar

    private static void actionBar(List<Symbol> s) {
        String ami = "org.telegram.ui.ActionBar.ActionBarMenuItem";
        String sub = "org.telegram.ui.ActionBar.ActionBarMenuSubItem";
        String item = "org.telegram.ui.ActionBar.ActionBarMenuItem$Item";
        String rp = "org.telegram.ui.ActionBar.Theme$ResourcesProvider";
        String drawable = "android.graphics.drawable.Drawable";

        s.add(cls("org.telegram.ui.ActionBar.ActionBar")
                .from(fieldTypeOf("org.telegram.ui.ActionBar.BaseFragment", "actionBar"))
                .where(extendsType("android.widget.FrameLayout")));
        // A tiny listener class every fragment subclasses: onItemClick(int) and canOpenMenu().
        s.add(cls("org.telegram.ui.ActionBar.ActionBar$ActionBarMenuOnItemClick")
                .from(fieldTypesOf("org.telegram.ui.ActionBar.ActionBar"))
                .where(extendsType("java.lang.Object"), hasMethod(false, "void", "int"),
                        hasMethod(false, "boolean"), methodCountAtMost(4)));
        s.add(method("ActionBar", "setActionBarMenuOnItemClick")
                .sig("void", "org.telegram.ui.ActionBar.ActionBar$ActionBarMenuOnItemClick"));

        // The menu button tells accessibility it is an ImageButton (icon) or a Button (text).
        // Its old helper overloads (addSubItem(int, CharSequence), ...) are gone in newer builds.
        s.add(cls(ami)
                .from(declaringStrings("android.widget.ImageButton", "android.widget.Button"))
                .where(inherits("android.widget.FrameLayout"), loadsString("android.widget.ImageButton"),
                        loadsString("android.widget.Button")));
        s.add(cls(sub)
                .from(returnTypeWhere(ami, "int", "int", "java.lang.CharSequence"),
                        returnTypeWhere(ami, "int", "int", "java.lang.String"))
                .where(extendsType("android.widget.FrameLayout")));
        // lazilyAddSubItem(int, int, CharSequence); R8 narrows the text to String when only
        // strings are passed.
        s.add(cls(item)
                .from(returnTypeWhere(ami, "int", "int", "java.lang.CharSequence"),
                        returnTypeWhere(ami, "int", "int", "java.lang.String"))
                .where(extendsType("java.lang.Object")));

        s.add(method("ActionBarMenuItem", "addSubItemIC").named("addSubItem").sig("android.widget.TextView", "int", "java.lang.CharSequence"));
        s.add(method("ActionBarMenuItem", "addSubItemIIC").named("addSubItem").sig(sub, "int", "int", "java.lang.CharSequence")
                .narrowedStrings());
        s.add(method("ActionBarMenuItem", "addSubItemIICO").named("addSubItem").sig(sub, "int", "int", "java.lang.CharSequence", rp));
        s.add(method("ActionBarMenuItem", "addSubItemIICZ").named("addSubItem").sig(sub, "int", "int", "java.lang.CharSequence", "boolean"));
        s.add(method("ActionBarMenuItem", "addSubItemIIDCZZ").named("addSubItem")
                .sig(sub, "int", "int", drawable, "java.lang.CharSequence", "boolean", "boolean"));
        s.add(method("ActionBarMenuItem", "addSubItemIIDCZZO").named("addSubItem")
                .sig(sub, "int", "int", drawable, "java.lang.CharSequence", "boolean", "boolean", rp));
        s.add(method("ActionBarMenuItem", "addSubItemIV").named("addSubItem").sig("android.view.View", "int", "android.view.View"));
        s.add(method("ActionBarMenuItem", "addSubItemIVII").named("addSubItem").sig("void", "int", "android.view.View", "int", "int"));
        s.add(method("ActionBarMenuItem", "addSubItemVII").named("addSubItem").sig("void", "android.view.View", "int", "int"));
        s.add(method("ActionBarMenuItem", "lazilyAddSubItemIDC").named("lazilyAddSubItem").sig(item, "int", drawable, "java.lang.CharSequence"));
        s.add(method("ActionBarMenuItem", "addSubItem").sig(sub, "int", "int", "java.lang.CharSequence")
                .narrowedStrings());
        s.add(method("ActionBarMenuItem", "lazilyAddSubItem").sig(item, "int", "int", "java.lang.CharSequence")
                .narrowedStrings());
        s.add(method("ActionBarMenuItem", "lazilyAddSubItemIIC").named("lazilyAddSubItem").sig(item, "int", "int", "java.lang.CharSequence")
                .narrowedStrings());
        s.add(method("ActionBarMenuItem", "lazilyAddSubItemIIDCZZ").named("lazilyAddSubItem")
                .sig(item, "int", "int", drawable, "java.lang.CharSequence", "boolean", "boolean"));
    }

    // -------------------------------------------------------------- alert dialog

    private static void alertDialog(List<Symbol> s) {
        String dialog = "org.telegram.ui.ActionBar.AlertDialog";
        String builder = "org.telegram.ui.ActionBar.AlertDialog$Builder";
        String listener = "org.telegram.ui.ActionBar.AlertDialog$OnButtonClickListener";

        // The dialog's own setTitle overrides android.app.Dialog's, so it keeps its name - and the
        // field it writes tells the builder's title setter apart from its message setter.
        s.add(method("AlertDialog", "setTitle").sig("void", "java.lang.CharSequence"));
        s.add(cls(listener)
                .from(paramTypeWhere(builder, "void", "java.lang.CharSequence", null),
                        paramTypeWhere(builder, builder, "java.lang.CharSequence", null))
                .where(isInterface(), hasMethod(false, "void", dialog, "int")));
        s.add(method("AlertDialog$OnButtonClickListener", "onClick").sig("void", dialog, "int"));
        s.add(method("AlertDialog", "setButton").sig("void", "int", "java.lang.CharSequence", listener)
                .where(switchKey(-3), switchKey(-2), switchKey(-1)));

        s.add(method("AlertDialog$Builder", "setTitle").sig(builder, "java.lang.CharSequence").voidable()
                .where(writesFieldWrittenBy("AlertDialog#setTitle", "java.lang.CharSequence")));
        s.add(method("AlertDialog$Builder", "setMessage").sig(builder, "java.lang.CharSequence").voidable()
                .where(writesFieldOfType(dialog, "java.lang.CharSequence"),
                        not(writesFieldWrittenBy("AlertDialog#setTitle", "java.lang.CharSequence"))));
        s.add(method("AlertDialog$Builder", "setViewV").named("setView").sig(builder, "android.view.View").voidable());
        s.add(method("AlertDialog$Builder", "setViewVI").named("setView").sig(builder, "android.view.View", "int").voidable());
        s.add(method("AlertDialog$Builder", "create").sig(dialog)
                .where(not(callsAnyNamed("show"))));
        s.add(method("AlertDialog$Builder", "show").sig(dialog).voidable()
                .where(callsAnyNamed("show")));
        s.add(method("AlertDialog$Builder", "getDismissRunnable").sig("java.lang.Runnable"));
        // Positive is pinned by meaning: its listener is the one the dialog invokes with
        // BUTTON_POSITIVE (-1). Negative and neutral cannot be told apart that way - Telegram's
        // neutral handler also passes BUTTON_NEGATIVE - so they are resolved only where a build
        // leaves exactly one candidate, and otherwise refused rather than risk a swapped action.
        s.add(method("AlertDialog$Builder", "setPositiveButton").sig(builder, "java.lang.CharSequence", listener)
                .voidable().narrowedStrings()
                .where(writesListenerInvokedWith(dialog, listener, -1)));
        s.add(method("AlertDialog$Builder", "setNegativeButton").sig(builder, "java.lang.CharSequence", listener)
                .voidable().narrowedStrings()
                .where(not(writesListenerInvokedWith(dialog, listener, -1))));
        s.add(method("AlertDialog$Builder", "setNeutralButton").sig(builder, "java.lang.CharSequence", listener)
                .voidable().narrowedStrings()
                .where(not(writesListenerInvokedWith(dialog, listener, -1))));
    }

    // -------------------------------------------------------------------- cells

    private static void cells(List<Symbol> s) {
        String rp = "org.telegram.ui.ActionBar.Theme$ResourcesProvider";
        s.add(cls("org.telegram.ui.Cells.HeaderCell")
                .from(subclassesOf("android.widget.FrameLayout"))
                .where(hasMethod(false, "void", "java.lang.CharSequence"), hasMethod(false, "void", "java.lang.CharSequence", "boolean"),
                        hasConstructor("android.content.Context", "int", "int", "int", "int", "boolean", "boolean", rp)));
        s.add(method("HeaderCell", "setTextC").named("setText").sig("void", "java.lang.CharSequence"));
        s.add(method("HeaderCell", "setTextCZ").named("setText").sig("void", "java.lang.CharSequence", "boolean"));

        s.add(cls("org.telegram.ui.Cells.TextCheckCell")
                .from(subclassesOf("android.widget.FrameLayout"))
                // setTextAndCheck keeps its CharSequence; setTextAndValueAndCheck's may be narrowed
                // to String. Look-alikes (TextCheckCell2, radio cells, NagramX's TextSettingsCell)
                // lack the switch or the circular reveal animation.
                .where(hasConstructor("android.content.Context"),
                        hasMethod(false, "void", "java.lang.CharSequence", "boolean", "boolean"),
                        hasField(false, "org.telegram.ui.Components.Switch"), loadsString("animationProgress")));
        s.add(method("TextCheckCell", "isChecked").sig("boolean"));
        s.add(method("TextCheckCell", "setChecked").sig("void", "boolean")
                .where(calls("org.telegram.ui.Components.Switch", "void", "boolean", "boolean")));
        s.add(method("TextCheckCell", "setTextAndCheck").sig("void", "java.lang.CharSequence", "boolean", "boolean"));
        // The setters start with textView.setText(...), so the first TextView they read is it.
        s.add(field("TextCheckCell", "textView").type("android.widget.TextView").readBy("TextCheckCell#setTextAndCheck", 0));
        s.add(method("TextCheckCell", "setTextAndValueAndCheck").narrowedStrings()
                .sig("void", "java.lang.CharSequence", "java.lang.CharSequence", "boolean", "boolean", "boolean"));

        s.add(cls("org.telegram.ui.Cells.TextSettingsCell")
                .from(subclassesOf("android.widget.FrameLayout"))
                .where(hasConstructor("android.content.Context"), hasConstructor("android.content.Context", "int", rp),
                        hasMethod(false, "void", "java.lang.CharSequence", "boolean"),
                        hasMethod(false, "void", "java.lang.CharSequence", "java.lang.CharSequence", "boolean", "boolean")));
        s.add(method("TextSettingsCell", "setText").sig("void", "java.lang.CharSequence", "boolean"));
        s.add(field("TextSettingsCell", "textView").type("android.widget.TextView").readBy("TextSettingsCell#setText", 0));
        s.add(method("TextSettingsCell", "setTextAndValueCCZ").named("setTextAndValue").sig("void", "java.lang.CharSequence", "java.lang.CharSequence", "boolean"));
        s.add(method("TextSettingsCell", "setTextAndValueCCZZ").named("setTextAndValue").sig("void", "java.lang.CharSequence", "java.lang.CharSequence", "boolean", "boolean"));

        s.add(cls("org.telegram.ui.Cells.ShadowSectionCell")
                .from(subclassesOf("android.view.View"))
                .where(hasConstructor("android.content.Context", "int", "int", rp)));
    }

    // --------------------------------------------------------------------- chat

    private static void profile(List<Symbol> s) {
        String p = "org.telegram.ui.ProfileActivity";
        String stv = "org.telegram.ui.ActionBar.SimpleTextView";

        // A View with setText(CharSequence) / setText(CharSequence, boolean) both returning
        // whether the text changed - the shape no other view has.
        s.add(cls(stv)
                .from(returnTypeWhere(p, "boolean"), subclassesOf("android.view.View"))
                .where(extendsType("android.view.View"),
                        hasMethod(false, "boolean", "java.lang.CharSequence"),
                        hasMethod(false, "boolean", "java.lang.CharSequence", "boolean"),
                        hasMethod(false, "java.lang.CharSequence")));
        s.add(method("SimpleTextView", "setText").sig("boolean", "java.lang.CharSequence"));
        s.add(method("SimpleTextView", "getText").sig("java.lang.CharSequence"));

        // onFragmentCreate: userId = arguments.getLong("user_id"); chatId = ...("chat_id").
        s.add(method("ProfileActivity", "onFragmentCreate"));
        s.add(field("ProfileActivity", "userId").type("long").writtenBy("ProfileActivity#onFragmentCreate", 0));
        s.add(field("ProfileActivity", "chatId").type("long").writtenBy("ProfileActivity#onFragmentCreate", 1));
        // Field initialisers: nameTextView = new SimpleTextView[2]; onlineTextView = new SimpleTextView[4].
        // (Bundle) only delegates to (Bundle, SharedMediaPreloader), which runs the initialisers.
        s.add(method("ProfileActivity", "<init>").where(usesOpcode(0x23)));
        s.add(field("ProfileActivity", "nameTextView").type(stv + "[]").writtenBy("ProfileActivity#<init>", 0));
        s.add(field("ProfileActivity", "onlineTextView").type(stv + "[]").writtenBy("ProfileActivity#<init>", 1));

        // Rebuilds the three-dot menu; the only (boolean) method offering "delete topics".
        s.add(method("ProfileActivity", "createActionBarMenu").sig("void", "boolean").where(string("DeleteTopics")));
        s.add(field("ProfileActivity", "otherItem").type("org.telegram.ui.ActionBar.ActionBarMenuItem")
                .readBy("ProfileActivity#createActionBarMenu", 0));
        // Fills in name, status and avatar; the only (boolean) method counting bot users.
        s.add(method("ProfileActivity", "updateProfileData").sig("void", "boolean").where(string("BotUsers")));
    }

    private static void chat(List<Symbol> s) {
        String cell = "org.telegram.ui.Cells.ChatMessageCell";
        String mo = "org.telegram.messenger.MessageObject";
        s.add(cls("org.telegram.ui.Cells.ChatMessageCell$MessageAccessibilityNodeProvider")
                .from(declaringStrings("AccActionEnterSelectionMode", "AccDescrMsgNotPlayed"))
                .where(extendsType("android.view.accessibility.AccessibilityNodeProvider")));
        s.add(cls(cell)
                .from(fieldTypesOf("org.telegram.ui.Cells.ChatMessageCell$MessageAccessibilityNodeProvider"))
                .where(inherits("android.view.ViewGroup")));
        s.add(method("ChatMessageCell", "getMessageObject").sig(mo).where(callsNothing()));
        // The only (MessageObject) method that labels imported messages.
        s.add(method("ChatMessageCell", "measureTime").sig("void", mo).where(string("ImportedMessage")));
        // measureTime builds currentTimeString first, then measures it:
        // timeTextWidth = timeWidth = ceil(chat_timePaint.measureText(currentTimeString)).
        s.add(field("ChatMessageCell", "currentTimeString").type("android.text.SpannableStringBuilder")
                .orType("java.lang.CharSequence").writtenBy("ChatMessageCell#measureTime", 0));
        s.add(field("ChatMessageCell", "timeWidth").type("int").writtenBy("ChatMessageCell#measureTime", 0));
        s.add(field("ChatMessageCell", "timeTextWidth").type("int").writtenBy("ChatMessageCell#measureTime", 1));

        // Call sites ask for these by their plain names.
        s.add(method("ChatActivity", "fillMessageMenu")
                .sig("void", mo, "java.util.ArrayList", "java.util.ArrayList", "java.util.ArrayList"));
        s.add(method("ChatActivity", "scrollToMessageId").sig("void", "int", "int", "boolean", "int", "boolean", "int"));
        // processSelectedOption starts with "if (selectedObject == null ...) return".
        s.add(field("ChatActivity", "selectedObject").type(mo).readBy("ChatActivity#processSelectedOption", 0));
        // The three-dot menu is the item createView fills with lazily added entries; forks add
        // action bar items of their own, so the order they are created in says nothing.
        s.add(field("ChatActivity", "headerItem").type("org.telegram.ui.ActionBar.ActionBarMenuItem")
                .handedTo("createView", "ActionBarMenuItem#lazilyAddSubItem"));

        // Builds the pinned bar lazily for updatePinnedMessageView: the one void() helper it calls
        // that stores a new anonymous FrameLayout in a field (its debug-name literal is stripped).
        s.add(method("ChatActivity", "createPinnedMessageView").sig("void")
                .where(calledBy("ChatActivity#updatePinnedMessageViewZI"),
                        storesNewSubclassOf("org.telegram.ui.ChatActivity", "android.widget.FrameLayout")));
        s.add(field("ChatActivity", "pinnedMessageView").type("android.widget.FrameLayout").narrowed()
                .writtenBy("ChatActivity#createPinnedMessageView", 0));
        // The one boolean query over the selection that reads a message's noforwards flag.
        s.add(method("ChatActivity", "hasSelectedNoforwardsMessage").sig("boolean")
                .where(touchesField("org.telegram.tgnet.TLRPC$Message", "noforwards")));
        // The context-menu dispatcher: one switch over the OPTION_* constants, including ones
        // (revenue-sharing ads, speed promo, welcome revert) no other int handler switches on.
        s.add(method("ChatActivity", "processSelectedOption").sig("void", "int")
                .where(switchKey(33), switchKey(103), switchKey(116)));
        s.add(method("ChatActivity", "scrollToMessageIdIIZIZI").named("scrollToMessageId")
                .sig("void", "int", "int", "boolean", "int", "boolean", "int"));
        s.add(method("ChatActivity", "scrollToMessageIdIIZIZIIR").named("scrollToMessageId")
                .sig("void", "int", "int", "boolean", "int", "boolean", "int", "java.lang.Integer", "java.lang.Runnable"));
        s.add(method("ChatActivity", "scrollToMessageIdIIZIZIIABR").named("scrollToMessageId")
                .sig("void", "int", "int", "boolean", "int", "boolean", "int", "java.lang.Integer", "byte[]", "java.lang.Runnable"));
        // Each is pinned by the kept MessagesController call it makes; R8 may narrow the returned
        // Runnable to the lambda class, which the call site's before-hook does not care about.
        s.add(method("ChatActivity", "sendSecretMediaDelete").sig("java.lang.Runnable", mo).narrowedReturn().staticized()
                .where(callsSymbol("MessagesController#createDeleteShowOnceTask")));
        s.add(method("ChatActivity", "sendSecretMessageRead").sig("java.lang.Runnable", mo, "boolean").narrowedReturn()
                .where(callsSymbol("MessagesController#markMessageAsRead2")));
        // (boolean, int) compares the top pin against the dismissed one saved under "pin_<dialog>".
        // 12.10.3 has it as (int, boolean).
        s.add(method("ChatActivity", "updatePinnedMessageViewZI").named("updatePinnedMessageView")
                .sig("void", "boolean", "int").anyOrder()
                .where(string("pin_"), callsSymbol("MessagesController#getNotificationsSettings")));
        // (boolean) only forwards to it; R8 may inline it, in which case hooking (boolean, int) is enough.
        s.add(method("ChatActivity", "updatePinnedMessageViewZ").named("updatePinnedMessageView").sig("void", "boolean")
                .where(callsSymbol("ChatActivity#updatePinnedMessageViewZI"), callCount(1)));

        // ChatActivity's cell delegate. Many screens implement the delegate interface; this one
        // holds its ChatActivity, and its didPressImage flags media for the downloads list.
        // R8 may drop didPressImage's fullPreview flag, so look for both shapes.
        Body pressesImage = touchesField("org.telegram.messenger.MessageObject", "putInDownloadsStore");
        s.add(cls("org.telegram.ui.ChatActivity$ChatMessageCellDelegate")
                .from(declaringMethod("void", cell, "float", "float", "boolean"),
                        declaringMethod("void", cell, "float", "float"))
                .where(hasField(false, "org.telegram.ui.ChatActivity"), someMethod(pressesImage)));
        // The hook reads only the cell (args[0]); the real parameter list is published for it.
        s.add(method("ChatActivity$ChatMessageCellDelegate", "didPressImage")
                .sig("void", cell, "float", "float", "boolean").reads(0).where(pressesImage));
    }

    // ------------------------------------------------------------- photo viewer

    private static void photoViewer(List<Symbol> s) {
        String pv = "org.telegram.ui.PhotoViewer";
        String provider = "org.telegram.ui.PhotoViewer$PhotoViewerProvider";
        String place = "org.telegram.ui.PhotoViewer$PlaceProviderObject";
        String mo = "org.telegram.messenger.MessageObject";
        String loc = "org.telegram.tgnet.TLRPC$FileLocation";
        String imgLoc = "org.telegram.messenger.ImageLocation";
        String chat = "org.telegram.ui.ChatActivity";
        String blocks = "org.telegram.ui.PhotoViewer$PageBlocksAdapter";
        String fragment = "org.telegram.ui.ActionBar.BaseFragment";
        String rp = "org.telegram.ui.ActionBar.Theme$ResourcesProvider";

        s.add(cls(loc).from(superclassOf("org.telegram.tgnet.TLRPC$TL_fileLocationToBeDeprecated")));
        s.add(cls(imgLoc).from(declaringStrings("[richmedia] strippedKey=", " fullObject=")));
        s.add(cls(provider)
                .from(declaringMethod(null, mo, loc, "int", "boolean", "boolean"),
                        paramTypeWhere("org.telegram.ui.SecretMediaViewer", "void", mo, null,
                                "java.lang.Runnable", "java.lang.Runnable"))
                .where(isInterface()));
        s.add(cls(place).from(returnTypeWhere(provider, mo, loc, "int", "boolean", "boolean")));
        s.add(field("PhotoViewer$PlaceProviderObject", "imageReceiver")
                .type("org.telegram.messenger.ImageReceiver").onlyOneOfType());
        s.add(method("PhotoViewer$PhotoViewerProvider", "getPlaceForPhoto").sig(place, mo, loc, "int", "boolean", "boolean"));
        // Named by openPhoto(int, PageBlocksAdapter, provider) - or, once R8 has inlined that
        // overload, by the one openPhoto every overload funnels into.
        s.add(cls(blocks)
                .from(paramTypeWhere(pv, "boolean", "int", null, provider),
                        paramTypeWhere(pv, "boolean", mo, loc, imgLoc, imgLoc, "java.util.ArrayList",
                                "java.util.ArrayList", "java.util.ArrayList", "int", provider, chat,
                                "long", "long", "long", "boolean", null, "java.lang.Integer"))
                .where(isInterface()));

        // getPipInstance and the like only return a field; getInstance creates the viewer.
        s.add(method("PhotoViewer", "getInstance").isStatic(true).sig(pv).where(refersTo(pv)));
        s.add(method("PhotoViewer", "openPhotoAIJJJO").named("openPhoto")
                .sig("boolean", "java.util.ArrayList", "int", "long", "long", "long", provider));
        s.add(method("PhotoViewer", "openPhotoAIO").named("openPhoto").sig("boolean", "java.util.ArrayList", "int", provider));
        s.add(method("PhotoViewer", "openPhotoIOO").named("openPhoto").sig("boolean", "int", blocks, provider));
        s.add(method("PhotoViewer", "openPhotoOIOJJJO").named("openPhoto")
                .sig("boolean", mo, "int", chat, "long", "long", "long", provider));
        s.add(method("PhotoViewer", "openPhotoOJJJOZ").named("openPhoto")
                .sig("boolean", mo, "long", "long", "long", provider, "boolean"));
        s.add(method("PhotoViewer", "openPhotoOO").named("openPhoto").sig("boolean", loc, provider));
        s.add(method("PhotoViewer", "openPhotoOOJJJO").named("openPhoto")
                .sig("boolean", mo, chat, "long", "long", "long", provider));
        s.add(method("PhotoViewer", "openPhotoOOO").named("openPhoto").sig("boolean", loc, imgLoc, provider));
        s.add(method("PhotoViewer", "openPhotoOOOOAAAIOOJJJZOI").named("openPhoto")
                .sig("boolean", mo, loc, imgLoc, imgLoc, "java.util.ArrayList", "java.util.ArrayList", "java.util.ArrayList",
                        "int", provider, chat, "long", "long", "long", "boolean", blocks, "java.lang.Integer"));
        s.add(method("PhotoViewer", "setIsAboutToSwitchToIndexIZZ").named("setIsAboutToSwitchToIndex")
                .sig("void", "int", "boolean", "boolean"));
        // A look-alike (int, boolean, boolean, boolean) exists; only this one names YouTube videos.
        s.add(method("PhotoViewer", "setIsAboutToSwitchToIndexIZZZ").named("setIsAboutToSwitchToIndex")
                .sig("void", "int", "boolean", "boolean", "boolean").where(string("YouTube")));
        s.add(method("PhotoViewer", "setParentActivityA").named("setParentActivity").sig("void", "android.app.Activity"));
        s.add(method("PhotoViewer", "setParentActivityAO").named("setParentActivity").sig("void", "android.app.Activity", rp));
        s.add(method("PhotoViewer", "setParentActivityAOO").named("setParentActivity")
                .sig("void", "android.app.Activity", fragment, rp));
        // Forks add their own items to the menu setParentActivity builds, but switching photos
        // still toggles the picture-in-picture item first and the gallery item second.
        s.add(field("PhotoViewer", "galleryButton").type("org.telegram.ui.ActionBar.ActionBarMenuSubItem")
                .readBy("PhotoViewer#setIsAboutToSwitchToIndexIZZZ", 1));
        s.add(method("PhotoViewer", "setParentActivityO").named("setParentActivity").sig("void", fragment));
        s.add(method("PhotoViewer", "setParentActivityOO").named("setParentActivity").sig("void", fragment, rp));

        // The Activity's content view. Not "the only FrameLayout field": R8 narrows it to its
        // anonymous subclass, leaving the tablet-only shadowTablet as the sole plain FrameLayout.
        s.add(field("LaunchActivity", "frameLayout").type("android.widget.FrameLayout")
                .handedTo("onCreate", "setContentView"));

        s.add(method("SecretMediaViewer", "closePhoto").sig("boolean", "boolean", "boolean"));
        s.add(method("SecretMediaViewer", "openMedia")
                .sig("void", mo, provider, "java.lang.Runnable", "java.lang.Runnable"));
    }

    // ------------------------------------------------------------------ stories

    private static void stories(List<Symbol> s) {

        // allowScreenshots(): false for noforwards stories, and for pinned ones of a noforwards
        // chat - the only boolean() in the app reading both flags of a story item.
        Body screenshots = Body.all(touchesField("org.telegram.tgnet.tl.TL_stories$StoryItem", "noforwards"),
                touchesField("org.telegram.tgnet.tl.TL_stories$StoryItem", "pinned"));
        // Some forks rename the tl.* classes; a TL object's constructor id pins it anyway.
        s.add(cls("org.telegram.tgnet.tl.TL_stories$TL_storyItem").from(declaringConstant(379894076))
                .where(hasMethod(false, "void", "org.telegram.tgnet.OutputSerializedData")));
        s.add(cls("org.telegram.tgnet.tl.TL_stories$StoryItem")
                .from(superclassOf("org.telegram.tgnet.tl.TL_stories$TL_storyItem")));
        s.add(cls("org.telegram.tgnet.tl.TL_stories$TL_peerStories").from(declaringConstant(-1707742823))
                .where(hasMethod(false, "void", "org.telegram.tgnet.OutputSerializedData")));
        s.add(cls("org.telegram.tgnet.tl.TL_stories$PeerStories")
                .from(superclassOf("org.telegram.tgnet.tl.TL_stories$TL_peerStories")));
        s.add(cls("org.telegram.ui.Stories.PeerStoriesView$StoryItemHolder")
                .from(declaringMethodWhere(screenshots, "boolean")));
        s.add(method("PeerStoriesView$StoryItemHolder", "allowScreenshots").sig("boolean").where(screenshots));
        String peerStories = "org.telegram.tgnet.tl.TL_stories$PeerStories";
        s.add(method("StoriesController", "hasStoriesJ").named("hasStories").sig("boolean", "long")
                .where(callsSibling(peerStories, "long"), callsSibling("boolean", "long"),
                        callsNamed("java.util.ArrayList", "isEmpty")));
        // hasStories(): "stories in the dialog list, or the user's own". R8 may inline it away
        // entirely, and a lookalike (hasOnlySelfStories) also checks the list, so the fingerprint
        // excludes anything that inspects individual peers.
        s.add(method("StoriesController", "hasStories").sig("boolean")
                .where(callsSibling("boolean"), callsNamed("java.util.ArrayList", "size"),
                        not(touchesField(peerStories, "peer"))));
    }

    // --------------------------------------------------------------------- misc

    private static void misc(List<Symbol> s) {
        String browser = "org.telegram.messenger.browser.Browser";
        String progress = "org.telegram.messenger.browser.Browser$Progress";
        String ctx = "android.content.Context";
        String uri = "android.net.Uri";

        s.add(method("LongSparseArray", "getJ").named("get").sig("java.lang.Object", "long"));
        s.add(method("LongSparseArray", "getJO").named("get").sig("java.lang.Object", "long", "java.lang.Object"));

        s.add(cls(progress).from(paramTypeWhere(browser, "void", ctx, uri, "boolean", "boolean", "boolean",
                null, "java.lang.String", "boolean", "boolean", "boolean")));
        // openUrlInSystemBrowser has the same signature, but it goes straight to the ten-parameter
        // overload; openUrl(Context, String) never does, whether or not R8 inlines the step between.
        s.add(method("Browser", "openUrlCS").named("openUrl").isStatic(true).sig("void", ctx, "java.lang.String")
                .where(not(callsSibling("void", ctx, uri, "boolean", "boolean", "boolean", progress,
                        "java.lang.String", "boolean", "boolean", "boolean"))));
        s.add(method("Browser", "openUrlCSZ").named("openUrl").isStatic(true).sig("void", ctx, "java.lang.String", "boolean"));
        s.add(method("Browser", "openUrlCSZZ").named("openUrl").isStatic(true)
                .sig("void", ctx, "java.lang.String", "boolean", "boolean"));
        s.add(method("Browser", "openUrlCU").named("openUrl").isStatic(true).sig("void", ctx, uri));
        s.add(method("Browser", "openUrlCUZ").named("openUrl").isStatic(true).sig("void", ctx, uri, "boolean"));
        s.add(method("Browser", "openUrlCUZZ").named("openUrl").isStatic(true).sig("void", ctx, uri, "boolean", "boolean"));
        s.add(method("Browser", "openUrlCUZZO").named("openUrl").isStatic(true)
                .sig("void", ctx, uri, "boolean", "boolean", progress));
        s.add(method("Browser", "openUrlCUZZZOSZZZ").named("openUrl").isStatic(true)
                .sig("void", ctx, uri, "boolean", "boolean", "boolean", progress, "java.lang.String", "boolean", "boolean", "boolean"));

        // "return currentTheme.isDark()": one call, returned as is. isCurrentThemeDay is the same
        // call negated (xor-int/lit8 ..., 1), or two calls where getActiveTheme() is not inlined.
        s.add(method("Theme", "isCurrentThemeDark").isStatic(true).sig("boolean")
                .where(callCount(1), not(usesOpcode(0xdf))));
        // measureTime also touches chat_unlockExtendedMediaTextPaint, but reads chat_timePaint first.
        s.add(field("Theme", "chat_timePaint").isStatic(true).type("android.text.TextPaint")
                .readBy("ChatMessageCell#measureTime", 0));
    }
}
