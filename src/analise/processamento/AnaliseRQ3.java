package analise.processamento;

import analise.aprendizagem.*;
import java.util.*;

/** Q3: resumo temporal calculado sem criar CSV suplementar. */
public final class AnaliseRQ3 {
    private AnaliseRQ3() {}
    public static void executar(List<Trajetoria> trajetorias) {
        Map<String,List<Double>> intervalos = new TreeMap<>(), tempos = new TreeMap<>();
        for (Trajetoria t:trajetorias) {
            String padrao=t.getPadrao();
            List<Double> valores=intervalos.computeIfAbsent(padrao,k->new ArrayList<>());
            for(Revisao r:t.getRevisoes()) valores.add(r.getIntervaloMinutos());
            if(t.conseguiuCorrigir()) tempos.computeIfAbsent(padrao,k->new ArrayList<>()).add(t.tempoAteCorrecao());
        }
        System.out.println("\nQ3 | Tempos cronologicos em minutos (nao tempo ativo)");
        System.out.println("Padrao | n intervalos | intervalo mediano | n corrigidas | tempo ate acerto mediano");
        for(String padrao:intervalos.keySet()) {
            List<Double> a=intervalos.get(padrao),b=tempos.getOrDefault(padrao,List.of());
            System.out.printf(Locale.US,"%s | %d | %s | %d | %s%n",padrao,a.size(),
                CSV.numero(Indicadores.mediana(a)),b.size(),CSV.numero(Indicadores.mediana(b)));
        }
    }
}
