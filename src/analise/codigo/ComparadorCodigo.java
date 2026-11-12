package analise.codigo;


import java.util.*;
import java.util.regex.Pattern;

/** Comparacao deterministica por linhas; NAO e um parser Java nem prova mudanca conceptual. */
public class ComparadorCodigo {
    private static final Pattern SPACE = Pattern.compile("\\s+");
    public ResultadoComparacaoCodigo comparar(String antes, String depois) {
        String a = antes == null ? "" : antes;
        String b = depois == null ? "" : depois;
        List<String> left = normalizarLinhas(a), right = normalizarLinhas(b);
        // distancia por sequencia comum: muda pouco para insercoes/delecoes
        int comuns = lcs(left, right);
        int adicionadas = right.size() - comuns;
        int removidas = left.size() - comuns;
        int denominador = Math.max(1, Math.max(left.size(),right.size()));
        double similaridade = Math.max(0.0, Math.min(1.0, (double)comuns/denominador));
        double magnitude = 1.0 - similaridade;
        boolean igual = a.equals(b);
        boolean normIgual = left.equals(right);
        boolean cond = mudouElementos(left,right,"if", "else", "switch", "case", "?:");
        boolean ciclo = mudouElementos(left,right,"for", "while", "do");
        boolean metodo = mudouElementos(left,right,"public", "private", "protected", "static", "void");
        // Aproximacao: numero de pares substituidos na distancia LCS
        int alteradas = Math.min(adicionadas, removidas);
        return new ResultadoComparacaoCodigo(similaridade,magnitude,
            adicionadas-alteradas,removidas-alteradas,alteradas,
            cond,ciclo,metodo,igual,normIgual);
    }
    private static List<String> normalizarLinhas(String src) {
        List<String> result=new ArrayList<>();
        for(String line:src.replace("\r\n","\n").replace('\r','\n').split("\n",-1)) {
           String normalized = SPACE.matcher(line.trim()).replaceAll(" ");
           if(!normalized.isEmpty()) result.add(normalized);
        }
        return result;
    }
    private static int lcs(List<String> a, List<String> b) {
        int[] prev=new int[b.size()+1], cur=new int[b.size()+1];
        for(String x:a){
            Arrays.fill(cur,0);
            for(int j=1;j<=b.size();j++) cur[j]=x.equals(b.get(j-1))?prev[j-1]+1:Math.max(prev[j],cur[j-1]);
            int[] tmp=prev;prev=cur;cur=tmp;
        }
        return prev[b.size()];
    }
    // Marcador heuristico: identifica alteracoes de linhas com palavras-chave; requer validacao.
    private static boolean mudouElementos(List<String> a,List<String> b,String... words){
        Set<String> x = identificar(a,words), y = identificar(b,words);
        return !x.equals(y);
    }
    private static Set<String> identificar(List<String> linhas,String... words){
        Set<String> matches=new LinkedHashSet<>();
        for(String line:linhas) for(String word:words)
            if(Pattern.compile("\\b"+Pattern.quote(word)+"\\b").matcher(line).find()) matches.add(line);
        return matches;
    }
}
