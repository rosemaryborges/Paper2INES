package analise.aprendizagem;

import java.util.ArrayList;
import java.util.List;
import analise.contexto.Aluno;
import analise.contexto.Questao;

public class Trajetoria {
    private final Aluno aluno;
    private final Questao questao;
    private final List<Submissao> submissoes = new ArrayList<>();

    public Trajetoria(Aluno aluno, Questao questao) { this.aluno = aluno; this.questao = questao; }
    public Aluno getAluno() { return aluno; }
    public Questao getQuestao() { return questao; }
    public List<Submissao> getSubmissoes() { return submissoes; }
    public void adicionarSubmissao(Submissao s) {
        if (!submissoes.isEmpty()) {
            Submissao anterior = submissoes.get(submissoes.size()-1);
            if (s.getTurma() != anterior.getTurma() || s.getAlunoId() != anterior.getAlunoId()
                || s.getQuestaoId() != anterior.getQuestaoId() || s.getQuizId() != anterior.getQuizId()
                || s.getQuestionAttemptId() != anterior.getQuestionAttemptId()
                || s.getMomento() < anterior.getMomento())
                throw new IllegalArgumentException("Submissao de outra tentativa ou fora de ordem.");
        }
        submissoes.add(s);
    }
    public int contarRevisoes() { return Math.max(0, submissoes.size()-1); }
    public boolean conseguiuCorrigir() {
        return !submissoes.isEmpty() && submissoes.get(submissoes.size()-1).estaCorreta();
    }
    public double tempoAteCorrecao() {
        if (!conseguiuCorrigir()) return Double.NaN;
        return (submissoes.get(submissoes.size()-1).getMomento()
                 - submissoes.get(0).getMomento()) / 60.0;
    }
    public List<Revisao> getRevisoes() {
        List<Revisao> revisoes = new ArrayList<>();
        for (int i=1; i<submissoes.size(); i++)
            revisoes.add(new Revisao(submissoes.get(i-1), submissoes.get(i)));
        return revisoes;
    }
    public long contarRevisoesLocalizadas() { return contarTipo("LOCALIZADA"); }
    public long contarRevisoesEstruturais() { return contarTipo("ESTRUTURAL"); }
    public long contarSubstituicoesAmplas() { return contarTipo("SUBSTITUICAO_AMPLA"); }
    private long contarTipo(String tipo) { return getRevisoes().stream().filter(r -> tipo.equals(r.getTipoRevisao())).count(); }
    public double getMagnitudeMediaRevisoes() { return getRevisoes().stream().mapToDouble(Revisao::getMagnitudeRevisao).average().orElse(Double.NaN); }
    public double getMaiorMagnitudeRevisao() { return getRevisoes().stream().mapToDouble(Revisao::getMagnitudeRevisao).max().orElse(Double.NaN); }
    public Revisao getPrimeiraRevisaoBemSucedida() { return getRevisoes().stream().filter(r -> r.getAtual().estaCorreta()).findFirst().orElse(null); }
    public String getIdentificador() { return aluno.getTurma()+":"+aluno.getId()+":"+questao.getAtividadeId()+":"+questao.getId()+":"+ (submissoes.isEmpty()?0:submissoes.get(0).getQuestionAttemptId()); }
    // Orthogonal descriptors: retain legacy getPadrao() for backward comparison.
    public String getResultadoFinal() {
        if (submissoes.isEmpty()) return "SEM_DADOS";
        if (conseguiuCorrigir()) return "CORRECAO_COMPLETA";
        return contarRevisoes() == 0 ? "SEM_NOVA_SUBMISSAO" : "SEM_CORRECAO_OBSERVADA";
    }
    public boolean teveRegressaoPontuacao() {
        return getRevisoes().stream().anyMatch(r -> r.getDiferencaPontuacao() < -0.000001);
    }
    public boolean teveMelhoriaParcial() {
        return getRevisoes().stream().anyMatch(r -> !r.getAtual().estaCorreta()
                && r.getDiferencaPontuacao() > 0.000001);
    }
    public long contarLacunasSequencia() {
        return getRevisoes().stream().filter(Revisao::temLacunaSequencia).count();
    }
    public long contarPausasLongas(double limiteMinutos) {
        return getRevisoes().stream().filter(r -> r.getIntervaloMinutos() > limiteMinutos).count();
    }
    public double medianaIntervalosMinutos() {
        double[] v = getRevisoes().stream().mapToDouble(Revisao::getIntervaloMinutos)
                .filter(Double::isFinite).sorted().toArray();
        if (v.length == 0) return Double.NaN;
        int meio = v.length/2;
        return v.length%2==1 ? v[meio] : (v[meio-1]+v[meio])/2.0;
    }
    public String getErroInicialCategoria() {
        return submissoes.isEmpty() ? "SEM_DADOS" : submissoes.get(0).getErro().getCategoria();
    }
    public String getErroInicialSubcategoria() {
        return submissoes.isEmpty() ? "SEM_DADOS" : submissoes.get(0).getErro().getSubcategoria();
    }
    public String getPadrao() {
        List<Revisao> revisoes = getRevisoes();
        if (revisoes.isEmpty()) return "SEM_NOVA_SUBMISSAO";
        if (conseguiuCorrigir() && revisoes.size() == 1) return "CORRECAO_IMEDIATA";
        boolean melhorouAntes = false, piorou = false;
        for (Revisao r : revisoes) {
            if (!r.getAtual().estaCorreta() && r.getDiferencaPontuacao() > 0.000001) melhorouAntes = true;
            if (r.getDiferencaPontuacao() < -0.000001) piorou = true;
        }
        if (piorou) return "OSCILACAO_PONTUACAO";
        if (conseguiuCorrigir() && melhorouAntes) return "MELHORIA_ANTES_CORRECAO";
        if (conseguiuCorrigir()) return "CORRECAO_SEM_MELHORIA_INTERMEDIA";
        if (melhorouAntes) return "MELHORIA_PARCIAL_SEM_CORRECAO";
        return "SEM_MELHORIA_MENSURAVEL";
    }
}