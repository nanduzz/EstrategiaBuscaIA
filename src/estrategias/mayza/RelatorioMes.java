/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package estrategias.mayza;

import estrategias.EstrategiaBuscaMayza;

/**
 *
 * @author mayza
 */
public class RelatorioMes {
    private Double carteiraMes = 0D;
    private Double porcGanhoMesComRelacaoCarteiraInicial = 0D;
    private Double porcGanhoMesComRelacaoCarteiraAnterior = 0D;
    private EstrategiaBuscaMayza.SiglaEmpresas acaoMaiorGanhoMes;
    private EstrategiaBuscaMayza.SiglaEmpresas acaoMenorGanhoMes;
    private Double valorMaiorGanho = 0D;
    private Double valorMenorGanho = 0D;

    public Double getCarteiraMes() {
        return carteiraMes;
    }

    public void setCarteiraMes(Double carteiraMes) {
        this.carteiraMes = carteiraMes;
    }

    public Double getPorcGanhoMesComRelacaoCarteiraInicial() {
        return porcGanhoMesComRelacaoCarteiraInicial;
    }

    public void setPorcGanhoMesComRelacaoCarteiraInicial(Double porcGanhoMesComRelacaoCarteiraInicial) {
        this.porcGanhoMesComRelacaoCarteiraInicial = porcGanhoMesComRelacaoCarteiraInicial;
    }

    public Double getPorcGanhoMesComRelacaoCarteiraAnterior() {
        return porcGanhoMesComRelacaoCarteiraAnterior;
    }

    public void setPorcGanhoMesComRelacaoCarteiraAnterior(Double porcGanhoMesComRelacaoCarteiraAnterior) {
        this.porcGanhoMesComRelacaoCarteiraAnterior = porcGanhoMesComRelacaoCarteiraAnterior;
    }

    public EstrategiaBuscaMayza.SiglaEmpresas getAcaoMaiorGanhoMes() {
        return acaoMaiorGanhoMes;
    }

    public void setAcaoMaiorGanhoMes(EstrategiaBuscaMayza.SiglaEmpresas acaoMaiorGanhoMes) {
        this.acaoMaiorGanhoMes = acaoMaiorGanhoMes;
    }

    public EstrategiaBuscaMayza.SiglaEmpresas getAcaoMenorGanhoMes() {
        return acaoMenorGanhoMes;
    }

    public void setAcaoMenorGanhoMes(EstrategiaBuscaMayza.SiglaEmpresas acaoMenorGanhoMes) {
        this.acaoMenorGanhoMes = acaoMenorGanhoMes;
    }

    public Double getValorMaiorGanho() {
        return valorMaiorGanho;
    }

    public void setValorMaiorGanho(Double valorMaiorGanho) {
        this.valorMaiorGanho = valorMaiorGanho;
    }

    public Double getValorMenorGanho() {
        return valorMenorGanho;
    }

    public void setValorMenorGanho(Double valorMenorGanho) {
        this.valorMenorGanho = valorMenorGanho;
    }
}
