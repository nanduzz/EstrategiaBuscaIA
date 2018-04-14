/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util.indicadores;

/**
 * Rate Of Change
 * Porcentagem de alteração de preco entre -1.0 e 1.0
 * @author fernando
 */
public class ROC implements Indicador {

    private final double[] precos;
    private final int diasAnteriores;

    public ROC(double[] precos, int diasAnteriores) {
        this.precos = precos;
        this.diasAnteriores = diasAnteriores;

    }

    @Override
    public double calcula() {
        double precoRecente = precos[precos.length - 1];
        double precoComparacao = precos[precos.length - this.diasAnteriores - 1];

        return (precoRecente - precoComparacao) / precoComparacao;

    }

}
