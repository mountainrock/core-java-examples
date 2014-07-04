package util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 	<dependency>
  <groupId>org.jsoup</groupId>
  <artifactId>jsoup</artifactId>
  <version>1.7.3</version>
</dependency>
 */
public class JsoupWebScraper {
	private static final String NEXT = "Next";
	static String host = "http://www.indiabix.com";
	static String path = "/technical/core-java/";

	public static void main(String[] args) throws Exception {

		System.setProperty("http.proxyHost", "emeaproxy.jpmchase.net");
		System.setProperty("http.proxyPort", "8080");

		String url = host + path;
		process(url);


	}

	private static Document process(String url) throws Exception {
		Document doc = Jsoup.connect(url).get();
		String title = doc.title();
		System.out.println(title);
		extractQA(doc);
		Elements select = doc.select("a");
		for (Element anchor : select) {
			if (anchor.text().contains(NEXT)) {
				url = host + anchor.attr("href");
				System.out.println(url);
				process(url);
			}
		}

		return doc;
	}

	private static void extractQA(Document doc) {
		Elements questions = doc.getElementsByClass("tech-question");
		Elements answers = doc.getElementsByClass("tech-answer");
		for (int i = 0; i < questions.size(); i++) {
			System.out.println(questions.get(i).text() + "\r\n" + answers.get(i).text());
		}
	}

}
