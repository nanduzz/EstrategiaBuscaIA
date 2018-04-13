package modelo;

import java.time.LocalDate;

/**
 *
 * @author Camargo
 */
public class Dia {

    String acao;
    LocalDate data;
    Double valorAbertura;
    Double valorFechamento;
    Double precoMin;
    Double precoMax;
    Double precoMedio;
    Double valorFechamentoAjustado;

    public Dia(String acao, LocalDate data, Double valorAbertura,
            Double valorFechamento, Double precoMin, Double precoMax,
            Double precoMedio, Double valorFechamentoAjustado) {
        this.acao = acao;
        this.data = data;
        this.valorAbertura = valorAbertura;
        this.valorFechamento = valorFechamento;
        this.precoMin = precoMin;
        this.precoMax = precoMax;
        this.precoMedio = precoMedio;
        this.valorFechamentoAjustado = valorFechamentoAjustado;
    }

}
