package judge.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HtmlHandleUtil {

	private static final String[][] pairs = new String[][] {
		{ "img", "src" },
		{ "script", "src" },
		{ "link", "href" },
		{ "a", "href" }
	};

	public static String transformUrlToAbs(String html, String baseUri) {
		Document doc = Jsoup.parse(html, baseUri);
		for (String[] pair : pairs) {
			Elements links = doc.select(pair[0]);
			for (Element element : links) {
				element.attr(pair[1], element.absUrl(pair[1]));
			}
		}
		return doc.toString()
				.replaceAll(">\\s*<", "><")
				.replaceAll("(?<=\\w)\\s+<(?!/pre)", " <")
				.replaceAll("(?<!pre)>\\s+(?=\\w)", "> ");
	}

	public static List<String> getStyleSheet(String html) {
		List<String> result = new ArrayList<String>();
		Matcher matcher = Pattern.compile("(?i)<link[^<>]*text/css[^<>]*/>|<style[^<>]*>[\\s\\S]*?</style>").matcher(html);
		while (matcher.find()) {
			result.add(matcher.group());
		}
		return result;
	}

	public static void main(String[] args) {
		String s = "<br>\n45";
		System.out.println(s.replaceAll("(?<!pre)>\\s* \\s*(?=\\w)", "> "));
	}

}
