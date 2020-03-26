package com.information.coronavirustracker.services;

import com.information.coronavirustracker.models.LocationStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * creating HTTP request
 */
@Service
public class CoronaVirusDataService {
    private static String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";

    private List<LocationStats> allStats = new ArrayList<>();

    public List<LocationStats> getAllStats() {
        return allStats;
    }

    @PostConstruct // когда бин будет сконструирован, выполнить данный метод
    @Scheduled(cron = "* * * 1 * *") // делать запрос каждый день на сайт за данными. сек.мин.час.день.мес.год
   public void fetchVirusData() throws IOException, InterruptedException {
       List<LocationStats> newStats = new ArrayList<>();
       HttpClient client = HttpClient.newHttpClient();
       HttpRequest request = HttpRequest.newBuilder()
               .uri(URI.create(VIRUS_DATA_URL))
               .build();
       HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
       StringReader csvBodyReader = new StringReader(httpResponse.body());
       Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);
        for (CSVRecord record : records) {
            LocationStats locationStats = new LocationStats();
            locationStats.setState(record.get("Province/State"));
            locationStats.setCountry(record.get("Country/Region"));
            int latestCases = Integer.parseInt(record.get(record.size() - 1)); // каждый день будет новая колонка с данными. И колонка должна быть в формате String. Указываем номер из какой колонки брать инфо.
            int previousDayCases = Integer.parseInt(record.get(record.size() - 2));
            locationStats.setLatestTotalCases(latestCases);
            locationStats.setDiffFromPrevDay(latestCases-previousDayCases);
            newStats.add(locationStats);
        }
        this.allStats = newStats;
   }

}
