package analise.codigo;


public class ClassificadorRevisao {
    public enum Tipo { SEM_ALTERACAO, LOCALIZADA, ESTRUTURAL, SUBSTITUICAO_AMPLA, INDETERMINADA }
    /** Regras exploratorias, nao validadas. Thresholds ajustaveis apos amostragem. */
    public Tipo classificar(ResultadoComparacaoCodigo c) {
        if (c.codigoNormalizadoIgual()) return Tipo.SEM_ALTERACAO;
        if (c.magnitude() >= 0.65) return Tipo.SUBSTITUICAO_AMPLA;
        if (c.alteracaoCondicional() || c.alteracaoCiclo() || c.alteracaoMetodo())
            return Tipo.ESTRUTURAL;
        if (c.magnitude() <= 0.20) return Tipo.LOCALIZADA;
        return Tipo.INDETERMINADA;
    }
    public boolean requerRevisaoManual(ResultadoComparacaoCodigo c, Tipo t) {
        return t == Tipo.INDETERMINADA || t == Tipo.SUBSTITUICAO_AMPLA ||
              (t == Tipo.LOCALIZADA && (c.alteracaoCondicional() || c.alteracaoCiclo()));
    }
}
