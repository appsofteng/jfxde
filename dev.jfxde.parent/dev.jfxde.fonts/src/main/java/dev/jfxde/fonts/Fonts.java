package dev.jfxde.fonts;

import java.util.Map;

public class Fonts {

    public static final String FONT_AWESOME_5_FREE_REGULAR = "Font Awesome 5 Free Regular";
    public static final String FONT_AWESOME_5_FREE_SOLID = "Font Awesome 5 Free Solid";
    public static final String OCTICONS = "octicons";

    private static final Map<String, String> URLS = Map.of(FONT_AWESOME_5_FREE_REGULAR, getUrl("/dev/jfxde/fonts/FontAwesome5Free-Regular-400.otf"),
            FONT_AWESOME_5_FREE_SOLID, getUrl("/dev/jfxde/fonts/FontAwesome5Free-Solid-900.otf"),
            OCTICONS, getUrl("/dev/jfxde/fonts/octicons.ttf"));

    private static String getUrl(String path) {
        return Fonts.class.getResource(path).toExternalForm();
    }

    public static Map<String,String> getUrls() {

        return URLS;
    }

    public static class FontAwesome {

        public static final String SEARCH = "\uf002";
        public static final String TH_LARGE = "\uf009";
        public static final String TIMES = "\uf00d";
        public static final String REDO = "\uf01e";
        public static final String BOOKMARK = "\uf02e";
        public static final String TIMES_CIRCLE = "\uf057";
        public static final String ARROW_LEFT = "\uf060";
        public static final String ARROW_RIGHT = "\uf061";
        public static final String EXPAND = "\uf065";
        public static final String COMPRESS = "\uf066";
        public static final String CHEVRON_UP = "\uf077";
        public static final String CHEVRON_DOWN = "\uf078";
        public static final String FOLDER = "\uf07b";
        public static final String FOLDER_OPEN = "\uf07c";
        public static final String GLOBE = "\uf0ac";
        public static final String UNDO = "\uf0e2";
        public static final String FILE = "\uf15b";
        public static final String FILE_ALT = "\uf15c";
        public static final String FILE_ARCHIVE = "\uf1c6";
        public static final String CLONE = "\uf24d";
        public static final String EXPAND_ARROWS_ALT = "\uf31e";
    }

    public static class Octicons {
        public static final String SCREEN_FULL = "\uf066";
        public static final String SCREEN_NORMAL = "\uf067";
    }

    public static class Unicode {

        public static final String WARNING_SIGN = "\u26a0";
        public static final String NEGATIVE_SQUARED_CROSS_MARK = "\u274e";
        public static final String FULLWIDTH_PLUS_SIGN = "\uFF0B";
        public static final String WHITE_LARGE_SQUARE = "\u2b1c";
        public static final String NORTH_WEST_ARROW_TO_CORNER = "\u21f1";
        public static final String UPPER_RIGHT_DROP_SHADOWED_WHITE_SQUARE = "\u2750";
        public static final String NORTH_EAST_AND_SOUTH_WEST_ARROW = "\u2922";
        public static final String TWO_JOINED_SQUARES = "\u29c9";
        public static final String FLOPPY_DISK = "\uD83D\uDCBE";
        public static final String OPEN_FILE_FOLDER = "\uD83D\uDCC2";
        public static final String CIRCLED_INFORMATION_SOURCE = "\ud83d\udec8";
    }
}
