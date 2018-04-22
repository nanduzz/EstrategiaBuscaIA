/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package estrategias;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import modelo.Dia;
import util.CarregaDados;

/**
 *
 * @author Camargo
 */
public class EstrategiaBuscaVinicius implements EstrategiaBusca {

    //variaveis de entrada
    private Map<Month, Mes> periodoTreino;
    private Map<Month, Mes> periodoTeste;

    //variveis que controlam dados financeiros
    private final Double CARTEIRA_INICIAL = 100000D;
    private final Map<String, Empresa> portfolio = new HashMap<>();
    private Double carteira = CARTEIRA_INICIAL;

    //parametros do problema
    private final int PESO_TREINO = 1;
    private final int PESO_TESTE = 4;
    private final int SOMA_PESOS = PESO_TESTE + PESO_TREINO;
    private static final int ULTIMO_DIA_MES = 29;
    private final double EXPECTATIVA_DE_ALTA_PARA_VENDER = 0.5;
    private final double EXPECTATIVA_DE_ALTA_PARA_COMPRAR = 0.3;

    //variaveis que controlam ciclo de execucao
    private final Locale BRAZIL = new Locale("pt","BR");
    private LocalDate diaAtual = LocalDate.of(2016, Month.JANUARY, 1);
    private final int DIAS_2016 = 366;
    private int diasRestantes = DIAS_2016;
    
    private final StringBuilder relatorio = new StringBuilder();
    
    @Override
    public void recebeDadosTreino(List<Dia> periodo) {
        periodoTreino = new HashMap<>();
        periodo.forEach((dia) -> {
            Month mesDia = dia.getData().getMonth();
            if (periodoTreino.containsKey(mesDia)) {
                periodoTreino.get(mesDia).dias.add(dia);
            } else {
                periodoTreino.put(mesDia, new Mes((mesDia)));
            }
        });
    }
    
    @Override
    public void recebeDadosTeste(List<Dia> periodo) {
        periodoTeste = new HashMap<>();
        periodo.forEach((dia) -> {
            Month mesDia = dia.getData().getMonth();
            if (periodoTeste.containsKey(mesDia)) {
                periodoTeste.get(mesDia).dias.add(dia);
            } else {
                periodoTeste.put(mesDia, new Mes((mesDia)));
            }
        });
    }
    
    private void inicializaEmpresas() {
        periodoTeste.get(diaAtual.getMonth()).dias.forEach((dia) -> {
            portfolio.put(dia.getSigla(), new Empresa(dia.getSigla(), dia.getAcao()));
        });
    }
    
    @Override
    public String devolveValorPortfolio() {
        return String.valueOf("\nCarteira obteve "
                + Math.floor(((carteira / CARTEIRA_INICIAL) * 100) * 100) / 100
                + "% do valor investido.\n");
    }
    
    @Override
    public String devolveResultadosMesAMes() {
        return relatorio.toString();
    }
    
    @Override
    public String devolveAcaoMaiorGanho() {
        double maiorGanho = Double.MIN_VALUE;
        Empresa melhorEmpresa = null;
        for (Empresa empresa : portfolio.values()) {
            if (empresa.investimento != 0) {
                if (empresa.receita / empresa.investimento > maiorGanho) {
                    maiorGanho = empresa.receita / empresa.investimento;
                    melhorEmpresa = empresa;
                }
            }
        }
        if (melhorEmpresa == null) {
            return "Nao houve ganho no periodo";
        }
        return String.valueOf("Empresa " + melhorEmpresa.nome + " obteve "
                + Math.floor((maiorGanho * 100) * 100) / 100 + "% do valor investido.");
    }
    
    @Override
    public String devolveAcaoMaiorPrejuizo() {
        double menorGanho = Double.MAX_VALUE;
        Empresa piorEmpresa = null;
        for (Empresa empresa : portfolio.values()) {
            if (empresa.investimento != 0) {
                if (empresa.receita / empresa.investimento < menorGanho) {
                    menorGanho = empresa.receita / empresa.investimento;
                    piorEmpresa = empresa;
                }
            }
        }
        if (piorEmpresa == null) {
            return "Nao houve prejuizo no periodo";
        }
        
        return String.valueOf("Empresa " + piorEmpresa.nome + " obteve "
                + Math.floor((menorGanho * 100) * 100) / 100 + "% do valor investido."
        );
    }
    
    @Override
    public void aplicaEstrategiaBusca() {
        inicializaEmpresas();
        while (diasRestantes > 1) {
            calculaExpectativaDeMercadoDiaria();
            investe();
            avancaDia();
            if (diaAtual.getDayOfMonth() == ULTIMO_DIA_MES) {
                geraResultadoMes();
            }
        }
        gastaTudo();
    }
    
    private void calculaExpectativaDeMercadoDiaria() {
        for (String empresa : CarregaDados.SIGLAS) {
            boolean TESTE = false;
            boolean TREINO = true;
            portfolio.get(empresa).expectativaPrecoAlto
                    = (calculaRSI(empresa, TREINO) * PESO_TREINO + calculaRSI(empresa, TESTE) * PESO_TESTE) / SOMA_PESOS;
        }
    }
    
    private double calculaRSI(String sigla, boolean treino) {
        List<Double> precos = new ArrayList<>();
        Mes mesTreino = treino ? periodoTreino.get(diaAtual.getMonth()) : periodoTeste.get(diaAtual.getMonth());
        
        mesTreino.dias.stream()
                .filter((dia) -> (dia.getSigla().equals(sigla)))
                .filter((dia) -> (dia.getData().getDayOfMonth() <= diaAtual.getDayOfMonth()))
                .forEachOrdered((dia) -> {
                    precos.add(dia.getValorFechamento());
                });
        
        int periodo = precos.size();
        
        double lucroMedio = 0, prejuizoMedio = 0;
        for (int dia = 1; dia < periodo; dia++) {
            double alteracao = precos.get(dia) - precos.get(dia - 1);
            if (alteracao >= 0) {
                lucroMedio += alteracao;
            } else {
                prejuizoMedio += alteracao;
            }
        }
        
        double rs = lucroMedio / Math.abs(prejuizoMedio);
        double rsi = 100 - (100 / (1 + rs));
        
        if (Double.isNaN(rsi)) {
            return 0.5;
        }
        
        return rsi / 100;
    }
    
    private void investe() {
        portfolio.values().forEach((empresa) -> {
            if (empresa.nrDeAcoes > 0 && empresa.expectativaPrecoAlto > EXPECTATIVA_DE_ALTA_PARA_VENDER) {
                double valorVenda = empresa.nrDeAcoes * achaPrecoAcao(empresa, diaAtual);
                carteira += valorVenda;
                empresa.nrDeAcoes = 0L;
                empresa.receita += valorVenda;
            } else if (empresa.expectativaPrecoAlto < EXPECTATIVA_DE_ALTA_PARA_COMPRAR) {
                double porcentagemMaximaCompra = calculaPorcentagemMaxima();
                double valorAcao = achaPrecoAcao(empresa, diaAtual);
                int nrAcoesCompradas = 0;
                if (valorAcao != 0) {
                    nrAcoesCompradas = (int) ((carteira * porcentagemMaximaCompra) / valorAcao);
                }
                double valorGasto = nrAcoesCompradas * valorAcao;
                carteira -= valorGasto;
                empresa.nrDeAcoes += nrAcoesCompradas;
                empresa.investimento += valorGasto;
            }
        });
    }
    
    private double calculaPorcentagemMaxima() {
        //return 1 - (diasRestantes / (float) DIAS_2016);
        return 1;
    }
    
    private void gastaTudo() {
        portfolio.values().forEach((empresa) -> {
            if (empresa.nrDeAcoes > 0) {
                double valorVenda = empresa.nrDeAcoes * achaPrecoAcao(empresa, diaAtual);
                carteira += valorVenda;
                empresa.nrDeAcoes = 0L;
                empresa.receita += valorVenda;
            }
        });
    }
    
    private double gastaTudoComCarteiraFalsa() {
        double carteiraFalsa = carteira;
        for (Empresa empresa : portfolio.values()) {
            double valorVenda = empresa.nrDeAcoes * achaPrecoAcao(empresa, diaAtual);
            carteiraFalsa += valorVenda;
        }
        return carteiraFalsa;
    }
    
    private double achaPrecoAcao(Empresa empresa, LocalDate diaAtual) {
        double precoAcao = 0D;
        for (Dia dia : periodoTeste.get(diaAtual.getMonth()).dias) {
            if (diaAtual.getDayOfMonth() == dia.getData().getDayOfMonth()
                    && dia.getSigla().equals(empresa.id)) {
                precoAcao = dia.getValorFechamento();
            }
        }
        if (precoAcao == 0) {
            if (diaAtual.getDayOfYear() > 1) {
                return achaPrecoAcao(empresa, diaAtual.minusDays(1));
            }
        }
        return precoAcao;
    }
    
    private void avancaDia() {
        diasRestantes -= 1;
        diaAtual = diaAtual.plusDays(1);
    }
    
    private void geraResultadoMes() {
        relatorio.append("Mes: ").append(diaAtual.getMonth()
                .getDisplayName(TextStyle.FULL, BRAZIL)).append("\n");
        relatorio.append("Carteira obteve ")
                .append(
                        Math.floor(
                                ((gastaTudoComCarteiraFalsa() / CARTEIRA_INICIAL) * 100) * 100) / 100)
                .append("% do valor investido." + "\n");
        relatorio.append(devolveAcaoMaiorGanho()).append("\n");
        relatorio.append(devolveAcaoMaiorPrejuizo()).append("\n");
        relatorio.append("-----------------------------------------\n");
    }
    
    private static class Mes {
        
        Month mes;
        List<Dia> dias;
        
        public Mes(Month mes) {
            this.mes = mes;
            dias = new ArrayList<>();
        }
    }
    
    private class Empresa implements Comparable<Empresa> {
        
        @Override
        public int compareTo(Empresa o) {
            return this.receita.compareTo(o.receita);
        }
        
        String id;
        String nome;
        Long nrDeAcoes = 0L;
        Double receita = 0D;
        Double investimento = 0D;
        Double expectativaPrecoAlto;
        
        public Empresa(String id, String nome) {
            this.id = id;
            this.nome = nome;
            
        }
        
        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + Objects.hashCode(this.id);
            return hash;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Empresa other = (Empresa) obj;
            if (!Objects.equals(this.id, other.id)) {
                return false;
            }
            return true;
        }
        
    }
}
