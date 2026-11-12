
package analise.aprendizagem;

import java.util.*;
import analise.codigo.*;

public class Revisao {

    // Resultado da comparacao de um teste
    public enum MudancaTeste {
        ERRO_PERSISTENTE,
        ERRO_RESOLVIDO,
        NOVO_ERRO,
        ACERTO_MANTIDO
    }

    // Guarda o resultado da comparacao
    public record ComparacaoTeste(
            String identificador,
            ResultadoTeste antes,
            ResultadoTeste depois,
            MudancaTeste mudanca,
            boolean saidaMudou) {
    }

    private final Submissao anterior;
    private final Submissao atual;
    private final ResultadoComparacaoCodigo comparacaoCodigo;
    private final ClassificadorRevisao.Tipo tipoRevisao;
    private static final ComparadorCodigo COMPARADOR = new ComparadorCodigo();
    private static final ClassificadorRevisao CLASSIFICADOR = new ClassificadorRevisao();

    public Revisao(Submissao anterior, Submissao atual) {

        this.anterior = Objects.requireNonNull(anterior);
        this.atual = Objects.requireNonNull(atual);
        if (anterior.getTurma() != atual.getTurma()
                || anterior.getAlunoId() != atual.getAlunoId()
                || anterior.getQuestaoId() != atual.getQuestaoId()
                || anterior.getQuizId() != atual.getQuizId()
                || anterior.getQuestionAttemptId() != atual.getQuestionAttemptId()) {

            throw new IllegalArgumentException(
                "As submissoes devem pertencer ao mesmo aluno, turma e questao."
            );
        }
        if (atual.getMomento() < anterior.getMomento()) {
            throw new IllegalArgumentException("Revisao com cronologia invertida.");
        }
        this.comparacaoCodigo = COMPARADOR.comparar(anterior.getCodigo(), atual.getCodigo());
        this.tipoRevisao = CLASSIFICADOR.classificar(comparacaoCodigo);
    }

    // A gap may be caused by an administrative or excluded step. It is a review flag,
    // not automatic proof of a missing valid student submission.
    public boolean temLacunaSequencia() {
        return anterior.getSequenceNumber() > 0 && atual.getSequenceNumber() > 0
            && atual.getSequenceNumber() - anterior.getSequenceNumber() > 1;
    }
    public boolean mudouSubcategoriaErro() {
        return !Objects.equals(anterior.getErro().getSubcategoria(), atual.getErro().getSubcategoria());
    }
    public boolean mudouCategoriaErro() {
        return !Objects.equals(anterior.getErro().getCategoria(), atual.getErro().getCategoria());
    }
    public boolean corrigiuNestaRevisao() { return atual.estaCorreta(); }
    public boolean persistiramTestesFalhados() {
        return testesComparaveis() && contar(MudancaTeste.ERRO_PERSISTENTE) > 0;
    }
    public long contarTestesFalhadosComSaidaAlterada() {
        return getComparacoesTestes().stream().filter(t ->
            t.mudanca() == MudancaTeste.ERRO_PERSISTENTE && t.saidaMudou()).count();
    }

    public ResultadoComparacaoCodigo getComparacaoCodigo() { return comparacaoCodigo; }
    public String getTipoRevisao() { return tipoRevisao.name(); }
    public double getMagnitudeRevisao() { return comparacaoCodigo.magnitude(); }
    public double getSimilaridadeCodigo() { return comparacaoCodigo.similaridade(); }
    public boolean requerRevisaoManual() { return CLASSIFICADOR.requerRevisaoManual(comparacaoCodigo,tipoRevisao); }
    public String getResultadoRevisao() {
        if (atual.estaCorreta()) return "CORRECAO_COMPLETA";
        if (getComportamento().equals("PASSOU_A_EXECUTAR") ||
            getDiferencaPontuacao() > 0.000001) return "PROGRESSO_PARCIAL";
        if (getComportamento().equals("REGRESSAO_COMPILACAO") ||
            getDiferencaPontuacao() < -0.000001) return "REGRESSAO";
        if (Double.isNaN(getDiferencaPontuacao())) return "INDETERMINADO";
        return "PERSISTENCIA_SEM_MELHORIA"; // technical score did not improve
    }
    public Submissao getAnterior() {
        return anterior;
    }

    public Submissao getAtual() {
        return atual;
    }

    public double getIntervaloMinutos() {
        return (atual.getMomento()
                - anterior.getMomento()) / 60.0;
    }

    public double getDiferencaPontuacao() {
        return atual.getPontuacao()
                - anterior.getPontuacao();
    }

    public boolean mudouCodigo() {
        return !Objects.equals(
            anterior.getCodigo(),
            atual.getCodigo()
        );
    }

    public String getTransicao() {
        return anterior.getErro().getCategoria()
                + " -> "
                + atual.getErro().getCategoria();
    }

    public int getDiferencaTestesAprovados() {
        return (int) (
            atual.getAvaliacao().getQuantidadeAprovados()
            - anterior.getAvaliacao().getQuantidadeAprovados()
        );
    }

    public String getComportamento() {

        if (atual.estaCorreta()) {
            return "CORRIGIU";
        }

        if (getDiferencaPontuacao() > 0.000001) {
            return "MELHOROU_TESTES";
        }

        if (getDiferencaPontuacao() < -0.000001) {
            return "PIOROU_TESTES";
        }

        if (anterior.getAvaliacao().getStatus()
                == StatusCodeRunner.ERRO_SINTAXE
                && atual.getAvaliacao().getStatus()
                == StatusCodeRunner.VALIDO) {

            return "PASSOU_A_EXECUTAR";
        }

        if (atual.getAvaliacao().getStatus()
                == StatusCodeRunner.ERRO_SINTAXE
                && anterior.getAvaliacao().getStatus()
                == StatusCodeRunner.VALIDO) {

            return "REGRESSAO_COMPILACAO";
        }

        if (!mudouCodigo()) {
            return "CODIGO_IGUAL";
        }

        return "ALTEROU_CODIGO_SEM_GANHO_MENSURAVEL";
    }

    // -------------------------------------------------
    // VERIFICAR SE OS TESTES PODEM SER COMPARADOS
    // -------------------------------------------------

    public boolean testesComparaveis() {

        if (anterior.getAvaliacao().getStatus()
                != StatusCodeRunner.VALIDO
                || atual.getAvaliacao().getStatus()
                != StatusCodeRunner.VALIDO) {

            return false;
        }

        Map<String, ResultadoTeste> antes =
            indexar(anterior.getAvaliacao().getTestes());

        Map<String, ResultadoTeste> depois =
            indexar(atual.getAvaliacao().getTestes());

        return !antes.isEmpty()
                && antes.keySet().equals(depois.keySet());
    }

    // -------------------------------------------------
    // COMPARAR CADA TESTE
    // -------------------------------------------------

    public List<ComparacaoTeste> getComparacoesTestes() {

        if (!testesComparaveis()) {
            return List.of();
        }

        Map<String, ResultadoTeste> antes =
            indexar(anterior.getAvaliacao().getTestes());

        Map<String, ResultadoTeste> depois =
            indexar(atual.getAvaliacao().getTestes());

        List<ComparacaoTeste> comparacoes =
            new ArrayList<>();

        for (Map.Entry<String, ResultadoTeste> item
                : antes.entrySet()) {

            String id = item.getKey();

            ResultadoTeste testeAntes = item.getValue();
            ResultadoTeste testeDepois = depois.get(id);

            MudancaTeste mudanca;

            if (!testeAntes.isCorreto()
                    && !testeDepois.isCorreto()) {

                mudanca = MudancaTeste.ERRO_PERSISTENTE;

            } else if (!testeAntes.isCorreto()) {

                mudanca = MudancaTeste.ERRO_RESOLVIDO;

            } else if (!testeDepois.isCorreto()) {

                mudanca = MudancaTeste.NOVO_ERRO;

            } else {

                mudanca = MudancaTeste.ACERTO_MANTIDO;
            }

            boolean saidaMudou = !Objects.equals(
                testeAntes.getObtido(),
                testeDepois.getObtido()
            );

            comparacoes.add(
                new ComparacaoTeste(
                    id,
                    testeAntes,
                    testeDepois,
                    mudanca,
                    saidaMudou
                )
            );
        }

        return Collections.unmodifiableList(comparacoes);
    }

    // -------------------------------------------------
    // CONTAR CADA RESULTADO
    // -------------------------------------------------

    public long contar(MudancaTeste mudanca) {

        return getComparacoesTestes().stream()
                .filter(t -> t.mudanca() == mudanca)
                .count();
    }

    // -------------------------------------------------
    // RESUMO DA REVISAO
    // -------------------------------------------------

    public String getResumoTestes() {

        if (!testesComparaveis()) {
            return "NAO_COMPARAVEL "
                    + "(compilacao ou testes diferentes/ausentes)";
        }

        return "persistentes="
                + contar(MudancaTeste.ERRO_PERSISTENTE)
                + ", resolvidos="
                + contar(MudancaTeste.ERRO_RESOLVIDO)
                + ", novos="
                + contar(MudancaTeste.NOVO_ERRO)
                + ", acertos_mantidos="
                + contar(MudancaTeste.ACERTO_MANTIDO);
    }

    // -------------------------------------------------
    // IDENTIFICAR OS MESMOS TESTES ENTRE SUBMISSOES
    // -------------------------------------------------

    private static Map<String, ResultadoTeste> indexar(
            List<ResultadoTeste> testes) {

        Map<String, ResultadoTeste> indice =
            new LinkedHashMap<>();

        Map<String, Integer> ocorrencias =
            new LinkedHashMap<>();

        for (ResultadoTeste teste : testes) {

            String assinatura = assinatura(teste);

            int ocorrencia = ocorrencias.merge(
                assinatura, 1, Integer::sum
            );

            indice.put(
                assinatura + "#" + ocorrencia,
                teste
            );
        }

        return indice;
    }

    private static String assinatura(
            ResultadoTeste teste) {

        return componente(teste.getNome())
                + componente(teste.getEntrada())
                + componente(teste.getEsperado());
    }

    private static String componente(String texto) {

        String valor = texto == null ? "" : texto;

        return valor.length() + ":" + valor;
    }
}
