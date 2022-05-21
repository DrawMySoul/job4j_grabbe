package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HarbCareerDateTimeParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HabrCareerParse implements Parse {
    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    @Override
    public List<Post> list(String link) {
        List<Post> posts = new ArrayList<>();
        for (int page = 1; page < 6; page++) {
            try {
                Connection connection = Jsoup.connect(link + "?page=" + page);
                Document document = connection.get();
                Elements rows = document.select(".vacancy-card__inner");
                rows.forEach(row -> {
                    Element titleElement = row.select(".vacancy-card__title").first();
                    Element linkElement = titleElement.child(0);
                    String vacancyName = titleElement.text();
                    String vacancyLink = String.format("%s%s", getSourceLink(link), linkElement.attr("href"));
                    Element dateElement = row.select(".vacancy-card__date").first();
                    String date = dateElement.child(0).attr("datetime");
                    posts.add(new Post(vacancyName, vacancyLink, retrieveDescription(vacancyLink), dateTimeParser.parse(date)));
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return posts;
    }

    private String retrieveDescription(String link) {
        String description = null;
        try {
            Document document = Jsoup.connect(link).get();
            description = document.select(".style-ugc").text();
        } catch (IOException e) {
            e.printStackTrace();
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

    public static void main(String[] args) {
        HabrCareerParse h = new HabrCareerParse(new HarbCareerDateTimeParser());
        h.list("https://career.habr.com/vacancies/java_developer");
    }
}
