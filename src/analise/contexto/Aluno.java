package analise.contexto;

import java.util.ArrayList;
import java.util.List;
import analise.aprendizagem.Trajetoria;

public class Aluno {
    private final int id;
    private final int turma;
    private final List<Trajetoria> trajetorias = new ArrayList<>();

    public Aluno(int id, int turma) { this.id = id; this.turma = turma; }
    public int getId() { return id; }
    public int getTurma() { return turma; }
    public List<Trajetoria> getTrajetorias() { return trajetorias; }
    public void adicionarTrajetoria(Trajetoria trajetoria) { trajetorias.add(trajetoria); }
}