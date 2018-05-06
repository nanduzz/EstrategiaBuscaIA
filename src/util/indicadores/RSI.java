/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util.indicadores;

import java.util.List;

/**
 * GERALMENE USADO COM 14 DIAS
 * RSI indica se uma ação está Superavaliada (70%), ou sobAvaliada(30%)
 *
 * @author fernando
 */
public class RSI implements Indicador {

    private final int periodo;
    private final List<Double> precos;

    public RSI( List<Double> precos, int periodo) {
        this.precos = precos;
        this.periodo = periodo;
    }

    @Override
    public double calcula() {
        if(precos != null && precos.size() < periodo){
            return 0;
        }
        int ultimoDia = precos.size() - 1;
        int primeiroDia = ultimoDia - this.periodo + 1;

        double lucroMedio = 0, prejuizoMedio = 0;
        for (int dia = primeiroDia + 1; dia <= ultimoDia; dia++) {
            double alteracao = precos.get(dia) - precos.get(dia -1 );
            if (alteracao >= 0) {
                lucroMedio += alteracao;
            } else {
                prejuizoMedio += alteracao;
            }
        }

        double rs = lucroMedio / Math.abs(prejuizoMedio);
        double rsi = 100 - (100 / (1 + rs));

        return rsi;

    }

}
