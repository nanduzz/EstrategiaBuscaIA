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
public class MediaMovelExponencial implements Indicador {

    private final int dias;
    private final double[] precos;

    public MediaMovelExponencial(double[] precos, int dias) {
        this.precos = precos;
        this.dias = dias;
    }

    @Override
    public double calcula() {
        if (this.dias >= 0) {
            double mmeMenosUm = new MediaMovelExponencial(precos, this.dias - 1).calcula();
            double mme = mmeMenosUm + (2 / (this.dias + 1)) * (/*P(this.dias)*/-mmeMenosUm);
            return mme;
        } else {
            return 0;
        }
    }
}
