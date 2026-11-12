package analise.aprendizagem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Uma avaliacao completa do CodeRunner de uma submissao.
public class AvaliacaoCodeRunner {
    private final StatusCodeRunner status;
    private final int errorcount;
    private final String mensagemErro;
    private final Double pontosObtidos;
    private final Double pontosPossiveis;
    private final boolean precheck;
    private final List<ResultadoTeste> testes;

    public AvaliacaoCodeRunner(StatusCodeRunner status, int errorcount, String mensagemErro,
                               Double pontosObtidos, Double pontosPossiveis,
                               boolean precheck, List<ResultadoTeste> testes) {
        this.status = status;
        this.errorcount = errorcount;
        this.mensagemErro = mensagemErro;
        this.pontosObtidos = pontosObtidos;
        this.pontosPossiveis = pontosPossiveis;
        this.precheck = precheck;
        this.testes = Collections.unmodifiableList(new ArrayList<>(testes));
    }
    public StatusCodeRunner getStatus() { return status; }
    public int getErrorcount() { return errorcount; }
    public String getMensagemErro() { return mensagemErro; }
    public Double getPontosObtidos() { return pontosObtidos; }
    public Double getPontosPossiveis() { return pontosPossiveis; }
    public boolean isPrecheck() { return precheck; }
    public List<ResultadoTeste> getTestes() { return testes; }
    public int getQuantidadeTestes() { return testes.size(); }
    public long getQuantidadeAprovados() {
        return testes.stream().filter(ResultadoTeste::isCorreto).count();
    }
    public double getProporcao() {
        if (pontosObtidos == null || pontosPossiveis == null || pontosPossiveis <= 0)
            return Double.NaN;
        return pontosObtidos / pontosPossiveis;
    }
    public boolean foiAceite() {
        return status == StatusCodeRunner.VALIDO &&
               !Double.isNaN(getProporcao()) && getProporcao() >= 0.999999;
    }
    public boolean avaliacaoDoEstudanteValida() {
        if (precheck) return false;
        if (status == StatusCodeRunner.ERRO_SINTAXE) return true;
        return status == StatusCodeRunner.VALIDO && !Double.isNaN(getProporcao());
    }
}
