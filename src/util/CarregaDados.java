/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import modelo.Dia;

/**
 *
 * @author Camargo
 */
public class CarregaDados {

    public static List<Dia> carregaDados(File file1, File file2) {
        List dias = carregaDados(file1);
        dias.addAll(carregaDados(file2));
        return dias;
    }

    public static List<Dia> carregaDados(File file) {
        List dias = new ArrayList();
        try {
            Scanner input;

            input = new Scanner(file);
            input.nextLine();

            while (input.hasNextLine()) {
                String line = input.nextLine();

                String empresa = line.substring(27, 39);
                LocalDate data = LocalDate.parse(line.substring(02, 10), DateTimeFormatter.BASIC_ISO_DATE);
                Double precoAbertura = Double.valueOf(line.substring(56, 69));
                Double precoMax = Double.valueOf(line.substring(69, 82));
                Double precoMin = Double.valueOf(line.substring(82, 95));
                Double precoMedio = Double.valueOf(line.substring(95, 108));
                Double precoFechamento = Double.valueOf(line.substring(108, 121));

                //Double nrNegocios = Double.valueOf(line.substring(147, 152));
                //Double total = Double.valueOf(line.substring(152, 170));
                dias.add(new Dia(empresa, data, precoAbertura, precoFechamento, precoMin, precoMax, precoMedio, precoFechamento));
            }
            input.close();

        } catch (FileNotFoundException | NumberFormatException ex) {
            throw new Error("Houve um erro ao abrir o arquivo.");
        } catch (StringIndexOutOfBoundsException  | DateTimeParseException ex) {
            return dias;
        }
        return dias;
    }

}
