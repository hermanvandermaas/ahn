package nl.waywayway.ahn;

import com.github.mikephil.charting.data.Entry;
import com.google.android.gms.maps.model.LatLng;

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
    private String shortTitle;
    private List<LatLng> pointsList;
    private final String decimalSeparator = getDecimalSeparator();
    // Scheidingsteken is een puntkomma als decimaalscheiding een komma is, t.b.v. inlezen in Excel
    private final String separator = (decimalSeparator.equals(",")) ? ";" : ",";
    private final boolean useQuotes = true;
    private final String quoteCharacter = "\"";
    private final String endOfLine = "\r\n";

    private CsvStringMaker(List<Entry> rowsList, List<String> columnLabels, String shortTitle, List<LatLng> pointsList) {
        this.rowsList = rowsList;
        this.columnLabels = columnLabels;
        this.shortTitle = shortTitle;
        this.pointsList = pointsList;
    }

    public static CsvStringMaker getInstance(List<Entry> rowsList, List<String> columnLabels, String shortTitle, List<LatLng> pointsList) {
        return new CsvStringMaker(rowsList, columnLabels, shortTitle, pointsList);
    }

    public String getCsvString() {
        StringBuilder csvStringbuilder = new StringBuilder();

        // Invoegen benaming databestand
        if (useQuotes) csvStringbuilder.append(quoteCharacter);
        csvStringbuilder.append(shortTitle);
        if (useQuotes) csvStringbuilder.append(quoteCharacter);
        csvStringbuilder.append(endOfLine);
        csvStringbuilder.append(endOfLine);

        // Maak kolom labels
        for (String label : columnLabels) {
            if (useQuotes) csvStringbuilder.append(quoteCharacter);
            csvStringbuilder.append(label);
            if (useQuotes) csvStringbuilder.append(quoteCharacter);
            csvStringbuilder.append(separator);
        }

        // Verwijder scheidingsteken op einde van de regel
        csvStringbuilder.setLength(csvStringbuilder.length() - 1);
        csvStringbuilder.append(endOfLine);

        int i = 0;

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

            csvStringbuilder.append(separator);

            if (useQuotes) csvStringbuilder.append(quoteCharacter);
            String longitude = localeNumber(String.valueOf(pointsList.get(i).longitude));
            csvStringbuilder.append(longitude);
            if (useQuotes) csvStringbuilder.append(quoteCharacter);

            csvStringbuilder.append(separator);

            if (useQuotes) csvStringbuilder.append(quoteCharacter);
            String latitude = localeNumber(String.valueOf(pointsList.get(i).latitude));
            csvStringbuilder.append(latitude);
            if (useQuotes) csvStringbuilder.append(quoteCharacter);
            csvStringbuilder.append(endOfLine);

            i++;
        }

        return csvStringbuilder.toString();
    }

    private String formatFloat(Float floatNumber) {
        String format = "%" + decimalSeparator + "2f";
        return String.format(Locale.getDefault(), format, floatNumber);
    }

    private String localeNumber(String numberString) {
        return numberString.replace(".", decimalSeparator);
    }

    private String getDecimalSeparator() {
        DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance();
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        return String.valueOf(symbols.getDecimalSeparator());
    }
}