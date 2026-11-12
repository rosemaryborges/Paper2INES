package analise.processamento;

import analise.aprendizagem.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

// Le dados PHP serializados sem executar PHP e sem guardar credenciais do sandbox.
// A leitura usa o comprimento em BYTES, como exige o formato PHP.
public class LeitorTestOutcome {
    public AvaliacaoCodeRunner ler(String texto) {
        if (texto == null || texto.isBlank()) return null;
        try {
            Parser parser = new Parser(texto);
            Map<String,Object> raiz = mapa(parser.lerValor());
            int status = inteiro(raiz.get("status"), -1);
            int quantidadeErros = inteiro(raiz.get("errorcount"), -1);
            String mensagem = string(raiz.get("errormessage"));
            Double obtidos = numero(raiz.get("actualmark"));
            Double possiveis = numero(raiz.get("maxpossmark"));
            boolean precheck = Boolean.TRUE.equals(raiz.get("isprecheck"));
            List<ResultadoTeste> testes = new ArrayList<>();
            Object resultado = raiz.get("testresults");
            if (resultado instanceof Map<?,?> lista) {
                for (Object valor : lista.values()) {
                    Map<String,Object> t = mapa(valor);
                    testes.add(new ResultadoTeste(

                    	    string(t.get("testcode")),
                    	    string(t.get("stdin")),
                    	    string(t.get("expected")),
                    	    string(t.get("got")),

                    	    // Novos atributos
                    	    string(t.get("stderr")),
                    	    string(t.get("stdout")),

                    	    Boolean.TRUE.equals(t.get("iscorrect")),
                    	    valorOuZero(t.get("mark")),
                    	    valorOuZero(t.get("awarded"))

                    	));
                }
            }
            return new AvaliacaoCodeRunner(StatusCodeRunner.deCodigo(status),
                quantidadeErros, mensagem, obtidos, possiveis, precheck, testes);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("_testoutcome invalido: " + e.getMessage(), e);
        }
    }
    @SuppressWarnings("unchecked")
    private static Map<String,Object> mapa(Object x) {
        if (x instanceof Map<?,?>) return (Map<String,Object>) x;
        return Collections.emptyMap();
    }
    private static String string(Object x) { return x == null ? "" : x.toString(); }
    private static Double numero(Object x) {
        if (x instanceof Number n) return n.doubleValue();
        try { return x == null ? null : Double.parseDouble(x.toString()); }
        catch (NumberFormatException e) { return null; }
    }
    private static double valorOuZero(Object x) { Double n = numero(x); return n == null ? 0.0 : n; }
    private static int inteiro(Object x, int padrao) { Double n = numero(x); return n == null ? padrao : n.intValue(); }

    private static class Parser {
        private final byte[] bytes;
        private int pos = 0;
        Parser(String entrada) { bytes = entrada.getBytes(StandardCharsets.UTF_8); }
        private int lerByte() {
            if (pos >= bytes.length) throw new IllegalArgumentException("fim inesperado");
            return bytes[pos++] & 0xff;
        }
        private void espera(char c) {
            if (lerByte() != c) throw new IllegalArgumentException("esperado: " + c + " na posicao " + pos);
        }
        private String ate(char c) {
            int inicio = pos;
            while (pos < bytes.length && (bytes[pos] & 0xff) != c) pos++;
            if (pos >= bytes.length) throw new IllegalArgumentException("separador ausente");
            String s = new String(bytes, inicio, pos - inicio, StandardCharsets.US_ASCII);
            pos++;
            return s;
        }
        private String texto(int tamanho) {
            if (tamanho < 0 || pos + tamanho > bytes.length)
                throw new IllegalArgumentException("tamanho PHP invalido");
            String s = new String(bytes, pos, tamanho, StandardCharsets.UTF_8);
            pos += tamanho;
            return s;
        }
        Object lerValor() {
            char tipo = (char) lerByte();
            if (tipo == 'N') { espera(';'); return null; }
            espera(':');
            switch (tipo) {
                case 'N': espera(';'); return null;
                case 'b': return ate(';').equals("1");
                case 'i': return Long.parseLong(ate(';'));
                case 'd': return Double.parseDouble(ate(';'));
                case 's': {
                    int tamanho = Integer.parseInt(ate(':'));
                    espera('"');
                    String s = texto(tamanho);
                    espera('"'); espera(';');
                    return s;
                }
                case 'a': {
                    int quantidade = Integer.parseInt(ate(':'));
                    espera('{');
                    Map<String,Object> mapa = new LinkedHashMap<>();
                    for (int i = 0; i < quantidade; i++) {
                        Object chave = lerValor();
                        Object valor = lerValor();
                        mapa.put(String.valueOf(chave), valor);
                    }
                    espera('}'); return mapa;
                }
                case 'O': {
                    int tamanho = Integer.parseInt(ate(':'));
                    espera('"'); texto(tamanho); espera('"'); espera(':');
                    int quantidade = Integer.parseInt(ate(':'));
                    espera('{');
                    Map<String,Object> mapa = new LinkedHashMap<>();
                    for (int i = 0; i < quantidade; i++) {
                        Object chave = lerValor();
                        Object valor = lerValor();
                        mapa.put(String.valueOf(chave), valor);
                    }
                    espera('}'); return mapa;
                }
                default: throw new IllegalArgumentException("tipo PHP nao suportado: " + tipo);
            }
        }
    }
}
