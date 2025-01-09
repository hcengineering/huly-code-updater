package com.hulylabs.updater;

public enum Platform {
    MAC(
            "mac",
            "huly-code-%s-aarch64.sit",
            "IC-%s-%s-patch-mac.jar",
            true),
    WINDOWS(
            "win",
            "huly-code-%s.win.zip",
            "IC-%s-%s-patch-win.jar",
            true),
    LINUX(
            "linux",
            "huly-code-%s.tar.gz",
            "IC-%s-%s-patch-linux.jar",
            false),
    ;

    private final String name;
    private final String downloadName;
    private final String patchName;
    private final boolean isZipArchive;

    private Platform(String name, String downloadName, String patchName, boolean isZipArchive) {
        this.name = name;
        this.downloadName = downloadName;
        this.patchName = patchName;
        this.isZipArchive = isZipArchive;
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

    public boolean isZipArchive() {
        return isZipArchive;
    }
}
