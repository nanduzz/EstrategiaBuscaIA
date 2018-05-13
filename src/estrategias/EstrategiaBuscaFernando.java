/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package estrategias;

import estrategias.fernando.Individuo;
import estrategias.fernando.Portfolio;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import modelo.Dia;
import util.indicadores.RSI;

/**
 *
 * @author fernando
 */
public class EstrategiaBuscaFernando implements EstrategiaBusca {

    HashMap<String, List<Dia>> empresaDiasTreino = new HashMap<>(); //
    HashMap<String, List<Dia>> empresaDiasTeste = new HashMap<>();
    HashMap<String, List<Double>> diasPassados = new HashMap<>(); // siglaEmpresa / valor
    LinkedHashSet<String> diasTreino = new LinkedHashSet<>();
    LinkedHashSet<String> diasTeste = new LinkedHashSet<>();

    List<Individuo> populacao = new ArrayList<>();
    private final int NRO_MUTACOES = 20;
    private final String resultadoMes[] = new String[12];

    private String valorPortfolio = "";
    private String melhorAcao;
    private String piorAcao;

    private static final DecimalFormat DF = new DecimalFormat("#.##");

    @Override
    public void recebeDadosTreino(List<Dia> periodo) {
        periodo.forEach((Dia d) -> {
            this.diasTreino.add(d.getData().toString());
            if (!empresaDiasTreino.containsKey(d.getData().toString())) {
                empresaDiasTreino.put(d.getData().toString(), new ArrayList<>());
            }
            empresaDiasTreino.get(d.getData().toString()).add(d);
        });

    }

    @Override
    public void recebeDadosTeste(List<Dia> periodo) {
        periodo.forEach((Dia d) -> {
            this.diasTeste.add(d.getData().toString());
            if (!empresaDiasTeste.containsKey(d.getData().toString())) {
                empresaDiasTeste.put(d.getData().toString(), new ArrayList<>());
            }
            empresaDiasTeste.get(d.getData().toString()).add(d);
        });
    }

    @Override
    public void aplicaEstrategiaBusca() {
        for (int geracao = 0; geracao < 1000; geracao++) {
            if (geracao > 0) {
                executaCrossovers();
            }
            for (int i = 0; i < 1000; i++) {
                executaPopulacaoNormal();
            }
            ordenaPorFitness();
            this.populacao.subList(1000, this.populacao.size()).clear();

        }
        this.realizaTeste();
    }

    private void executaPopulacaoNormal() {
        Individuo individuo = new Individuo();
        individuo.geraGenesAleatorios();

        this.realizaNegociacoes(individuo, this.diasTreino, this.empresaDiasTreino, false);
        this.populacao.add(individuo);
    }

    private void executaCrossovers() {
        for (int mutacao = 0; mutacao < NRO_MUTACOES; mutacao++) {
            Individuo membroMutado = Individuo.crossOver(Individuo.selecionaParaCrossOver(populacao),
                    Individuo.selecionaParaCrossOver(populacao)
            );
            this.realizaNegociacoes(membroMutado, diasTreino, empresaDiasTreino, false);
            this.populacao.add(membroMutado);
        }
    }

    private void ordenaPorFitness() {
        try {
            Collections.sort(populacao, (Individuo membro1, Individuo membro2) -> {
                if (!(membro1 instanceof Individuo) || !(membro2 instanceof Individuo)) {
                    return -1;
                }
                if (membro1.getValorCaixaFinal() <= membro2.getValorCaixaFinal()) {
                    return 1;
                } else {
                    return -1;
                }
            });
        } catch (Exception e) {
        }
    }

    private void realizaNegociacoes(Individuo membroPopulacao, LinkedHashSet<String> dias, HashMap<String, List<Dia>> dadosDias, boolean executandoFinal) {
        this.diasPassados = new HashMap<>();
        String mesTmp = null;
        for (String dia : dias) {
            if (executandoFinal) {
                mesTmp = this.realizaCalculoMesAMes(mesTmp, dia, diasPassados);
            }
            dadosDias.get(dia).forEach((Dia diaEmpresa) -> {
                String sigla = diaEmpresa.getSigla();
                if (!diasPassados.containsKey(sigla)) {
                    diasPassados.put(sigla, new ArrayList<>());
                }
                double rsi = new RSI(this.diasPassados.get(sigla), (int) membroPopulacao.getPeriodoCalculoRSI()).calcula();

                if (membroPopulacao.deveComprarAcao(rsi, sigla)) {
                    membroPopulacao.compraAcoes(sigla, this.getUltimoValorFechamento(sigla));
                } else if (membroPopulacao.deveVenderAcao(rsi, sigla, this.getUltimoValorFechamento(sigla))) {

                    membroPopulacao.vendeAcao(sigla, this.getUltimoValorFechamento(sigla));
                }
                diasPassados.get(sigla).add(diaEmpresa.getValorFechamento());
            });

        }
        membroPopulacao.vendeTodasAcoes(this.diasPassados);
    }

    private static void printaCompra(String sigla, int quantidade, double valor) {
        System.out.println("-------------");
        System.out.println("Comprando: " + sigla + "\t quantidade :" + quantidade + "  \t valor" + valor);
        System.out.println("-------------");
    }

    private static void printaVenda(String sigla, int quantidade, double valor) {
        System.out.println("-------------");
        System.out.println("Vendendo: " + sigla + "\t quantidade :" + quantidade + "  \t valor" + valor);
        System.out.println("-------------");
    }

    @Override
    public String devolveValorPortfolio() {
        return "Estrategia de Fernando Carvalho\n" + this.valorPortfolio;
    }

    @Override
    public String devolveResultadosMesAMes() {
        String resultadoMesAMes = "";
        for (int i = 0; i < this.resultadoMes.length - 1; i++) {
            resultadoMesAMes += "------------------------------------------------------\n";
            resultadoMesAMes += "Mês " + (i + 1) + ": " + this.resultadoMes[i] + "\n";
        }
        resultadoMesAMes += "------------------------------------------------------\n";
        resultadoMesAMes += "Mês 12: " + this.valorPortfolio + "\n";

        return resultadoMesAMes;
    }

    @Override
    public String devolveAcaoMaiorGanho() {
        return this.melhorAcao;
    }

    @Override
    public String devolveAcaoMaiorPrejuizo() {
        return this.piorAcao;
    }

    public double getUltimoValorFechamento(String sigla) {
        if (this.diasPassados.containsKey(sigla) && this.diasPassados.get(sigla).size() > 0) {
            return this.diasPassados.get(sigla).get(this.diasPassados.get(sigla).size() - 1);
        }
        return 0;
    }

    private void setValorPortfolio(double valor) {
        this.valorPortfolio = DF.format(valor);
    }

    private void setValorMensal(int mes, String valor) {
        int mMes = mes - 1;
        this.resultadoMes[mMes] = valor;
    }

    private void setMelhorAcao(String melhorAcao) {
        this.melhorAcao = melhorAcao;
    }

    private void setPiorAcao(String piorAcao) {
        this.piorAcao = piorAcao;
    }

    private String realizaCalculoMesAMes(String mesTmp, String dia, HashMap<String, List<Double>> diasPassados) {
        String mes = dia.split("-")[1];
        if (mesTmp == null) {
            mesTmp = mes;
            return mesTmp;
        }
        if (!mesTmp.equals(mes)) {
            this.setValorMensal(
                    Integer.valueOf(mesTmp),
                    DF.format(this.populacao.get(0).calculaDadosMensal(mesTmp, diasPassados))
            );
            mesTmp = mes;
        }

        return mesTmp;
    }

    private Individuo melhorDaPopulacao() {
        return this.populacao.get(0);
    }

    private void realizaTeste() {
        Individuo melhorIndividuo = this.melhorDaPopulacao();
        melhorIndividuo.setPortfolio(new Portfolio());

        this.realizaNegociacoes(melhorIndividuo, diasTeste, empresaDiasTeste, true);

        this.setValorPortfolio(melhorIndividuo.getValorCaixaFinal());
        this.setMelhorAcao(melhorIndividuo.getPortfolio().getMelhorAcao());
        this.setPiorAcao(melhorIndividuo.getPortfolio().getPiorAcao());
    }

}
