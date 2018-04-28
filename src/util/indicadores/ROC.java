/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util.indicadores;

import java.util.List;

/**
 * Rate Of Change Porcentagem de alteração de preco entre -1.0 e 1.0
 *
 * @author fernando
 */
public class ROC implements Indicador {

    private final List<Double> precos;
    private final int diasAnteriores;

    public ROC(List<Double> precos, int diasAnteriores) {
        this.precos = precos;
        this.diasAnteriores = diasAnteriores;

    }

    @Override
    public double calcula() {
        if (this.precos.size() -1 < this.diasAnteriores ) {
            return 0;
        }
        double precoRecente = precos.get(precos.size() - 1);
        double precoComparacao = precos.get(precos.size() - this.diasAnteriores - 1);

        return (precoRecente - precoComparacao) / precoComparacao;

    }

}
