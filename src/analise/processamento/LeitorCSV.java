package analise.processamento;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

// Faz a leitura do .csv com virgulas, aspas e codigos que ocupam varias linhas
public class LeitorCSV {
    public List<Map<String,String>> ler(String caminho) throws IOException {
        List<Map<String,String>> linhas = new ArrayList<>();
        try (PushbackReader leitor = new PushbackReader(
                Files.newBufferedReader(Path.of(caminho), StandardCharsets.UTF_8), 1)) {
            List<String> cabecalho = lerRegistro(leitor);
            if (cabecalho == null) throw new IOException("CSV vazio");
            cabecalho.set(0, cabecalho.get(0).replace("\uFEFF", ""));
            List<String> valores;
            while ((valores = lerRegistro(leitor)) != null) {
                if (valores.size() != cabecalho.size())
                    throw new IOException("Número diferente de colunas numa linha do CSV");
                Map<String,String> linha = new LinkedHashMap<>();
                for (int i=0; i<cabecalho.size(); i++) linha.put(cabecalho.get(i), valores.get(i));
                linhas.add(linha);
            }
        }
        return linhas;
    }
    private List<String> lerRegistro(PushbackReader leitor) throws IOException {
        List<String> valores = new ArrayList<>();
        StringBuilder campo = new StringBuilder();
        boolean entreAspas = false, leuAlgo = false;
        int codigo;
        while ((codigo = leitor.read()) != -1) {
            leuAlgo = true;
            char c = (char) codigo;
            if (c == '"') {
                if (entreAspas) {
                    int seguinte = leitor.read();
                    if (seguinte == '"') campo.append('"');
                    else { entreAspas = false; if (seguinte != -1) leitor.unread(seguinte); }
                } else if (campo.length() == 0) entreAspas = true;
                else campo.append(c);
            } else if (c == ',' && !entreAspas) {
                valores.add(campo.toString()); campo.setLength(0);
            } else if ((c == '\n' || c == '\r') && !entreAspas) {
                if (c == '\r') { int seguinte = leitor.read(); if (seguinte != '\n' && seguinte != -1) leitor.unread(seguinte); }
                valores.add(campo.toString()); return valores;
            } else campo.append(c);
        }
        if (entreAspas) throw new IOException("Aspas não fechadas no CSV");
        if (!leuAlgo) return null;
        valores.add(campo.toString()); return valores;
    }
}