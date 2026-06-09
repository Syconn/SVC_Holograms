package mod.syconn.svc.utils.generic;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;

public class FontUtil {
    private static final FormattedText ELLIPSIS = FormattedText.of("...");

    public static FormattedText ellipsis(Font font, FormattedText text, int maxWidth) {
        final int strWidth = font.width(text);
        final int ellipsisWidth = font.width(ELLIPSIS);
        if (strWidth > maxWidth) {
            if (ellipsisWidth >= maxWidth) return font.substrByWidth(text, maxWidth);
            return FormattedText.composite(font.substrByWidth(text, maxWidth - ellipsisWidth), ELLIPSIS);
        }
        return text;
    }
}
