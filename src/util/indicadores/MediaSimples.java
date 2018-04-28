/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util.indicadores;

import java.util.List;

/**
 *
 * @author fernando
 */
public class MediaSimples implements Indicador {

    private final List<Double> precos;
    private final int dias;

    public MediaSimples(List<Double> dados, int dias) {

        this.precos = dados;
        this.dias = dias;
    }

    @Override
    public double calcula() {
        double soma = 0;
        if(this.precos.size() < this.dias ){
            return 0;
        }
        for (int i = this.precos.size() -1; i > this.precos.size() - this.dias; i--) {
            soma += this.precos.get(i);
        }
        
        return soma / this.dias;
    }

}
