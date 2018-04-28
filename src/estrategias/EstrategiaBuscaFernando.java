/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package estrategias;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import modelo.Dia;
import util.indicadores.RSI;

/**
 *
 *
 * FALTA ELITISMO RODAR AS GERAÇÕeS MUTAÇÔES MELHROAR A QUANTIDADE DE GENES ----
 * % de investimento por empresa podem ser 10 genes
 *
 * @author fernando
 */
public class EstrategiaBuscaFernando implements EstrategiaBusca {

    HashMap<String, List<Dia>> empresaDiasTreino = new HashMap<>(); //
    HashMap<String, List<Dia>> empresaDiasTeste = new HashMap<>();
    HashMap<String, List<Double>> diasPassados = new HashMap<>(); // siglaEmpresa / valor
    LinkedHashSet<String> diasTreino = new LinkedHashSet<>();
    LinkedHashSet<String> diasTeste = new LinkedHashSet<>();

    List<MembroPopulacao> populacao = new ArrayList<>();

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
        for (int i = 0; i < 10000; i++) {
            MembroPopulacao membroPopulacao = new MembroPopulacao();
            membroPopulacao.geraGenesAleatorios();

            this.realizaNegociacoes(membroPopulacao, this.diasTreino, this.empresaDiasTreino);
            this.populacao.add(membroPopulacao);
        }

        Collections.sort(populacao, (MembroPopulacao membro1, MembroPopulacao membro2) -> {
            if (membro1.getValorCaixaFinal() <= membro2.getValorCaixaFinal()) {
                return 1; //To change body of generated lambdas, choose Tools | Templates.
            } else {
                return -1;
            }
        });

        int i = 0;
        for (MembroPopulacao m : populacao) {
            System.out.println(m.getValorCaixaFinal());
            if (i > 5) {
                break;
            }
            i++;
        }

        this.realizaNegociacoes(this.populacao.get(0), diasTeste, empresaDiasTeste);
        System.out.println("FINAAL");
        System.out.println(this.populacao.get(0).getValorCaixaFinal());

    }

    private void realizaNegociacoes(MembroPopulacao membroPopulacao, LinkedHashSet<String> dias, HashMap<String, List<Dia>> dadosDias) {
        Portifolio portifolio = new Portifolio();
        this.diasPassados = new HashMap<>();
        for (String dia : dias) {
            for (Dia diaEmpresa : dadosDias.get(dia)) {
                String sigla = diaEmpresa.getSigla();
                if (!diasPassados.containsKey(sigla)) {
                    diasPassados.put(sigla, new ArrayList<>());
                }

//                double roc = new ROC(this.diasPassados.get(diaEmpresa.getSigla()), 5).calcula();
                double rsi = new RSI(this.diasPassados.get(sigla), (int) membroPopulacao.getPeriodoCalculoRSI()).calcula();

                if (rsi > 0 && rsi < membroPopulacao.getRSIMin() && !portifolio.possuiAcao(sigla)) {

                    double quantidadeAcoes = (membroPopulacao.getPorcentagemInvestimento(sigla) * portifolio.getValorEmCaixa()) / this.getUltimoValorFechamento(sigla);
                    int quantidadeCompra = (int) Math.floor(quantidadeAcoes);
                    portifolio.compraAcao(sigla, quantidadeCompra, this.getUltimoValorFechamento(sigla));
                } else if (rsi > membroPopulacao.getRSIMax() && portifolio.possuiAcao(sigla)) {
                    portifolio.vendeAcao(sigla, this.getUltimoValorFechamento(sigla));
                }

                diasPassados.get(sigla).add(diaEmpresa.getValorFechamento());
            }
        }

        
        //vende todas as ações para calcular valor em caixa
        for (String sigla : this.diasPassados.keySet()) {
            portifolio.vendeAcao(sigla, this.diasPassados.get(sigla).get(this.diasPassados.size() - 1));
        }
        membroPopulacao.setValorFinal(portifolio.valorEmCaixa);
    }

    @Override
    public String devolveValorPortfolio() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String devolveResultadosMesAMes() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String devolveAcaoMaiorGanho() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String devolveAcaoMaiorPrejuizo() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public double getUltimoValorFechamento(String sigla) {
        return this.diasPassados.get(sigla).get(this.diasPassados.get(sigla).size() - 1);
    }

    class Portifolio {

        private final HashMap<String, AcaoPossuida> carteira = new HashMap<>();
        private double valorEmCaixa = 100000D;

        public void iniciaEmpresa(String sigla) {
            this.carteira.put(sigla, new AcaoPossuida());
        }

        public void compraAcao(String sigla, int quantidadeComprada, double valor) {
            if (!this.carteira.containsKey(sigla)) {
                this.iniciaEmpresa(sigla);
            }
            double valorNegociacao = quantidadeComprada * valor;
            if (this.valorEmCaixa - valorNegociacao < 0) {
                return;
            }
            this.valorEmCaixa -= valorNegociacao;
            this.carteira.get(sigla).realizaCompra(quantidadeComprada, valor);
        }

        public boolean possuiAcao(String sigla) {
            return this.carteira.containsKey(sigla) && this.carteira.get(sigla).getQuantidade() > 0;
        }

        public void vendeAcao(String sigla, double valor) {
            if (!this.possuiAcao(sigla)) {
                return;
            }
            int quantidadePossuida = this.carteira.get(sigla).quantidade;
            this.valorEmCaixa += quantidadePossuida * valor;
            this.carteira.get(sigla).realizaVenda();
        }

        public double getValorEmCaixa() {
            return valorEmCaixa;
        }

        class AcaoPossuida {

            private int quantidade;
            private double valorCompra;

            public AcaoPossuida() {
                this.quantidade = 0;
                this.valorCompra = 0;
            }

            public void realizaCompra(int quantidade, double valor) {
                this.quantidade = quantidade;
                this.valorCompra = valor;
            }

            public void realizaVenda() {
                this.quantidade = 0;
                this.valorCompra = 0;
            }

            public int getQuantidade() {
                return quantidade;
            }

            public void setQuantidade(int quantidade) {
                this.quantidade = quantidade;
            }

            public double getValorCompra() {
                return valorCompra;
            }

            public void setValorCompra(double valorCompra) {
                this.valorCompra = valorCompra;
            }

        }

    }

    class MembroPopulacao {

        private static final int I_RSI_MIN = 10;
        private static final int I_RSI_MAX = 11;
        private static final int I_PERIODO_RSI = 12;

        private double[] genes = new double[13];

        private double valorCaixaFinal = 0;

        public void geraGenesAleatorios() {
            Random rand = new Random();
            this.genes[I_RSI_MAX] = rand.nextInt(100);
            this.genes[I_RSI_MIN] = rand.nextInt(100);
            if (this.genes[I_RSI_MAX] <= this.genes[I_RSI_MIN]) {
                this.geraGenesAleatorios();
            }
            this.genes[I_PERIODO_RSI] = ThreadLocalRandom.current().nextInt(2, 20 + 1);

            //Inicia a quantidade a ser investida por empresas do ENUM
            for (int i = 0; i < 10; i++) {
                this.genes[i] = rand.nextDouble() / 10;
            }

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

        private void setValorFinal(double valorEmCaixa) {
            this.valorCaixaFinal = valorEmCaixa;
        }

        public double getValorCaixaFinal() {
            return valorCaixaFinal;
        }

    }

    enum Empresas {
        WEGE3,
        NATU3,
        SBSP3,
        CIEL3,
        CSNA3,
        VIVT3,
        GRND3,
        JSLG3,
        LREN3,
        UGPA3;
    }

}
