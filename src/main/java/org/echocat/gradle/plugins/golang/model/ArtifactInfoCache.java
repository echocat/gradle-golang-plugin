package org.echocat.gradle.plugins.golang.model;

import com.google.gson.annotations.SerializedName;
import org.echocat.gradle.plugins.golang.Version;
import org.echocat.gradle.plugins.golang.vcs.VcsFullReference;
import org.echocat.gradle.plugins.golang.vcs.RawVcsReference;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.TreeMap;

import static java.lang.System.currentTimeMillis;

public class ArtifactInfoCache {

    @SerializedName("revision")
    private int _revision = 1;
    @SerializedName("creator")
    private String _creator = Version.NAME + " " + Version.VERSION;
    @SerializedName("entries")
    private Map<String, Entry> _entries = new TreeMap<>();

    public int getRevision() {
        return _revision;
    }

    public void setRevision(int revision) {
        _revision = revision;
    }

    public String getCreator() {
        return _creator;
    }

    public void setCreator(String creator) {
        _creator = creator;
    }

    public Map<String, Entry> getEntries() {
        return _entries;
    }

    @Nullable
    public Entry findEntryBy(@Nonnull String id) {
        final Map<String, Entry> entries = _entries;
        if (entries == null) {
            return null;
        }
        return entries.get(id);
    }

    public void saveEntry(@Nonnull String id, @Nonnull Entry entry) {
        if (_entries == null) {
            _entries = new TreeMap<>();
        }
        _entries.put(id, entry);
    }

    public void saveEntry(@Nonnull String id, @Nullable String ref, @Nonnull String detailedRef, @Nonnegative long lastUpdatedMillis, boolean managed) {
        final Entry entry = new Entry();
        entry.setRef(ref);
        entry.setDetailedRef(detailedRef);
        entry.setLastUpdatedMillis(lastUpdatedMillis);
        entry.setManaged(managed);
        saveEntry(id, entry);
    }

    public void saveEntry(@Nonnull RawVcsReference rawReference, @Nonnull VcsFullReference fullReference, boolean managed) {
        saveEntry(rawReference.getId(), rawReference.getRef(), fullReference.getFull(), currentTimeMillis(), managed);
    }

    public void removeEntry(@Nonnull String id) {
        final Map<String, Entry> entries = _entries;
        if (entries == null) {
            return;
        }
        entries.remove(id);
    }

    public static class Entry {

        @SerializedName("ref")
        private String _ref;
        @SerializedName("detailedRef")
        private String _detailedRef;
        @SerializedName("lastUpdatedMillis")
        private long _lastUpdatedMillis;
        @SerializedName("managed")
        private boolean _managed = true;

        public String getRef() {
            return _ref;
        }

        public void setRef(String ref) {
            _ref = ref;
        }

        public String getDetailedRef() {
            return _detailedRef;
        }

        public void setDetailedRef(String detailedRef) {
            _detailedRef = detailedRef;
        }

        public long getLastUpdatedMillis() {
            return _lastUpdatedMillis;
        }

        public void setLastUpdatedMillis(long lastUpdatedMillis) {
            _lastUpdatedMillis = lastUpdatedMillis;
        }

        public boolean isManaged() {
            return _managed;
        }

        public void setManaged(boolean managed) {
            this._managed = managed;
        }
    }

}
