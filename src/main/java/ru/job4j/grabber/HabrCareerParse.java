package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.grabber.utils.DateTimeParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HabrCareerParse implements Parse {
    private static final int PAGE_LIMIT = 1;
    private final DateTimeParser dateTimeParser;
    private static final Logger LOG = LoggerFactory.getLogger(HabrCareerParse.class.getName());

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    @Override
    public List<Post> list(String link) {
        List<Post> posts = new ArrayList<>();
        for (int page = 1; page <= PAGE_LIMIT; page++) {
            try {
                Connection connection = Jsoup.connect(link + "?page=" + page);
                Document document = connection.get();
                Elements rows = document.select(".vacancy-card__inner");
                rows.forEach(row -> posts.add(vacancyParse(row)));
            } catch (IOException e) {
                LOG.error(e.getMessage());
            }
        }
        return posts;
    }

    private Post vacancyParse(Element element) {
        Element titleElement = element.select(".vacancy-card__title").first();
        Element linkElement = titleElement.child(0);
        String vacancyName = titleElement.text();
        String vacancyLink = String.format("%s%s", getSourceLink(element.baseUri()), linkElement.attr("href"));
        Element dateElement = element.select(".vacancy-card__date").first();
        String date = dateElement.child(0).attr("datetime");
        return new Post(vacancyName, vacancyLink, retrieveDescription(vacancyLink), dateTimeParser.parse(date));
    }

    private String retrieveDescription(String link) {
        String description = null;
        try {
            Document document = Jsoup.connect(link).get();
            description = document.select(".style-ugc").text();
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        return description;
    }

    private String getSourceLink(String link) {
        String sourceLink = null;
        Pattern pattern = Pattern.compile("http.+com");
        Matcher matcher = pattern.matcher(link);
        if (matcher.find()) {
            sourceLink = matcher.group();
        }
        return sourceLink;
    }
}
