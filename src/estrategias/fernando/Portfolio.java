/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package estrategias.fernando;

import java.text.DecimalFormat;
import java.util.HashMap;

/**
 *
 * @author fernando
 */
public class Portfolio {

    private final HashMap<String, AcaoPossuida> carteira = new HashMap<>();
    private double valorEmCaixa = 100000D;

    private static final DecimalFormat DF = new DecimalFormat("#.##");
//    private int i  = 0;
//    private int iv  = 0;

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
//        i++;
//        System.out.println("compra: " + i);
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
        this.carteira.get(sigla).realizaVenda(valor);
//        iv++;
//        System.out.println("vende: " + iv);
    }

    public double getValorEmCaixa() {
        return valorEmCaixa;
    }

    public boolean deveVenderAcao(double rsi, Individuo membroPopulacao, String sigla, double ultimoValorFechamento) {
        return rsi > membroPopulacao.getRSIMax()
                && this.possuiAcao(sigla)
                && this.carteira.get(sigla).valorCompra < ultimoValorFechamento;
    }

    public boolean deveComprarAcao(double rsi, Individuo membroPopulacao, String sigla) {
        return rsi > 0
                && rsi < membroPopulacao.getRSIMin()
                && !this.possuiAcao(sigla);
    }

    public String getMelhorAcao() {
        double melhorAcao = Double.MIN_VALUE;
        String retorno = "";
        for (String sigla : this.carteira.keySet()) {
            if (this.carteira.get(sigla).getResultado() > melhorAcao) {
                melhorAcao = this.carteira.get(sigla).getResultado();
                retorno = "Melhor Ação: " + sigla + " - Com " + DF.format(melhorAcao);
            }
        }
        return retorno;

    }

    public double vendeAcaoMensal(String sigla, double valor) {
        if (!this.possuiAcao(sigla)) {
            return 0D;
        }
        return this.carteira.get(sigla).quantidade * valor;
    }

    public String getPiorAcao() {

        double pior = Double.MAX_VALUE;
        String retorno = "";
        for (String sigla : this.carteira.keySet()) {
            if (this.carteira.get(sigla).getResultado() < pior) {
                pior = this.carteira.get(sigla).getResultado();
                retorno = "Pior Ação: " + sigla + " - Com " + DF.format(pior);
            }
        }
        return retorno;
    }

    class AcaoPossuida {

        private int quantidade;
        private double valorCompra;
        private double resultado;

        public AcaoPossuida() {
            this.quantidade = 0;
            this.valorCompra = 0;
            this.resultado = 0;
        }

        public void realizaCompra(int quantidade, double valor) {
            this.quantidade = quantidade;
            this.valorCompra = valor;
            this.resultado -= valor * quantidade;
        }

        public void realizaVenda(double valorVenda) {
            this.resultado += this.quantidade * valorVenda;
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

        public double getResultado() {
            return this.resultado;
        }

    }

}
