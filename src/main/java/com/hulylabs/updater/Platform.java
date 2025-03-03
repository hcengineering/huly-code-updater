package com.hulylabs.updater;

public enum Platform {
    MAC(
            "mac",
            "huly-code-%s-aarch64.sit",
            "IC-%s-%s-patch-aarch64-mac.jar"),
    MAC_X64(
            "mac-x64",
            "huly-code-%s.sit",
            "IC-%s-%s-patch-mac.jar"),
    WINDOWS(
            "win",
            "huly-code-%s.win.zip",
            "IC-%s-%s-patch-win.jar"),
    LINUX(
            "linux",
            "huly-code-%s.tar.gz",
            "IC-%s-%s-patch-unix.jar"),
    ;

    private final String name;
    private final String downloadName;
    private final String patchName;

    Platform(String name, String downloadName, String patchName) {
        this.name = name;
        this.downloadName = downloadName;
        this.patchName = patchName;
    }

    public String getName() {
        return name;
    }

    public String getDownloadName(String version) {
        return String.format(downloadName, version);
    }

    public String getPatchName(String fromVersion, String toVersion) {
        return String.format(patchName, fromVersion, toVersion);
    }
}
