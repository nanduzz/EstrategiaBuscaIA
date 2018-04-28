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
public class MediaMovelSimples implements Indicador {

    private final List<Double> precos;
    private final int diasRapido;
    private final int diasLento;

    public MediaMovelSimples(List<Double>precos, int diasRapido, int diasLento) {
        this.precos = precos;
        this.diasRapido = diasRapido;
        this.diasLento = diasLento;
    }

    /**
     * 
     * @return 
     */
    @Override
    public double calcula() {
        if(this.precos.size() < this.diasLento){
            return 0;
        }
        double mediaRapida = new MediaSimples(this.precos, this.diasRapido).calcula();
        double mediaLenta = new MediaSimples(this.precos, this.diasLento).calcula();

        return mediaRapida - mediaLenta;

    }

}
