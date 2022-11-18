package com.smartsparrow.asset.data;

import java.util.Arrays;

public class AllowedAsset {

    public enum IMAGES {
        PNG,
        JPG,
        JPEG,
        GIF,
        SVG;

        public static boolean allows(String fileExtension) {
            return Arrays.stream(IMAGES.values())
                    .anyMatch(one-> one.toString().equalsIgnoreCase(fileExtension));
        }
    }

    public enum AUDIO {
        AAC,
        MP3,
        OGG,
        WAV;

        public static boolean allows(String fileExtension) {
            return Arrays.stream(AUDIO.values())
                    .anyMatch(one-> one.toString().equalsIgnoreCase(fileExtension));
        }
    }

    public enum VIDEO {
        MP4,
        MOV
    }

    public enum DOCUMENT {
        PDF,
        WORD
    }

    public enum SUBTITLE {
        TTML,
        SSA,
        ASS,
        VTT,
        SRT;
        public static boolean allows(String fileExtension) {
            return Arrays.stream(SUBTITLE.values())
                    .anyMatch(one-> one.toString().equalsIgnoreCase(fileExtension));
        }
    }

    public enum ICON {
        SVG;

        public static boolean allows(String fileExtension) {
            return Arrays.stream(ICON.values())
                    .anyMatch(one-> one.toString().equalsIgnoreCase(fileExtension));
        }
    }
}
