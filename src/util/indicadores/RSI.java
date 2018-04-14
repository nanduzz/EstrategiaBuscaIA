/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util.indicadores;

/**
 * GERALMENE USADO COM 14 DIAS
 * RSI indica se uma ação está Superavaliada (70%), ou sobAvaliada(30%)
 *
 * @author fernando
 */
public class RSI implements Indicador {

    private final int periodo;
    private final double[] precos;

    public RSI(double[] precos, int periodo) {
        this.precos = precos;
        this.periodo = periodo;
    }

    @Override
    public double calcula() {
        int ultimoDia = precos.length - 1;
        int primeiroDia = ultimoDia - this.periodo + 1;

        double lucroMedio = 0, prejuizoMedio = 0;
        for (int dia = primeiroDia + 1; dia <= ultimoDia; dia++) {
            double alteracao = precos[dia] - precos[dia - 1];
            if (alteracao >= 0) {
                lucroMedio += alteracao;
            } else {
                prejuizoMedio += alteracao;
            }
        }

        double rs = lucroMedio / Math.abs(prejuizoMedio);
        double rsi = 100 - 100 / (1 + rs);

        return rsi;

    }

}
