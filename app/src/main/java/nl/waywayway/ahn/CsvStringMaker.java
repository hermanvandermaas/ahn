package nl.waywayway.ahn;

import com.github.mikephil.charting.data.Entry;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

// Maakt van een List een String om naar een .csv bestand te schrijven
// De List bevat Entry objecten: telkens een x, y paar, voor een x- en y-kolom van een tabel
// De class verwacht een aparte List met de tekstlabels voor elk van de kolommen

public class CsvStringMaker {
    private List<String> columnLabels;
    private List<Entry> rowsList;
    private final String decimalSeparator = getDecimalSeparator();
    // Scheidingsteken is een puntkomma als decimaalscheiding een komma is, t.b.v. inlezen in Excel
    private final String separator = (decimalSeparator.equals(",")) ? ";" : ",";
    private final boolean useQuotes = true;
    private final String quoteCharacter = "\"";
    private final String endOfLine = "\r\n";

    private CsvStringMaker(List<Entry> rowsList, List<String> columnLabels) {
        this.rowsList = rowsList;
        this.columnLabels = columnLabels;
    }

    public static CsvStringMaker getInstance(List<Entry> rowsList, List<String> columnLabels) {
        return new CsvStringMaker(rowsList, columnLabels);
    }

    public String getCsvString() {
        StringBuilder csvStringbuilder = new StringBuilder();

        // Maak kolom labels
        for (String label : columnLabels) {
            if (useQuotes) csvStringbuilder.append(quoteCharacter);
            csvStringbuilder.append(label);
            if (useQuotes) csvStringbuilder.append(quoteCharacter);
            csvStringbuilder.append(endOfLine);
        }

        // Maak regels volgens .csv formaat
        for (Entry entry : rowsList) {
            if (useQuotes) csvStringbuilder.append(quoteCharacter);
            String x = formatFloat(entry.getX());
            csvStringbuilder.append(x);
            if (useQuotes) csvStringbuilder.append(quoteCharacter);

            csvStringbuilder.append(separator);

            if (useQuotes) csvStringbuilder.append(quoteCharacter);
            String y = formatFloat(entry.getY());
            csvStringbuilder.append(y);
            if (useQuotes) csvStringbuilder.append(quoteCharacter);
            csvStringbuilder.append(endOfLine);
        }

        return csvStringbuilder.toString();
    }

    private String formatFloat(Float floatNumber) {
        return String.format(Locale.getDefault(), "%.2f", floatNumber);
    }

    private String getDecimalSeparator() {
        DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance();
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        return String.valueOf(symbols.getDecimalSeparator());
    }
}