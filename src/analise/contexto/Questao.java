package analise.contexto;

public class Questao {
    private final int id;
    private final String nome;
    private final int unidade;
    private final String tipo;
    private final int atividadeId;
    private final String grupoAnalitico;
    private final String tipoAtividade;

    public Questao(int id, String nome, int unidade, String tipo) {
        this(id, nome, unidade, tipo, 0);
    }
    public Questao(int id, String nome, int unidade, String tipo, int atividadeId) {
        this(id, nome, unidade, tipo, atividadeId, "", "");
    }
    public Questao(int id, String nome, int unidade, String tipo, int atividadeId,
                  String grupoAnalitico, String tipoAtividade) {
        this.id=id; this.nome=nome; this.unidade=unidade; this.tipo=tipo;
        this.atividadeId=atividadeId;
        this.grupoAnalitico=grupoAnalitico == null ? "" : grupoAnalitico;
        this.tipoAtividade=tipoAtividade == null ? "" : tipoAtividade;
    }
    public int getAtividadeId() { return atividadeId; }
    public int getId() { return id; }
    public String getNome() { return nome; }
    public int getUnidade() { return unidade; }
    public String getTipo() { return tipo; }
    public String getGrupoAnalitico() { return grupoAnalitico; }
    public String getTipoAtividade() { return tipoAtividade; }
}
