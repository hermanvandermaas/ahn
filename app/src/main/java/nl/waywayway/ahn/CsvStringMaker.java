package nl.waywayway.ahn;

import com.github.mikephil.charting.data.Entry;

import java.util.List;

// Maakt van een List een String om naar een .csv bestand te schrijven
// De List bevat meerdere Lists, die elk een kolom in een tabel vertegenwoordigen
// De class verwacht een aparte List met de tekstlabels voor elk van de kolommen

public class CsvStringMaker {
    private Entry columnLabels;
    private List<Entry> rowsList;
    private final String separator = ";";
    private final boolean useQuotes = true;
    private final String quoteCharacter = "\"";
    private final String endOfLine = "\r\n";

    private CsvStringMaker(List<Entry> rowsList, Entry columnLabels) {
        this.rowsList = rowsList;
        this.columnLabels = columnLabels;
    }

    public CsvStringMaker getInstance(List<Entry> rowsList, Entry columnLabels) {
        return new CsvStringMaker(rowsList, columnLabels);
    }

    public String getCsvString() {
        // Voeg rij labels als eerste item toe aan de data List
        rowsList.add(0, columnLabels);

        StringBuilder csvStringbuilder = new StringBuilder();

        // Maak regels volgens .csv formaat
        for (Entry entry : rowsList) {
            if (useQuotes) csvStringbuilder.append(quoteCharacter);
            String x = String.valueOf(entry.getX());
            csvStringbuilder.append(x);
            if (useQuotes) csvStringbuilder.append(quoteCharacter);

            csvStringbuilder.append(separator);

            if (useQuotes) csvStringbuilder.append(quoteCharacter);
            String y = String.valueOf(entry.getY());
            csvStringbuilder.append(y);
            if (useQuotes) csvStringbuilder.append(quoteCharacter);
            csvStringbuilder.append(endOfLine);
        }

        return csvStringbuilder.toString();
    }
}