/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util.indicadores;

/**
 *
 * @author fernando
 */
public class MediaSimples implements Indicador {

    private final double[] precos;
    private final int dias;

    public MediaSimples(double[] dados, int dias) {

        this.precos = dados;
        this.dias = dias;
    }

    @Override
    public double calcula() {
        double soma = 0;
        for (int i = this.precos.length; i > this.precos.length - this.dias; i--) {
            soma += precos[i];
        }
        
        return soma / this.dias;
    }

}
