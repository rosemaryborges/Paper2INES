package analise.aprendizagem;

public class Submissao {
    private final int turma, alunoId, questaoId, questaoIdOriginal, idPasso;
    private final long momento;
    private final String codigo, estado;
    private final Double notaMoodle;
    private final AvaliacaoCodeRunner avaliacao;
    private final Erro erro;
    private final int questionAttemptId, quizAttemptId, quizId, sequenceNumber;
    private final String grupoAnalitico, tipoAtividade;
    private final int unidade;

    public Submissao(int turma, int alunoId, int questaoId, int questaoIdOriginal,
                     int idPasso, long momento, String codigo, String estado,
                     Double notaMoodle, AvaliacaoCodeRunner avaliacao, Erro erro) {
        this(turma, alunoId, questaoId, questaoIdOriginal, idPasso, momento, codigo, estado,
                notaMoodle, avaliacao, erro, 0, 0, 0, 0);
    }

    public Submissao(int turma, int alunoId, int questaoId, int questaoIdOriginal,
                     int idPasso, long momento, String codigo, String estado,
                     Double notaMoodle, AvaliacaoCodeRunner avaliacao, Erro erro,
                     int questionAttemptId, int quizAttemptId, int quizId, int sequenceNumber) {
        this(turma, alunoId, questaoId, questaoIdOriginal, idPasso, momento, codigo, estado,
             notaMoodle, avaliacao, erro, questionAttemptId, quizAttemptId, quizId, sequenceNumber,
             "", "", -1);
    }

    public Submissao(int turma, int alunoId, int questaoId, int questaoIdOriginal,
                     int idPasso, long momento, String codigo, String estado,
                     Double notaMoodle, AvaliacaoCodeRunner avaliacao, Erro erro,
                     int questionAttemptId, int quizAttemptId, int quizId, int sequenceNumber,
                     String grupoAnalitico, String tipoAtividade, int unidade) {
        this.grupoAnalitico = grupoAnalitico == null ? "" : grupoAnalitico;
        this.tipoAtividade = tipoAtividade == null ? "" : tipoAtividade;
        this.unidade = unidade;
        this.questionAttemptId = questionAttemptId;
        this.quizAttemptId = quizAttemptId;
        this.quizId = quizId;
        this.sequenceNumber = sequenceNumber;
        this.turma = turma; this.alunoId = alunoId; this.questaoId = questaoId;
        this.questaoIdOriginal = questaoIdOriginal; this.idPasso = idPasso;
        this.momento = momento; this.codigo = codigo; this.estado = estado;
        this.notaMoodle = notaMoodle; this.avaliacao = avaliacao; this.erro = erro;
    }
    public String getGrupoAnalitico() { return grupoAnalitico; }
    public String getTipoAtividade() { return tipoAtividade; }
    public int getUnidade() { return unidade; }
    public int getQuestionAttemptId() { return questionAttemptId; }
    public int getQuizAttemptId() { return quizAttemptId; }
    public int getQuizId() { return quizId; }
    public int getSequenceNumber() { return sequenceNumber; }
    public int getTurma() { return turma; }
    public int getAlunoId() { return alunoId; }
    public int getQuestaoId() { return questaoId; }
    public int getQuestaoIdOriginal() { return questaoIdOriginal; }
    public int getIdPasso() { return idPasso; }
    public long getMomento() { return momento; }
    public String getCodigo() { return codigo; }
    public String getEstado() { return estado; }
    public Double getNotaMoodle() { return notaMoodle; }
    public AvaliacaoCodeRunner getAvaliacao() { return avaliacao; }
    public Erro getErro() { return erro; }
    public boolean estaCorreta() { return avaliacao.foiAceite(); }
    public double getPontuacao() {
        if (avaliacao.getStatus() == StatusCodeRunner.ERRO_SINTAXE) return 0.0;
        return avaliacao.getProporcao();
    }
}
