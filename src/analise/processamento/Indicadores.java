package analise.processamento;

import analise.aprendizagem.*;
import java.util.*;

/** Metricas comuns, sem efeitos secundarios. */
public final class Indicadores {
    private Indicadores() {}
    public static final double EPS = 1e-6;
    public static String turma(int turma) { return turma==3 ? "T1" : turma==5 ? "T2" : "T"+turma; }
    public static String id(Trajetoria t) { return t.getIdentificador(); }
    public static double media(Collection<Double> valores) {
        return valores.stream().filter(Objects::nonNull).filter(Double::isFinite)
                .mapToDouble(Double::doubleValue).average().orElse(Double.NaN);
    }
    public static double mediana(Collection<Double> valores) {
        double[] a=valores.stream().filter(Objects::nonNull).filter(Double::isFinite)
                .mapToDouble(Double::doubleValue).sorted().toArray();
        if(a.length==0) return Double.NaN;
        return a.length%2==1 ? a[a.length/2] : (a[a.length/2-1]+a[a.length/2])/2.0;
    }
    public static int revisoesAteSucesso(Trajetoria t) {
        if(!t.conseguiuCorrigir()) return -1;
        for(int i=1;i<t.getSubmissoes().size();i++)
            if(t.getSubmissoes().get(i).estaCorreta()) return i;
        throw new IllegalStateException("Trajetoria inconsistente: "+id(t));
    }
    public static String comportamentoInicial(Trajetoria t) {
        List<Revisao> r=t.getRevisoes();
        if(r.isEmpty()) return "SEM_NOVA_SUBMISSAO";
        Revisao primeira=r.get(0);
        if(primeira.corrigiuNestaRevisao()) return "CORRECAO_IMEDIATA";
        String comportamento=primeira.getComportamento();
        if("PASSOU_A_EXECUTAR".equals(comportamento)) return "MELHORIA_PARCIAL";
        if("REGRESSAO_COMPILACAO".equals(comportamento)) return "REGRESSAO";
        double delta=primeira.getDiferencaPontuacao();
        if(!Double.isFinite(delta)) return "INDETERMINADO";
        if(delta>EPS) return "MELHORIA_PARCIAL";
        if(delta< -EPS) return "REGRESSAO";
        return "SEM_MELHORIA_NA_PONTUACAO";
    }
    public static String[] prefixo(Trajetoria t) {
        Submissao s=t.getSubmissoes().get(0);
        return new String[]{id(t),turma(s.getTurma()),""+s.getAlunoId(),""+t.getQuestao().getUnidade(),""+s.getQuestaoId(),
            ""+s.getQuizId(),""+s.getQuestionAttemptId(),""+s.getIdPasso()};
    }
    public static String[] juntar(String[]... partes) {
        int n=Arrays.stream(partes).mapToInt(a->a.length).sum(); String[] out=new String[n];int p=0;
        for(String[] a:partes) { System.arraycopy(a,0,out,p,a.length);p+=a.length; }return out;
    }
    public static List<Trajetoria> selecionarUnidades(List<Trajetoria> trajetorias) {
        List<Trajetoria> saida=new ArrayList<>();
        for(Trajetoria t:trajetorias) {
            if(!Set.of(2,4,5).contains(t.getQuestao().getUnidade())) continue;
            if(t.getSubmissoes().isEmpty() || t.getSubmissoes().get(0).estaCorreta())
                throw new IllegalArgumentException("Trajetoria nao comeca com erro: "+id(t));
            boolean houveCorrecao=false;
            for(int i=0;i<t.getSubmissoes().size();i++) {
                if(houveCorrecao) throw new IllegalArgumentException("Submissoes apos primeira correcao: "+id(t));
                houveCorrecao=t.getSubmissoes().get(i).estaCorreta();
            }
            saida.add(t);
        }
        saida.sort(Comparator.comparing(Indicadores::id));
        return saida;
    }
}
