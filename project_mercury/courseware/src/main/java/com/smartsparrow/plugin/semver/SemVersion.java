package com.smartsparrow.plugin.semver;

import java.util.UUID;

import com.github.zafarkhaja.semver.ParseException;
import com.github.zafarkhaja.semver.Version;
import com.google.common.base.Strings;
import com.smartsparrow.plugin.data.PluginVersion;
import com.smartsparrow.plugin.lang.VersionParserFault;

/**
 * This class is aimed to be used to compare and operate over SemVer versions.
 * It encapsulate the usage of jsemver library.
 *
 * @see Version
 * @see <a href="https://semver.org/">Semantic Versioning</a>
 * @see <a href="https://github.com/zafarkhaja/jsemver">Java SemVer library</a>
 */
public class SemVersion {

    private Version version;

    private SemVersion() {

    }

    /**
     * Creates SemVersion instance from {@link PluginVersion} object
     *
     * @param v plugin version
     * @return SemVersion
     */
    public static SemVersion from(PluginVersion v) {
        String version = Version.forIntegers(v.getMajor(), v.getMinor(), v.getPatch()).toString();
        Version.Builder builder = new Version.Builder();
        builder.setNormalVersion(version);
        builder.setPreReleaseVersion(v.getPreRelease());
        builder.setBuildMetadata(v.getBuild());
        SemVersion semversion = new SemVersion();
        semversion.version = builder.build();
        return semversion;
    }

    /**
     * Creates SemVersion instance from {@link String}
     *
     * @param v string to parse into SemVersion (ex. "1.2.0", "1.0.0-alpha+001")
     * @return SemVersion
     * @throws VersionParserFault - if {@param v} is empty or has incorrect format
     */
    public static SemVersion from(String v) throws VersionParserFault {
        Version version;
        try {
            version = Version.valueOf(v);
        } catch (IllegalArgumentException | ParseException e) {
            throw new VersionParserFault(String.format("Invalid format '%s' ", v));
        }
        SemVersion semversion = new SemVersion();
        semversion.version = version;
        return semversion;
    }

    /**
     * Verifies if this version satisfies the expression.
     * <br/>
     * Note: Expressions and wildcards are supported only for stable versions, ex '1.*' or '1.2.*'. For unstable versions
     * (versions with pre-release or build) it searches a suitable version by full match.
     *
     * @param expression expression
     * @return {@code true} - if satisfies the expression, otherwise - {@code false}
     */
    public boolean satisfies(SemVerExpression expression) {
        if (expression.isSearchByEquals()) {
            return this.version.toString().equals(expression.getExprString());
        } else {
            return this.version.satisfies(expression.getParsedExpr()) && isStable();
        }
    }

    /**
     * Verifies if this version is stable. Stable means that it contains only major, minor and patch numbers.
     *
     * @return {@code true} - if version is stable, otherwise - {@code false}
     */
    public boolean isStable() {
        return (this.version.getBuildMetadata() == null || this.version.getBuildMetadata().isEmpty())
                && (this.version.getPreReleaseVersion() == null || this.version.getPreReleaseVersion().isEmpty());
    }

    /**
     * Converts this version to {@link PluginVersion} object
     *
     * @param pluginId    plugin id
     * @param releaseDate date when this version was released/published
     * @return new instance of PluginVersion
     */
    public PluginVersion toPluginVersion(UUID pluginId, long releaseDate) {
        Version v = this.version;
        return new PluginVersion()
                .setMajor(v.getMajorVersion())
                .setMinor(v.getMinorVersion())
                .setPatch(v.getPatchVersion())
                .setBuild(Strings.isNullOrEmpty(v.getBuildMetadata()) ? null : v.getBuildMetadata())
                .setPreRelease(Strings.isNullOrEmpty(v.getPreReleaseVersion()) ? null : v.getPreReleaseVersion())
                .setPluginId(pluginId)
                .setReleaseDate(releaseDate);
    }

    /**
     * Returns the major version number
     *
     * @return the major version number
     * @see Version#getMajorVersion()
     */
    public int getMajorVersion() {
        return this.version.getMajorVersion();
    }

    /**
     * Returns the minor version number
     *
     * @return the minor version number
     * @see Version#getMinorVersion()
     */
    public int getMinorVersion() {
        return this.version.getMinorVersion();
    }

    /**
     * Returns the patch version number
     *
     * @return the patch version number
     * @see Version#getPatchVersion()
     */
    public int getPatchVersion() {
        return this.version.getPatchVersion();
    }

    /**
     * Verifies if this version is greater than the other version.
     *
     * @param other the other version to compare to
     * @return {@code true} if this version is greater than the other version
     * or {@code false} otherwise
     * @see Version#greaterThan(Version)
     */
    public boolean greaterThan(SemVersion other) {
        return this.version.greaterThan(other.version);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.version.toString();
    }

}
