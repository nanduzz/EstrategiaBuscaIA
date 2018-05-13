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
public class EmpresaDoPortfolio {
        
    public EmpresaDoPortfolio(EstrategiaBuscaMayza.SiglaEmpresas siglaEmpresas){
        this.siglaEmpresa = siglaEmpresas;
    }

    private EstrategiaBuscaMayza.SiglaEmpresas siglaEmpresa;
    private Integer quantidadeAcoesCompradas = 0;
    private Integer quantidadeAcoesVendidas = 0;
    private Double valorCompraAcao = 0D;
    private Double valorVendaAcao = 0D;
    private Double quantidadeFeromoniosInvestimento = 10D; 

    public EstrategiaBuscaMayza.SiglaEmpresas getSiglaEmpresa() {
        return siglaEmpresa;
    }

    public void setSiglaEmpresa(EstrategiaBuscaMayza.SiglaEmpresas siglaEmpresa) {
        this.siglaEmpresa = siglaEmpresa;
    }

    public Integer getQuantidadeAcoesCompradas() {
        return quantidadeAcoesCompradas;
    }

    public void setQuantidadeAcoesCompradas(Integer quantidadeAcoesCompradas) {
        this.quantidadeAcoesCompradas = quantidadeAcoesCompradas;
    }

    public Integer getQuantidadeAcoesVendidas() {
        return quantidadeAcoesVendidas;
    }

    public void setQuantidadeAcoesVendidas(Integer quantidadeAcoesVendidas) {
        this.quantidadeAcoesVendidas = quantidadeAcoesVendidas;
    }

    public Double getValorCompraAcao() {
        return valorCompraAcao;
    }

    public void setValorCompraAcao(Double valorCompraAcao) {
        this.valorCompraAcao = valorCompraAcao;
    }

    public Double getValorVendaAcao() {
        return valorVendaAcao;
    }

    public void setValorVendaAcao(Double valorVendaAcao) {
        this.valorVendaAcao = valorVendaAcao;
    }

    public Double getQuantidadeFeromoniosInvestimento() {
        return quantidadeFeromoniosInvestimento;
    }

    public void setQuantidadeFeromoniosInvestimento(Double quantidadeFeromoniosInvestimento) {
        this.quantidadeFeromoniosInvestimento = quantidadeFeromoniosInvestimento;
    }
}
