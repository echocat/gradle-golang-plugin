package org.echocat.gradle.plugins.golang.model;

import com.google.gson.annotations.SerializedName;
import org.echocat.gradle.plugins.golang.Version;
import org.echocat.gradle.plugins.golang.vcs.VcsType;

import java.net.URI;

public class VcsRepositoryInfo {

    @SerializedName("creator")
    private String _creator = Version.NAME + " " + Version.VERSION;

    @SerializedName("type")
    private VcsType _type;
    @SerializedName("id")
    private String _id;
    @SerializedName("uri")
    private String _uri;
    @SerializedName("ref")
    private String _ref;

    @SerializedName("fullRef")
    private String _fullRef;
    @SerializedName("lastUpdatedMillis")
    private long _lastUpdatedMillis;

    public String getCreator() {
        return _creator;
    }

    public VcsRepositoryInfo setCreator(String creator) {
        _creator = creator;
        return this;
    }

    public VcsType getType() {
        return _type;
    }

    public VcsRepositoryInfo setType(VcsType type) {
        _type = type;
        return this;
    }

    public String getId() {
        return _id;
    }

    public VcsRepositoryInfo setId(String id) {
        _id = id;
        return this;
    }

    public String getUri() {
        return _uri;
    }

    public VcsRepositoryInfo setUri(String uri) {
        _uri = uri;
        return this;
    }

    public VcsRepositoryInfo setUri(URI uri) {
        return setUri(uri != null ? uri.toString() : null);
    }

    public String getRef() {
        return _ref;
    }

    public VcsRepositoryInfo setRef(String ref) {
        _ref = ref;
        return this;
    }

    public String getFullRef() {
        return _fullRef;
    }

    public VcsRepositoryInfo setFullRef(String fullRef) {
        _fullRef = fullRef;
        return this;
    }

    public long getLastUpdatedMillis() {
        return _lastUpdatedMillis;
    }

    public VcsRepositoryInfo setLastUpdatedMillis(long lastUpdatedMillis) {
        _lastUpdatedMillis = lastUpdatedMillis;
        return this;
    }

}
