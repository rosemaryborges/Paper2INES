package analise.processamento;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Um unico escritor de CSV, incluindo escape de codigos Java multilinha. */
public final class CSV {
    private CSV() {}
    public static void escrever(Path destino, List<String[]> linhas) throws IOException {
        Files.createDirectories(destino.toAbsolutePath().getParent());
        try (BufferedWriter out = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
            for (String[] linha : linhas) {
                for (int i=0; i<linha.length; i++) {
                    if (i>0) out.write(',');
                    String valor = linha[i] == null ? "" : linha[i];
                    out.write('"'); out.write(valor.replace("\"", "\"\"")); out.write('"');
                }
                out.newLine();
            }
        }
    }
    public static String numero(double n) { return Double.isFinite(n) ? String.format(Locale.US,"%.6f",n) : ""; }
    public static String inteiro(Integer n) { return n==null ? "" : n.toString(); }
    public static String[] cabecalho(String... nomes) { return nomes; }
}
