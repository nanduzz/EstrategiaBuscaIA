package principal;

import estrategias.EstrategiaBusca;
import estrategias.EstrategiaBuscaFernando;
import estrategias.EstrategiaBuscaMayza;
import estrategias.EstrategiaBuscaVinicius;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import modelo.Dia;
import util.CarregaDados;

/**
 *
 * @author Camargo
 */
public class Principal {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        System.out.println("Bem-vindo ao Simulador de portfólio");

        List<Dia> periodo2014e2015 = CarregaDados.carregaDados(
                new File("COTAHIST_A2014.TXT"),
                new File("COTAHIST_A2015.TXT"));
        List<Dia> periodo2016 = CarregaDados.carregaDados(
                new File("COTAHIST_A2016.TXT"));

        List<EstrategiaBusca> estrategias = Arrays.asList(new EstrategiaBuscaMayza(), new EstrategiaBuscaVinicius(), new EstrategiaBuscaFernando());
        for (EstrategiaBusca estrategiaBuscaIA : estrategias) {
            estrategiaBuscaIA.recebeDadosTreino(periodo2014e2015);
            estrategiaBuscaIA.recebeDadosTeste(periodo2016);
            estrategiaBuscaIA.aplicaEstrategiaBusca();

            System.out.println(estrategiaBuscaIA.devolveValorPortfolio());
            System.out.println(estrategiaBuscaIA.devolveResultadosMesAMes());
            System.out.println(estrategiaBuscaIA.devolveAcaoMaiorGanho());
            System.out.println(estrategiaBuscaIA.devolveAcaoMaiorPrejuizo());
        }

    }

}
