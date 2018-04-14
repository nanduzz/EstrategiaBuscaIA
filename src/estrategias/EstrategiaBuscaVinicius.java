/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package estrategias;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import modelo.Dia;
import util.CarregaDados;

/**
 *
 * @author Camargo
 */
public class EstrategiaBuscaVinicius implements EstrategiaBusca {

    private LocalDate diaAtual = LocalDate.of(2016, Month.JANUARY, 28);
    private Map<Month, Mes> periodoTreino;
    private Map<Month, Mes> periodoTeste;
    private final Double CARTEIRA_INICIAL = 1000000D;
    private final Map<String, Empresa> portfolio = new HashMap<>();
    private Double carteira = CARTEIRA_INICIAL;
    private final int RESFRIAMENTO = 1;
    int temperatura = 365;

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
            portfolio.put(dia.getSigla(), new Empresa(dia.getSigla(), dia.getAcao(), 0, 0D, 0D));
        });
    }

    @Override
    public Double devolveValorPorfolio() {
        return Collections.max(portfolio.values(),
                (Empresa e1, Empresa e2) -> e1.valorGerado.compareTo(e2.valorGerado))
                .valorGerado;
    }

    @Override
    public String devolveResultadosMesAMes() {
        return "";
    }

    @Override
    public String devolveAcaoMaiorGanho() {
        return "";
    }

    @Override
    public String devolveAcaoMaiorPrejuizo() {
        return "";
    }

    @Override
    public void aplicaEstrategiaBusca() {
        inicializaEmpresas();
        calculaExpectativaDeMercadoDiaria();
        while (temperatura > 1) {
            calculaExpectativaDeMercadoDiaria();
            temperatura -= RESFRIAMENTO;
            avancaDia();

        }
        /*
        // Loop until system has cooled
        while (temp > 1) {
            // Create new neighbour tour
            Tour newSolution = new Tour(currentSolution.getTour());

            // Get a random positions in the tour
            int tourPos1 = (int) (newSolution.tourSize() * Math.random());
            int tourPos2 = (int) (newSolution.tourSize() * Math.random());

            // Get the cities at selected positions in the tour
            City citySwap1 = newSolution.getCity(tourPos1);
            City citySwap2 = newSolution.getCity(tourPos2);

            // Swap them
            newSolution.setCity(tourPos2, citySwap1);
            newSolution.setCity(tourPos1, citySwap2);

            // Get energy of solutions
            int currentEnergy = currentSolution.getDistance();
            int neighbourEnergy = newSolution.getDistance();

            // Decide if we should accept the neighbour
            if (acceptanceProbability(currentEnergy, neighbourEnergy, temp) > Math.random()) {
                currentSolution = new Tour(newSolution.getTour());
            }

            // Keep track of the best solution found
            if (currentSolution.getDistance() < best.getDistance()) {
                best = new Tour(currentSolution.getTour());
            }

            // Cool system
            temp *= 1 - coolingRate;
            
        }

        System.out.println("Final solution distance: " + best.getDistance());
        System.out.println("Tour: " + best);
         */
    }

    private void calculaExpectativaDeMercadoDiaria() {
        for (String empresa : CarregaDados.SIGLAS) {
            portfolio.get(empresa).expectativaLucroVenda = portfolio.get(empresa).expectativaLucroVenda + calculaRSI(empresa) / 2;
        }
    }

    private double calculaRSI(String sigla) {
        List<Double> precos = new ArrayList<>();
        Mes mesTreino = periodoTreino.get(diaAtual.getMonth());

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

    private void avancaDia() {
        diaAtual = diaAtual.plusDays(RESFRIAMENTO);
    }

    private static class Mes {

        Month mes;
        List<Dia> dias;

        public Mes(Month mes) {
            this.mes = mes;
            dias = new ArrayList<>();
        }
    }

    private class Empresa {

        String id;
        String nome;
        Integer nrDeAcoes;
        Double valorGerado;
        Double expectativaLucroVenda;

        public Empresa(String id, String nome, Integer nrDeAcoes, Double valorGerado, Double expectativaLucroVenda) {
            this.id = id;
            this.nome = nome;
            this.nrDeAcoes = nrDeAcoes;
            this.valorGerado = valorGerado;
            this.expectativaLucroVenda = expectativaLucroVenda;
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
