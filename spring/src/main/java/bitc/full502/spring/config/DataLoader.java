package bitc.full502.spring.config;

import bitc.full502.spring.domain.entity.Flight;
import bitc.full502.spring.domain.entity.Lodging;
import bitc.full502.spring.domain.repository.FlightRepository;
import bitc.full502.spring.domain.repository.LodgingRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;

@Configuration
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final FlightRepository flightRepository;
    private final LodgingRepository lodgingRepository;

    @Override
    public void run(String... args) {
        loadFlights("flight_data.csv");
        loadLodgings("lodging_data.csv");
        System.out.println("CSV 데이터 로드 완료 ✅");
    }


    private void loadFlights(String fileName) {
        try (CSVReader reader = new CSVReader(
                Files.newBufferedReader(
                        Paths.get(new ClassPathResource(fileName).getURI()),
                        StandardCharsets.UTF_8))) {

            String[] row;
            boolean header = true;
            while ((row = reader.readNext()) != null) {
                if (header) { header = false; continue; }
                if (row.length < 8) continue;

                Flight f = Flight.builder()
                        .type(trim(row[0]))
                        .airline(trim(row[1]))
                        .flNo(trim(row[2]))
                        .dep(trim(row[3]))
                        .arr(trim(row[4]))
                        .depTime(parseTime(row[5]))
                        .arrTime(parseTime(row[6]))
                        .days(trim(row[7]))
                        .totalSeat(20)
                        .build();

                flightRepository.save(f);
            }
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException("Flight CSV 로딩 실패: " + fileName, e);
        }
    }

    private void loadLodgings(String fileName) {
        try (CSVReader reader = new CSVReader(
                Files.newBufferedReader(
                        Paths.get(new ClassPathResource(fileName).getURI()),
                        StandardCharsets.UTF_8))) {

            String[] row;
            boolean header = true;
            while ((row = reader.readNext()) != null) {
                if (header) { header = false; continue; }
                if (row.length < 11) continue;

                Lodging l = Lodging.builder()
                        .city(trim(row[1]))
                        .town(trim(row[2]))
                        .vill(trim(row[3]))
                        .name(trim(row[4]))
                        .phone(trim(row[6]))
                        .addrRd(trim(row[7]))
                        .addrJb(trim(row[8]))
                        .lat(parseDouble(row[9]))
                        .lon(parseDouble(row[10]))
                        .totalRoom(3)
                        .img(null)
                        .build();

                lodgingRepository.save(l);
            }
        } catch (IOException | CsvValidationException e) {
            throw new RuntimeException("Lodging CSV 로딩 실패: " + fileName, e);
        }
    }

    private static String trim(String v) {
        return v == null ? null : v.trim();
    }

    private static LocalTime parseTime(String s) {
        if (s == null || s.isBlank()) return null;
        return LocalTime.parse(s.trim()); // "06:30" 형식
    }

    private static Double parseDouble(String s) {
        try { return (s == null || s.isBlank()) ? null : Double.valueOf(s.trim()); }
        catch (NumberFormatException e) { return null; }
    }
}
