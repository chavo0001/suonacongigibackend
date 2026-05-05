package it.generation.suonacongigi.service;

import it.generation.suonacongigi.repository.ParolaBanditaRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CensuraService {

    private final ParolaBanditaRepository parolaBanditaRepository;
    private final List<Map.Entry<String, Pattern>> patternCache = new ArrayList<>();

    @PostConstruct
    public void inizializzaCache() {
        List<String> parole = parolaBanditaRepository.findAllParoleAttive();
        for (String parola : parole) {
            String separatore = "[\\s.\\-_*|]*";
            StringBuilder regexBuilder = new StringBuilder();
            for (char c : parola.toCharArray()) {
                regexBuilder.append(Pattern.quote(String.valueOf(c)));
                regexBuilder.append(separatore);
            }
            String regex = regexBuilder.substring(0, regexBuilder.length() - separatore.length());
            Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            patternCache.add(new AbstractMap.SimpleEntry<>(parola, p));
        }
    }

    public String filtra(String testo) {
        if (testo == null || testo.isBlank()) {
            return "";
        }

        String risultato = testo;

        for (Map.Entry<String, Pattern> entry : patternCache) {
            Pattern p = entry.getValue();
            Matcher m = p.matcher(risultato);
            StringBuilder sb = new StringBuilder();

            while (m.find()) {
                String match = m.group();
                int len = (match != null) ? match.length() : 0;
                m.appendReplacement(sb, Matcher.quoteReplacement("*".repeat(len)));
            }
            m.appendTail(sb);

            risultato = Objects.requireNonNull(sb.toString());
        }

        return Objects.requireNonNull(risultato);
    }

    public String filtraSeNecessario(String testo, boolean censuraAttiva) {
        if (!censuraAttiva) {
            return Objects.requireNonNull(testo != null ? testo : "");
        }
        return filtra(testo);
    }
}
