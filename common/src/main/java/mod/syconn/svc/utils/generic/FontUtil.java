package mod.syconn.svc.utils.generic;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

public class FontUtil {
    private static final FormattedText ELLIPSIS = FormattedText.of("...");

    public static FormattedText ellipsize(Font font, FormattedText text, int maxWidth) {
        final int strWidth = font.width(text);
        final int ellipsisWidth = font.width(ELLIPSIS);
        if (strWidth > maxWidth) {
            if (ellipsisWidth >= maxWidth) return font.substrByWidth(text, maxWidth);
            return FormattedText.composite(font.substrByWidth(text, maxWidth - ellipsisWidth), ELLIPSIS);
        }
        return text;
    }

    static final Pattern URL_PATTERN = Pattern.compile("((?:[a-z0-9]{2,}://)?(?:(?:[0-9]{1,3}\\.){3}[0-9]{1,3}|[-\\w_]+\\.[a-z]{2,}?)(?::[0-9]{1,5})?.*?(?=[!\"§ \n]|$))", Pattern.CASE_INSENSITIVE);

    public static Component newChatWithLinks(String string) {
        return newChatWithLinks(string, true);
    }
    public static Component newChatWithLinks(String string, boolean allowMissingHeader) {
        var chat = (MutableComponent) null;
        var matcher = URL_PATTERN.matcher(string);
        var lastEnd = 0;

        while (matcher.find()) {
            var start = matcher.start();
            var end = matcher.end();

            var part = string.substring(lastEnd, start);
            if (!part.isEmpty()) {
                if (chat == null) chat = Component.literal(part);
                else chat.append(part);
            }
            lastEnd = end;
            var url = string.substring(start, end);
            var link = Component.literal(url);

            try {
                if ((new URI(url)).getScheme() == null) {
                    if (!allowMissingHeader) {
                        if (chat == null) chat = Component.literal(url);
                        else chat.append(url);
                        continue;
                    }
                    url = "http://" + url;
                }
            }
            catch (URISyntaxException e) {
                if (chat == null) chat = Component.literal(url);
                else chat.append(url);
                continue;
            }

            var click = new ClickEvent(ClickEvent.Action.OPEN_URL, url);
            link.setStyle(link.getStyle().withClickEvent(click).withUnderlined(true).withColor(TextColor.fromLegacyFormat(ChatFormatting.BLUE)));
            if (chat == null) chat = Component.literal("");
            chat.append(link);
        }

        var end = string.substring(lastEnd);
        if (chat == null) chat = Component.literal(end);
        else if (!end.isEmpty()) chat.append(Component.literal(string.substring(lastEnd)));
        return chat;
    }
}
