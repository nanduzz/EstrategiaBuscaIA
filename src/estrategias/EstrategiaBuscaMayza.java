/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package estrategias;

import estrategias.mayza.EmpresaDoPortfolio;
import estrategias.mayza.RelatorioAno;
import estrategias.mayza.RelatorioMes;
import java.text.DecimalFormat;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import modelo.Dia;
import util.indicadores.RSI;

/**
 *
 * @author mayza
 */
public class EstrategiaBuscaMayza implements EstrategiaBusca {
    
    private final Locale BRAZIL = new Locale("pt","BR");
    private final DecimalFormat df = new DecimalFormat("###,##0.00");
    private static final Integer QTD_DIAS_UTEIS_2016 = 249;
    private static final Integer QTD_DIAS_UTEIS_2014_2015 = 488;
    private static final Integer RSI_COMPRA = 30;
    private static final Integer RSI_VENDA = 70;
    private static final Integer DEVO_COMPRAR = 1;
    private static final Integer DEVO_VENDER = 0;
    private static final Integer NAO_DEVO_MOVIMENTAR = -1;
    private static final Integer PERIODO_RSI = 7;
    private static final Double CARTEIRA_INICIAL = 100000D;
    private static final Double FEROMONIO_INICIAL_POR_EMPRESA = 10D;
    private static final Double PORCENTAGEM_EVAPORACAO_FEROMONIO = 0.1;
    private static final Double PORCENTAGEM_PERCA_FEROMONIO_PREJUIZO = 0.2;
      
    /*DADOS HISTORICOS*/
    private final Map<String,List<Dia>> historico2014e2015 = new HashMap<>();
    private final Map<String,List<Dia>> historico2016 = new HashMap<>();
    private final Map<SiglaEmpresas,List<Double>> fechamentos = new HashMap<>();
    private final Map<SiglaEmpresas,Double> aberturaUltimoDiaMes = new HashMap<>();
    
    /*MOVIMENTAÇÕES*/
    private Double carteiraAtual;
    private Double carteiraMesAnterior; //para o cálculo Mês a Mês
    private Double carteiraVirtual; //para o cálculo Mês a Mês
    
    private Double feromoniosParaDistribuir = 0D;
    private Double feromoniosEvaporados = 0D;
    private final List<SiglaEmpresas> empresasEvaporadas = new ArrayList<>();
    private final Map<SiglaEmpresas,Double> empresasReceberFeromonios = new HashMap<>();   
    private final Map<SiglaEmpresas,EmpresaDoPortfolio> portfolio = new HashMap<>();
           
    /*RELATÓRIOS*/
    private final RelatorioAno relatorioAno = new RelatorioAno();
    private final Map<Integer,RelatorioMes> relatorioMesAMes = new HashMap<>(); //chave = mês
    private final Map<SiglaEmpresas,Double> ganhoMensalPorEmpresa = new HashMap<>();
    private final Map<SiglaEmpresas,Double> ganhoAnualPorEmpresa = new HashMap<>();
    
    public EstrategiaBuscaMayza(){
        this.carteiraAtual = CARTEIRA_INICIAL;
        this.carteiraMesAnterior = CARTEIRA_INICIAL;
        this.carteiraVirtual = CARTEIRA_INICIAL;
        for(SiglaEmpresas sigla: SiglaEmpresas.values()){
            this.historico2014e2015.put(sigla.toString(), new ArrayList<>());
            this.historico2016.put(sigla.toString(), new ArrayList<>());
            this.portfolio.put(sigla, new EmpresaDoPortfolio(sigla));
            this.fechamentos.put(sigla, new ArrayList<>());
            this.ganhoMensalPorEmpresa.put(sigla, 0D);
            this.ganhoAnualPorEmpresa.put(sigla, 0D);
        }
        for(Integer i=1;i<13;i++){
            this.relatorioMesAMes.put(i, new RelatorioMes());
        }
    }

    @Override
    public void recebeDadosTreino(List<Dia> diasTreino) {
        diasTreino.forEach((dia) -> {
            historico2014e2015.get(dia.getSigla()).add(dia);
        });
    }

    @Override
    public void recebeDadosTeste(List<Dia> diasTeste) {
        diasTeste.forEach((dia) -> {
            historico2016.get(dia.getSigla()).add(dia);
        });
    }

    @Override
    public void aplicaEstrategiaBusca() {
        algoritmoTreino();
        iniciaNovoAno();
        algoritmoTeste();
    }
    
    private void algoritmoTreino(){
        Integer diaAtual = 0;
        Integer indicadorMovimentacao;
        Double lucroEmpresaDia;
        Dia dia;
        
        while(diaAtual < QTD_DIAS_UTEIS_2014_2015){
            for(SiglaEmpresas sigla: SiglaEmpresas.values()){
                if(diaAtual < historico2014e2015.get(sigla.toString()).size()){
                    dia = historico2014e2015.get(sigla.toString()).get(diaAtual);
                    indicadorMovimentacao = devoComprarVenderOuNaoMovimentar(sigla);
                    if(indicadorMovimentacao.equals(DEVO_COMPRAR)){
                        lucroEmpresaDia = comprarAcoes(sigla,dia);
                        calculaPorcentagemPercaOuGanhoDeFeromonios(sigla,lucroEmpresaDia);
                    } else if (indicadorMovimentacao.equals(DEVO_VENDER)){
                        lucroEmpresaDia = venderAcoes(sigla,dia);
                        calculaPorcentagemPercaOuGanhoDeFeromonios(sigla,lucroEmpresaDia);
                    } else {
                        naoMovimentarAcoes(sigla);
                    }
                    fechamentos.get(sigla).add(dia.getValorFechamento());
                }
            } 
            recalculaFeromonios();
            diaAtual++;
        }
    }
    
    private void algoritmoTeste(){
        Integer diaAtual = 0;
        Integer indicadorMovimentacao;       
        Double lucroEmpresaDia = 0D;
        Boolean isFinalMes = Boolean.FALSE;
        Dia dia = null;
        
        while(diaAtual < QTD_DIAS_UTEIS_2016){
            for(SiglaEmpresas sigla: SiglaEmpresas.values()){
                dia = historico2016.get(sigla.toString()).get(diaAtual);
                indicadorMovimentacao = devoComprarVenderOuNaoMovimentar(sigla);
                if(indicadorMovimentacao.equals(DEVO_COMPRAR)){
                    lucroEmpresaDia = comprarAcoes(sigla,dia);
                    calculaPorcentagemPercaOuGanhoDeFeromonios(sigla,lucroEmpresaDia);
                } else if (indicadorMovimentacao.equals(DEVO_VENDER)){
                    lucroEmpresaDia = venderAcoes(sigla,dia);
                    calculaPorcentagemPercaOuGanhoDeFeromonios(sigla,lucroEmpresaDia);
                } else {
                    naoMovimentarAcoes(sigla);
                }
                fechamentos.get(sigla).add(dia.getValorFechamento());               
                isFinalMes = atualizaGanhosEmpresaMes(sigla, lucroEmpresaDia, diaAtual);
                
                if(isFinalMes){ aberturaUltimoDiaMes.put(sigla, dia.getValorAbertura()); }
                
                atualizaGanhosEmpresaAno(sigla, lucroEmpresaDia);
                
                lucroEmpresaDia = 0D;
            }
            if(isFinalMes) { 
                atualizaRelatorioMesAMes(dia.getData().getMonthValue());
            }
            recalculaFeromonios();
            diaAtual++;
        }
        atualizaRelatorioAnual();
    }
    
    private Integer devoComprarVenderOuNaoMovimentar(SiglaEmpresas sigla){
        Double rsi = new RSI(this.fechamentos.get(sigla), PERIODO_RSI).calcula();
        if(rsi < RSI_COMPRA && rsi != 0){
            return DEVO_COMPRAR;
        } else if(rsi > RSI_VENDA){
            return DEVO_VENDER;
        } 
        return NAO_DEVO_MOVIMENTAR;
    }
    
    private Double comprarAcoes(SiglaEmpresas sigla, Dia dia){
        EmpresaDoPortfolio empresa;
        Double feromonioInvestimento;
        Double valorMaximoInvestimento;
        Double qtdMaximaAcoes;
        Double sobra;
        Double lucroSobreFechamento;
        
        empresa = portfolio.get(sigla);
        if(!(empresa.getQuantidadeAcoesCompradas() > 0)){
            feromonioInvestimento = empresa.getQuantidadeFeromoniosInvestimento();
            valorMaximoInvestimento = ((carteiraAtual*feromonioInvestimento)/100);
            qtdMaximaAcoes = valorMaximoInvestimento/dia.getValorAbertura();
            sobra = valorMaximoInvestimento - (qtdMaximaAcoes.intValue()*dia.getValorAbertura());

            carteiraAtual = carteiraAtual - (valorMaximoInvestimento-sobra);
            carteiraVirtual = carteiraAtual;
            empresa.setQuantidadeAcoesCompradas(qtdMaximaAcoes.intValue());
            empresa.setValorCompraAcao(dia.getValorAbertura());
            lucroSobreFechamento = (qtdMaximaAcoes.intValue()*dia.getValorFechamento()) - (qtdMaximaAcoes.intValue()*empresa.getValorCompraAcao());
        } else {
            lucroSobreFechamento = 0D; 
        }      
        return lucroSobreFechamento;
    } 
    
    private Double venderAcoes(SiglaEmpresas sigla, Dia dia){
        EmpresaDoPortfolio empresa;
        Double valorVendido;
        Integer qtdVendido;
        Double lucroSobreFechamento;
        
        empresa = portfolio.get(sigla);
        if(!(empresa.getQuantidadeAcoesCompradas() == 0)){
            valorVendido = dia.getValorAbertura();
            qtdVendido = empresa.getQuantidadeAcoesCompradas();
            carteiraAtual = carteiraAtual + (empresa.getQuantidadeAcoesCompradas()*dia.getValorAbertura());
            carteiraVirtual = carteiraAtual;
            empresa.setQuantidadeAcoesCompradas(0);
            empresa.setValorCompraAcao(0D);
            empresa.setQuantidadeAcoesVendidas(qtdVendido);
            empresa.setValorVendaAcao(valorVendido);
            lucroSobreFechamento = (qtdVendido*valorVendido) - qtdVendido*dia.getValorFechamento();
        } else {
            lucroSobreFechamento = 0D; 
        }
        
        return lucroSobreFechamento;
    } 
    
    private void naoMovimentarAcoes(SiglaEmpresas sigla){
        EmpresaDoPortfolio empresa;
        Double feromoniosEmpresa;
        Double evaporar;
        
        empresa = portfolio.get(sigla);
        feromoniosEmpresa = empresa.getQuantidadeFeromoniosInvestimento();
        
        evaporar = feromoniosEmpresa*PORCENTAGEM_EVAPORACAO_FEROMONIO;
        empresa.setQuantidadeFeromoniosInvestimento(feromoniosEmpresa - evaporar);
        feromoniosEvaporados = feromoniosEvaporados + evaporar;
        empresasEvaporadas.add(sigla);
    } 
    
    private void calculaPorcentagemPercaOuGanhoDeFeromonios(SiglaEmpresas sigla, Double lucroMovimentacao){
        EmpresaDoPortfolio empresa;
        Double feromoniosEmpresa;
        Double feromonioPerdido;
        
        empresa = portfolio.get(sigla);
        feromoniosEmpresa = empresa.getQuantidadeFeromoniosInvestimento();
        
        if(lucroMovimentacao > 0){
            empresasReceberFeromonios.put(sigla, 0D);
        } else if(lucroMovimentacao < 0){
            feromonioPerdido = feromoniosEmpresa*PORCENTAGEM_PERCA_FEROMONIO_PREJUIZO;
            empresa.setQuantidadeFeromoniosInvestimento(feromoniosEmpresa - feromonioPerdido);
            feromoniosParaDistribuir = feromoniosParaDistribuir + feromonioPerdido;
        } 
    }
    
    private void recalculaFeromonios(){
        /*Divide a evaporação entre as empresas que foram movimentadas, independente do lucro ou prejuizo*/
        Double qtdFeromoniosEvaporadosParaCadaEmpresa;
        Double qtdFeromoniosMerecido;
        EmpresaDoPortfolio empresa;
        Double totalFeromonios;
        
        if(empresasEvaporadas.size() < 10){
            qtdFeromoniosEvaporadosParaCadaEmpresa = feromoniosEvaporados/(10-empresasEvaporadas.size());
        } else {
            qtdFeromoniosEvaporadosParaCadaEmpresa = feromoniosEvaporados/10;
        }
        
        for(SiglaEmpresas sigla: SiglaEmpresas.values()){
            if(empresasReceberFeromonios.isEmpty()){
                empresasReceberFeromonios.put(sigla, 10D);
            } 
            if(empresasReceberFeromonios.containsKey(sigla)){
                empresasReceberFeromonios.put(sigla, (100.0/empresasReceberFeromonios.size()));
                qtdFeromoniosMerecido = (feromoniosParaDistribuir*empresasReceberFeromonios.get(sigla))/100;
                empresa = portfolio.get(sigla);
                totalFeromonios = empresa.getQuantidadeFeromoniosInvestimento();
                empresa.setQuantidadeFeromoniosInvestimento(totalFeromonios + qtdFeromoniosMerecido);
            }
        }
        for(SiglaEmpresas sigla: SiglaEmpresas.values()){
            if(!empresasEvaporadas.contains(sigla) || empresasEvaporadas.size() == 10){
               empresa = portfolio.get(sigla);
               totalFeromonios = empresa.getQuantidadeFeromoniosInvestimento();
               empresa.setQuantidadeFeromoniosInvestimento(totalFeromonios + qtdFeromoniosEvaporadosParaCadaEmpresa);
            }
        }
        feromoniosEvaporados = 0D;
        feromoniosParaDistribuir = 0D;
        empresasEvaporadas.clear();
        empresasReceberFeromonios.clear();
    }
    
    private Boolean atualizaGanhosEmpresaMes(SiglaEmpresas sigla, Double lucroEmpresaDia, Integer diaAtual){
        Double ganhosEmpresa;
        Boolean isFinalMes;
        Dia diaHoje;
        Dia proximoDia;
        
        diaHoje = historico2016.get(sigla.toString()).get(diaAtual);
        if(diaAtual+1 < QTD_DIAS_UTEIS_2016){
            proximoDia = historico2016.get(sigla.toString()).get(diaAtual+1);
            isFinalMes = diaHoje.getData().getMonthValue() != proximoDia.getData().getMonthValue();                    
        } else {
            isFinalMes = (diaAtual + 1) == QTD_DIAS_UTEIS_2016;
        }                  
        ganhosEmpresa = ganhoMensalPorEmpresa.get(sigla);
        ganhosEmpresa = ganhosEmpresa + lucroEmpresaDia;
        ganhoMensalPorEmpresa.put(sigla, ganhosEmpresa);
        
        return isFinalMes;
    }
    
    private void atualizaRelatorioMesAMes(Integer mes){
        RelatorioMes relatorioMes;
        Double ganhoTemp;
        Double maiorGanho = Double.NEGATIVE_INFINITY;
        Double menorGanho = Double.MAX_VALUE;
        SiglaEmpresas siglaMaiorGanho = null;
        SiglaEmpresas siglaMenorGanho = null;
        Double porcRelacaoInicial;
        Double porcRelacaoAnterior;
  
        relatorioMes = relatorioMesAMes.get(mes);
        /*CALCULA EMPRESAS COM MAIOR E MENOR GANHOS NO MES*/
        for(SiglaEmpresas sigla: SiglaEmpresas.values()){
            ganhoTemp = ganhoMensalPorEmpresa.get(sigla);
            maiorGanho = Math.max(ganhoTemp, maiorGanho);
            menorGanho = Math.min(ganhoTemp, menorGanho);
            if(Objects.equals(ganhoTemp, maiorGanho)){ siglaMaiorGanho = sigla; } 
            if(Objects.equals(ganhoTemp, menorGanho)){ siglaMenorGanho = sigla; }
        }
        for(SiglaEmpresas sigla: SiglaEmpresas.values()){ 
            /*Zera ganhos mensais da empresa*/
            ganhoMensalPorEmpresa.put(sigla, 0D);
        }
        /*CALCULA CARTEIRA DO MES COM RELACAO A CARTEIRA INICIAL E ANTERIOR*/
        EmpresaDoPortfolio empresa;
        Double valorAberturaUltimoDia;
        for(SiglaEmpresas sigla: SiglaEmpresas.values()){ 
            empresa = portfolio.get(sigla);
            valorAberturaUltimoDia = aberturaUltimoDiaMes.get(sigla);
            carteiraVirtual = carteiraVirtual + (empresa.getQuantidadeAcoesCompradas()*valorAberturaUltimoDia);
        }
        
        porcRelacaoInicial = ((carteiraVirtual*100)/CARTEIRA_INICIAL)-100;
        porcRelacaoAnterior = ((carteiraVirtual*100)/carteiraMesAnterior)-100;
        
        relatorioMes.setAcaoMaiorGanhoMes(siglaMaiorGanho);
        relatorioMes.setAcaoMenorGanhoMes(siglaMenorGanho);
        relatorioMes.setValorMaiorGanho(maiorGanho);
        relatorioMes.setValorMenorGanho(menorGanho);
        relatorioMes.setCarteiraMes(carteiraVirtual);
        relatorioMes.setPorcGanhoMesComRelacaoCarteiraInicial(porcRelacaoInicial);
        relatorioMes.setPorcGanhoMesComRelacaoCarteiraAnterior(porcRelacaoAnterior);
        
        carteiraMesAnterior = carteiraVirtual;
        carteiraVirtual = carteiraAtual;
    }
    
    private void atualizaGanhosEmpresaAno(SiglaEmpresas sigla, Double lucroEmpresaDia){
        Double ganhosEmpresa;
                       
        ganhosEmpresa = ganhoAnualPorEmpresa.get(sigla);
        ganhosEmpresa = ganhosEmpresa + lucroEmpresaDia;
        ganhoAnualPorEmpresa.put(sigla, ganhosEmpresa);
    }
    
    private void atualizaRelatorioAnual(){
        Double ganhoTemp;
        Double maiorGanho = Double.NEGATIVE_INFINITY;
        Double menorGanho = Double.MAX_VALUE;
        SiglaEmpresas siglaMaiorGanho = null;
        SiglaEmpresas siglaMenorGanho = null;
        Double porcRelacaoInicial;
        
        /*CALCULA EMPRESAS COM MAIOR E MENOR GANHOS NO ANO*/
        for(SiglaEmpresas sigla: SiglaEmpresas.values()){
            ganhoTemp = ganhoAnualPorEmpresa.get(sigla);
            maiorGanho = Math.max(ganhoTemp, maiorGanho);
            menorGanho = Math.min(ganhoTemp, menorGanho);
            if(Objects.equals(ganhoTemp, maiorGanho)){ siglaMaiorGanho = sigla; } 
            if(Objects.equals(ganhoTemp, menorGanho)){ siglaMenorGanho = sigla; }
        }
        
        /*CALCULA CARTEIRA DO ANO COM RELACAO A CARTEIRA INICIAL*/
        EmpresaDoPortfolio empresa;
        Double valorAberturaUltimoDia;
        for(SiglaEmpresas sigla: SiglaEmpresas.values()){ 
            empresa = portfolio.get(sigla);
            valorAberturaUltimoDia = aberturaUltimoDiaMes.get(sigla);
            carteiraAtual = carteiraAtual + (empresa.getQuantidadeAcoesCompradas()*valorAberturaUltimoDia);
        }
        
        porcRelacaoInicial = ((carteiraAtual*100)/CARTEIRA_INICIAL)-100;
        
        relatorioAno.setAcaoMaiorGanhoAno(siglaMaiorGanho);
        relatorioAno.setAcaoMenorGanhoAno(siglaMenorGanho);
        relatorioAno.setValorMaiorGanho(maiorGanho);
        relatorioAno.setValorMenorGanho(menorGanho);
        relatorioAno.setCarteiraFinal(carteiraAtual);
        relatorioAno.setPorcGanhoAnoComRelacaoCarteiraInicial(porcRelacaoInicial);
        
    }
    
    private void iniciaNovoAno(){
        carteiraAtual = CARTEIRA_INICIAL;
        carteiraVirtual = CARTEIRA_INICIAL;
        carteiraMesAnterior = CARTEIRA_INICIAL;
        for(SiglaEmpresas sigla: SiglaEmpresas.values()){
            fechamentos.put(sigla, new ArrayList<>());
            reiniciaPortfolioEmpresa(sigla);
        }
    }
    
    private void reiniciaPortfolioEmpresa(SiglaEmpresas sigla){
        EmpresaDoPortfolio empresa;
        empresa = portfolio.get(sigla);
        
        empresa.setQuantidadeAcoesCompradas(0);
        empresa.setQuantidadeAcoesVendidas(0);
        empresa.setValorCompraAcao(0D);
        empresa.setValorVendaAcao(0D);
    }

    @Override
    public String devolveValorPortfolio() {
        StringBuilder string = new StringBuilder();
        string.append("x-----------------x\n");
        string.append("| RESULTADO FINAL |\n");
        string.append("x-----------------x\n\n");
        string.append("O investimento de " + df.format(CARTEIRA_INICIAL) + " foi transformado em " + df.format(relatorioAno.getCarteiraFinal())
                + ". Isto equivale a um aumento de " + df.format(relatorioAno.getPorcGanhoAnoComRelacaoCarteiraInicial()) + "%.\n");
        return string.toString();
    }

    @Override
    public String devolveResultadosMesAMes() {
        StringBuilder string = new StringBuilder();
        RelatorioMes relatorioMes;
        string.append("x-----------------------------------x\n");
        string.append("| RESULTADO MENSAL DE INVESTIMENTOS |\n");
        string.append("x-----------------------------------x\n\n");
        for(Month mes: Month.values()){
            relatorioMes = relatorioMesAMes.get(mes.getValue());
            string.append("| ").append(mes.getDisplayName(TextStyle.FULL, BRAZIL).toUpperCase()).append(" |\n");
            string.append("Carteira: ").append(df.format(relatorioMes.getCarteiraMes())).append(" -> Aprox. ").append(df.format(relatorioMes.getPorcGanhoMesComRelacaoCarteiraAnterior())).append("% em relação a carteira anterior e ").append(df.format(relatorioMes.getPorcGanhoMesComRelacaoCarteiraInicial())).append("% em relação a carteira inicial.\n");
            string.append("Maior Movimentação: ").append(relatorioMes.getAcaoMaiorGanhoMes()).append(" com movimentação de ").append(df.format(relatorioMes.getValorMaiorGanho())).append("\n");
            string.append("Menor Movimentação: ").append(relatorioMes.getAcaoMenorGanhoMes()).append(" com movimentação de ").append(df.format(relatorioMes.getValorMenorGanho())).append("\n\n");
        }
        return string.toString();
    }

    @Override
    public String devolveAcaoMaiorGanho() {
        return "MAIOR GANHO: " + relatorioAno.getAcaoMaiorGanhoAno() + " com ganho total de " + df.format(relatorioAno.getValorMaiorGanho());
    }

    @Override
    public String devolveAcaoMaiorPrejuizo() {
        return "MENOR GANHO: " + relatorioAno.getAcaoMenorGanhoAno() + " com movimentação total de " + df.format(relatorioAno.getValorMenorGanho());
    }
  
    public enum SiglaEmpresas {
        WEGE3,
        NATU3,
        SBSP3,
        CIEL3,
        CSNA3,
        VIVT3,
        GRND3,
        JSLG3,
        LREN3,
        UGPA3;
    }
}
