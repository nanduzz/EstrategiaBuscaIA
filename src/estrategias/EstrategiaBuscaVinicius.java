/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package estrategias;

import java.util.List;
import modelo.Dia;

/**
 *
 * @author Camargo
 */
public class EstrategiaBuscaVinicius implements EstrategiaBusca {

    class Empresa{
        String nome;
        Integer nrDeAcoes;
        Double valor;
    }
    
    List<Dia> periodoTreino;
    List<Dia> periodoTeste;
    final Double CARTEIRA_INICIAL = 1000000D;
    Double carteira = 0D;

    @Override
    public void recebeDadosTreino(List<Dia> periodo) {
        periodoTreino = periodo;
    }

    @Override
    public void recebeDadosTeste(List<Dia> periodo) {
        periodoTeste = periodo;
    }

    @Override
    public void aplicaEstrategiaBusca() {
        
        
        System.out.println(periodoTeste.size());
    }

    @Override
    public Double devolveValorPorfolio() {
        return 0D;
    }

    @Override
    public String devolveResultadosMesAMes() {
        return "null";
    }

    @Override
    public String devolveAcaoMaiorGanho() {
        return "null";
    }

    @Override
    public String devolveAcaoMaiorPrejuizo() {
        return "null";
    }

}
