
package analise.aprendizagem;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Representa o resultado de UM teste do CodeRunner.
public class ResultadoTeste {

    private final String nome;
    private final String entrada;
    private final String esperado;
    private final String obtido;

    private final String stderr;
    private final String stdout;
    private final String exceptionType;

    private final boolean correto;
    private final double pontosPossiveis;
    private final double pontosObtidos;

    // Construtor original
    public ResultadoTeste(
            String nome,
            String entrada,
            String esperado,
            String obtido,
            boolean correto,
            double pontosPossiveis,
            double pontosObtidos) {

        this(nome, entrada, esperado, obtido,
             "", "",
             correto, pontosPossiveis, pontosObtidos);
    }

    // Construtor com informacoes de execucao
    public ResultadoTeste(
            String nome,
            String entrada,
            String esperado,
            String obtido,
            String stderr,
            String stdout,
            boolean correto,
            double pontosPossiveis,
            double pontosObtidos) {

        this.nome = nome;
        this.entrada = entrada;
        this.esperado = esperado;
        this.obtido = obtido;
        this.stderr = stderr == null ? "" : stderr;
        this.stdout = stdout == null ? "" : stdout;

        this.correto = correto;
        this.pontosPossiveis = pontosPossiveis;
        this.pontosObtidos = pontosObtidos;

        // Identificar excecoes automaticamente
        this.exceptionType = identificarExcecao(
            this.stderr + "\n" + this.obtido
        );
    }

    // Procura o nome de uma excecao Java
    private String identificarExcecao(String texto) {

        Pattern padrao = Pattern.compile(
            "(?:[\\w$]+\\.)*([\\w$]+(?:Exception|Error))\\b"
        );

        Matcher resultado = padrao.matcher(texto);

        if (resultado.find()) {
            return resultado.group(1);
        }

        return "";
    }

    public String getNome() {
        return nome;
    }

    public String getEntrada() {
        return entrada;
    }

    public String getEsperado() {
        return esperado;
    }

    public String getObtido() {
        return obtido;
    }

    public String getStderr() {
        return stderr;
    }

    public String getStdout() {
        return stdout;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public boolean temExcecao() {
        return !exceptionType.isEmpty();
    }

    public boolean isCorreto() {
        return correto;
    }

    public double getPontosPossiveis() {
        return pontosPossiveis;
    }

    public double getPontosObtidos() {
        return pontosObtidos;
    }
}
