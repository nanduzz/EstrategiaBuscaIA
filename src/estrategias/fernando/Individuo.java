/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package estrategias.fernando;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author fernando
 */
public class Individuo {

    private static final int I_RSI_MIN = 10;
    private static final int I_RSI_MAX = 11;
    private static final int I_PERIODO_RSI = 12;

    private double[] genes = new double[13];

    private Portfolio portfolio = new Portfolio();

    public void geraGenesAleatorios() {

        Random rand = new Random();
        this.genes[I_RSI_MAX] = rand.nextInt(100);
        this.genes[I_RSI_MIN] = rand.nextInt(100);
        if (this.genes[I_RSI_MAX] <= this.genes[I_RSI_MIN]) {
            this.geraGenesAleatorios();
        }
        this.genes[I_PERIODO_RSI] = ThreadLocalRandom.current().nextInt(2, 20 + 1);

        //Inicia a quantidade a ser investida por empresas do ENUM
        double somaInvestimento = 0;
        for (int i = 0; i < 10; i++) {
            this.genes[i] = rand.nextDouble() / 10;
            somaInvestimento += this.genes[i];
        }
        double rateioInvestimento = somaInvestimento / 1;
        for (int i = 0; i < 10; i++) {
            this.genes[i] /= rateioInvestimento;
        }
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(Portfolio portfolio) {
        this.portfolio = portfolio;
    }

    public double getRSIMin() {
        return this.genes[I_RSI_MIN];
    }

    public double getRSIMax() {
        return this.genes[I_RSI_MAX];
    }

    public double getPeriodoCalculoRSI() {
        return this.genes[I_PERIODO_RSI];
    }

    public double getPorcentagemInvestimento(String sigla) {
        return this.genes[Empresas.valueOf(sigla).ordinal()];
    }

    public double getValorCaixaFinal() {
        return this.portfolio.getValorEmCaixa();
    }

    public static Individuo crossOver(Individuo membroPopulacao1, Individuo membroPopulacao2) {
        Random rand = new Random();
        Individuo resultado = new Individuo();
        double somaTotalInvestimento = 0;
        for (int i = 0; i < resultado.genes.length; i++) {
            resultado.genes[i] = rand.nextInt(1) == 1 ? membroPopulacao1.genes[i] : membroPopulacao2.genes[i];
            if (i < 10) {
                somaTotalInvestimento += resultado.genes[i];
            }
        }

        for (int i = 0; i < 10; i++) {
            resultado.genes[i] /= somaTotalInvestimento;
        }

        return resultado;
    }

    public static Individuo selecionaParaCrossOver(List<Individuo> populacao) {
        Random rand = new Random();
        return populacao.get(rand.nextInt(50));
    }

    public boolean deveVenderAcao(double rsi, String sigla, double ultimoValorFechamento) {
        return this.portfolio.deveVenderAcao(rsi, this, sigla, ultimoValorFechamento);
    }

    public boolean deveComprarAcao(double rsi, String sigla) {
        return this.portfolio.deveComprarAcao(rsi, this, sigla);
    }

    public void compraAcoes(String sigla, double ultimoValorFechamento) {
        double quantidadeAcoes = calculaQuantidadeAcoes(sigla, ultimoValorFechamento);
        int quantidadeCompra = (int) Math.floor(quantidadeAcoes);
        portfolio.compraAcao(sigla, quantidadeCompra, ultimoValorFechamento);
    }

    private double calculaQuantidadeAcoes(String sigla, double ultimoValorFechamento) {
        return (this.getPorcentagemInvestimento(sigla) * portfolio.getValorEmCaixa())
                / ultimoValorFechamento;

    }
//

    public void vendeTodasAcoes(HashMap<String, List<Double>> diasPassados) {
        for (String sigla : diasPassados.keySet()) {
            portfolio.vendeAcao(sigla, diasPassados.get(sigla).get(diasPassados.size() - 1));
        }
    }

    public void vendeAcao(String sigla, double ultimoValorFechamento) {
        this.portfolio.vendeAcao(sigla, ultimoValorFechamento);
    }

    public double calculaDadosMensal(String mesTmp, HashMap<String, List<Double>> diasPassados) {
        double tmpCaixaMomento = this.getValorCaixaFinal();
        for (String sigla : diasPassados.keySet()) {
            tmpCaixaMomento += portfolio.vendeAcaoMensal(sigla, diasPassados.get(sigla).get(diasPassados.size() - 1));
        }

        return tmpCaixaMomento;
    }

}
