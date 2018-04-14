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
public class MediaMovelSimples implements Indicador {

    private final double[] precos;
    private final int diasRapido;
    private final int diasLento;

    public MediaMovelSimples(double[] precos, int diasRapido, int diasLento) {
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
        double mediaRapida = new MediaSimples(this.precos, this.diasRapido).calcula();
        double mediaLenta = new MediaSimples(this.precos, this.diasLento).calcula();

        return mediaRapida - mediaLenta;

    }

}
