package analise.processamento;

import analise.aprendizagem.*;
import java.util.*;

/** Q4: classificacao inicial e sucesso; a analise inferencial fica para R. */
public final class AnaliseRQ4 {
    private AnaliseRQ4() {}
    public static void executar(List<Trajetoria> trajetorias) {
        Map<String,int[]> resumo=new TreeMap<>();
        int elegiveis=0;
        for (Trajetoria t:trajetorias) {
            String comportamento=Indicadores.comportamentoInicial(t);
            int[] contagem=resumo.computeIfAbsent(comportamento,k->new int[3]);
            contagem[0]++;
            if(t.conseguiuCorrigir()) contagem[1]++;
            if(t.conseguiuCorrigir() && t.contarRevisoes()>1) contagem[2]++;
            if(t.contarRevisoes()>0 && !"CORRECAO_IMEDIATA".equals(comportamento)
                && !"INDETERMINADO".equals(comportamento)) elegiveis++;
        }
        System.out.println("\nQ4 | Comportamento da primeira revisao");
        System.out.println("Comportamento | n | sucesso eventual | sucesso posterior");
        resumo.forEach((nome,c)->System.out.printf("%s | %d | %d | %d%n",nome,c[0],c[1],c[2]));
        System.out.println("Elegiveis para comparacao do sucesso posterior: "+elegiveis);
        System.out.println("***** verificar dependencia entre trajetorias do mesmo aluno antes de aplicar Fisher.");
    }
}
