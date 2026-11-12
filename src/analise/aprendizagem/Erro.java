
package analise.aprendizagem;

public class Erro {

    private String categoria;
    private String subcategoria;
    private String evidencia;

    // Construtor original
    public Erro(String categoria) {
        this(categoria, "NAO_CLASSIFICADO", "");
    }

    // Construtor com subcategoria
    public Erro(String categoria, String subcategoria) {
        this(categoria, subcategoria, "");
    }

    // Construtor completo
    public Erro(String categoria, String subcategoria,
                String evidencia) {

        this.categoria = categoria;
        this.subcategoria = subcategoria;
        this.evidencia = evidencia;
    }

    // Metadata for auditing rule-based classification; existing constructors remain valid.
    private String metodoClassificacao = "REGRAS_CODERUNNER";
    private String nivelConfianca = "A_VALIDAR";
    private boolean requerValidacaoManual = true;
    public String getMetodoClassificacao() { return metodoClassificacao; }
    public String getNivelConfianca() { return nivelConfianca; }
    public boolean isRequerValidacaoManual() { return requerValidacaoManual; }
    public void definirAuditoria(String metodo, String confianca, boolean validacaoManual) {
        this.metodoClassificacao = metodo;
        this.nivelConfianca = confianca;
        this.requerValidacaoManual = validacaoManual;
    }

    // Consultar categoria geral
    public String getCategoria() {
        return categoria;
    }

    // Consultar subcategoria
    public String getSubcategoria() {
        return subcategoria;
    }

    // Consultar evidencia utilizada
    public String getEvidencia() {
        return evidencia;
    }

    // Alterar classificacao
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public void setSubcategoria(String subcategoria) {
        this.subcategoria = subcategoria;
    }

    public void setEvidencia(String evidencia) {
        this.evidencia = evidencia;
    }

    // Verificar se foi possivel identificar
    // uma subcategoria especifica
    public boolean temSubcategoria() {

        return subcategoria != null
            && !subcategoria.isBlank()
            && !subcategoria.equals("NAO_CLASSIFICADO");
    }

    // Apresentacao resumida
    @Override
    public String toString() {

        if (temSubcategoria()) {
            return categoria + ": " + subcategoria;
        }

        return categoria;
    }
}
