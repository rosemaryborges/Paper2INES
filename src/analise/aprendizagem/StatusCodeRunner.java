package analise.aprendizagem;

public enum StatusCodeRunner {
    VALIDO(1, "STATUS_VALID"),
    ERRO_SINTAXE(2, "STATUS_SYNTAX_ERROR"),
    ERRO_COMBINADOR(3, "STATUS_BAD_COMBINATOR"),
    ERRO_SANDBOX(4, "STATUS_SANDBOX_ERROR"),
    PROTOTIPO_AUSENTE(5, "STATUS_MISSING_PROTOTYPE"),
    DESSERIALIZACAO_FALHOU(6, "STATUS_UNSERIALIZE_FAILED"),
    DESCONHECIDO(-1, "DESCONHECIDO");

    private final int codigo;
    private final String descricao;

    StatusCodeRunner(int codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }
    public int getCodigo() { return codigo; }
    public String getDescricao() { return descricao; }

    public static StatusCodeRunner deCodigo(int codigo) {
        for (StatusCodeRunner s : values()) {
            if (s.codigo == codigo) return s;
        }
        return DESCONHECIDO;
    }
}
