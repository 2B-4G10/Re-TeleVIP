package com.my.televip.reflect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * The empty ad answers (messages.sponsoredMessagesEmpty, contacts.sponsoredPeersEmpty) are made
 * with {@link XReflect#allocateInstance} on builds whose R8 removed their constructors (Nagram,
 * Nekogram 12.10.x). The object must come out usable: its inherited lists empty, not null.
 */
public class XReflectAllocateTest {

    /** Like TLRPC.messages_SponsoredMessages: the lists are filled by the constructor. */
    abstract static class SponsoredMessages {
        public int flags;
        public ArrayList<Object> messages = new ArrayList<>();
        public List<Object> chats = new ArrayList<>();
        public Object other;
    }

    /** Like the stripped TL_messages_sponsoredMessagesEmpty: no usable no-argument constructor. */
    static final class SponsoredMessagesEmpty extends SponsoredMessages {
        private SponsoredMessagesEmpty(int unused) {
            throw new AssertionError("must not run");
        }
    }

    @Test
    public void makesAnInstanceWithoutItsConstructor() {
        Object empty = XReflect.allocateInstance(SponsoredMessagesEmpty.class);
        assertTrue(empty instanceof SponsoredMessagesEmpty);
    }

    @Test
    public void fillsTheListsAConstructorWouldHave() {
        SponsoredMessagesEmpty empty = (SponsoredMessagesEmpty) XReflect.allocateInstance(SponsoredMessagesEmpty.class);
        assertNotNull(empty.messages);
        assertNotNull(empty.chats);
        assertEquals(0, empty.messages.size());
        assertEquals(0, empty.chats.size());
        assertEquals(0, empty.flags);
        // Only list fields are filled; anything else stays as allocated.
        assertEquals(null, empty.other);
    }

    /** Like TL_messages_affectedMessages on builds that keep its constructor. */
    static final class AffectedMessages {
        public int pts = 7;
    }

    @Test
    public void usesTheConstructorWhereThereIsOne() {
        AffectedMessages made = (AffectedMessages) XReflect.newInstanceOrAllocate(AffectedMessages.class);
        assertEquals(7, made.pts);
    }

    @Test
    public void allocatesWhereTheConstructorIsGone() {
        Object made = XReflect.newInstanceOrAllocate(SponsoredMessagesEmpty.class);
        assertTrue(made instanceof SponsoredMessagesEmpty);
        assertNotNull(((SponsoredMessagesEmpty) made).messages);
    }
}
