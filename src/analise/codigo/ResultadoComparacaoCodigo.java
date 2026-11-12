package analise.codigo;


public record ResultadoComparacaoCodigo(double similaridade, double magnitude,
      int linhasAdicionadas, int linhasRemovidas, int linhasAlteradas,
      boolean alteracaoCondicional, boolean alteracaoCiclo,
      boolean alteracaoMetodo, boolean codigoIgual, boolean codigoNormalizadoIgual) {}
