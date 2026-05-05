package it.generation.suonacongigi.service;

import it.generation.suonacongigi.repository.ParolaBanditaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
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
    private boolean cacheInizializzata = false;

    public synchronized void inizializzaCache() {
        if (cacheInizializzata) {
            return;
        }

        try {
            List<String> parole = parolaBanditaRepository.findAllParoleAttive();
            patternCache.clear();
            for (String parola : parole) {
                patternCache.add(new AbstractMap.SimpleEntry<>(parola, creaPattern(parola)));
            }
            cacheInizializzata = true;
        } catch (DataAccessException e) {
            patternCache.clear();
        }
    }

    private Pattern creaPattern(String parola) {
        String separatore = "[\\s.\\-_*|]*";
        StringBuilder regexBuilder = new StringBuilder();
        for (char c : parola.toCharArray()) {
            regexBuilder.append(Pattern.quote(String.valueOf(c)));
            regexBuilder.append(separatore);
        }
        String regex = regexBuilder.substring(0, regexBuilder.length() - separatore.length());
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }

    public String filtra(String testo) {
        if (testo == null || testo.isBlank()) {
            return "";
        }

        inizializzaCache();
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
