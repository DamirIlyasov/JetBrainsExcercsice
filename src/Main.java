import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Thread.sleep;

public class Main {

    public static String dirIn;
    public static String dirOut;
    public static String mask;
    public static int waitInterval;
    public static boolean includeSubfolders;
    public static boolean autoDelete;
    public static boolean checher = true;
    public static String errorMsg= "";
    public static int scanCounter = 0;
    public static int dirCounter = 0;


    public static void main(String[] args) throws IOException {


        Scanner aa = new Scanner(System.in);
        Thread myThread = new Thread();
        String input;
        String inputAll;

        counter:
        while (true) {                       //Зацикливаем, чтобы основной поток только считывал данные. ( scan и exit )
            input = aa.next();
            inputAll = aa.nextLine();
            if (input.equals("scan")) {
                scanCounter++;              // Увеличиваем значение переменной scanCounter, чтобы предотвратить
                if (scanCounter == 2) {     // ввод команды scan 2 раза подряд(без остановки первого скана командой exit)
                    System.out.println("Нельзя запускать метод scan, пока сканер работает.");
                    scanCounter = 0;
                    continue counter;

                }
                myThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            try {
                                superScan();                  // Запускается сканер и вывод сообщений о начале и конце сканирования.
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try {
                                sleep(waitInterval);            // выжидаем интервал. И снова работать!
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                setParameters(inputAll);
                if (checher) {                      // Если все входные данные нас устроили, то checker будет true. Тогда запускаем второй поток.
                    myThread.start();
                } else {
                    errorMsg = "Error: Scanner was not started due to following reasons:" + "\n" + errorMsg;       // Иначе выводим сообщение об ошибке
                    System.out.println(errorMsg);                                                                  // С указанием причины ошибки
                    errorMsg = "";
                    checher = true;
                }
            }                               //stop в нашем случае безопасен, т.к. мы останавливаем скан только после завершения сканирования.
            if (input.equals("exit")) {                                         //Если на вход получаем exit, то убиваем второй поток, обнуляем переменные
                myThread.stop();                                               // и можно запускать его снова. Основной поток всё ещё ждёт данные
                scanCounter = 0;                                    // Обнуляем scanCounter. Т.к. сканер останавливается.
                System.out.println("Сканирование прекращено.");
                resetValues();
            }
            if (!input.equals("scan") && !input.equals("exit")){
                System.out.println("Вы ввели неверную команду.");
            }
        }
    }



    // проверяет по маске
    public static boolean maskCheck(File item, String mask) {
        Pattern p = Pattern.compile(mask);
        Matcher m = p.matcher(item.getName());
        return m.matches();
    }


    // главный метод, сканирует, копирует и тд.. При includeSubfolders == true сохраняет структуру подкаталога.
   public static void scan(String dirIn, String dirOut, String mask, int waitInterval, boolean includeSubfolders, boolean autoDelete) throws IOException {

        File dirInScan = new File(dirIn);

        for (File item : dirInScan.listFiles()) {
            if (item.isDirectory()) {
                if (includeSubfolders) {
                    dirCounter++;                   // Считаем кол-во поддиректорий, которые нужно будет создать
                    scan(item.getPath(), dirOut+item.getName()+"\\", mask, waitInterval, includeSubfolders, autoDelete);
                }
            }
            if (maskCheck(item, mask)) {
                File outFile = new File(dirOut + item.getName());
                if (includeSubfolders){
                    if (dirCounter>1){              // Если поддиректорий больше , чем одна, то используем .mkdirs();
                        outFile.mkdirs();
                    }
                    if (dirCounter == 1){           // Если поддиректория одна, то используем .mkdir();
                        outFile.mkdir();
                    }
                    dirCounter = 0;             // Обнуляем счётчик для следующего сканирования
                }
                Files.copy(item.toPath(), outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                if (autoDelete) {
                    item.delete();              // Удаляем файл из директории сканирования, если autoDelete==true
                }
            }
        }


    }


    // трансформирует маску в читабельный для программы вид. Регулярыне выражения....
    public static String maskTransform(String mask) {

        String[] maskArray = mask.split("");
        mask = "";
        for (int i = 0; i < maskArray.length; i++) {
            if (maskArray[i].equals("*")) {
                maskArray[i] = ".*";
            }
            if (maskArray[i].equals(".")) {
                maskArray[i] = "\\.";
            }
            if (maskArray[i].equals("?")){
                maskArray[i] = "\\.?";
            }
            mask = mask + maskArray[i];
        }
        return mask;
    }


    // Задаёт все параметры с клавиатуры. Например: scan -input "C:\Users\hp\Desktop\JBdir" -output "C:\Users\hp\Desktop\JBdirSecond" -mask "*ForCopy*" -waitInterval 5000 -includeSubfolders true -autoDelete false
    // Если параметры нас не устроят, то значение boolean checker будет равно false, сканер не запустится и выдаст ошибку с описанием проблемы.
    public static void setParameters(String inputAll) {

        String[] inputArray = inputAll.split("\"");
        String[] inputArray2 = new String[0];
        int count = 0;
        for (int i = 0; i < inputArray.length; i++){

            switch (inputArray[i]){
                case " –input ": {
                    dirIn = inputArray[++i];
                    File dirIn1 = new File(dirIn);
                    if (!dirIn1.isDirectory()){
                        checher = false;
                        errorMsg = "Illegal symbols in input path: "+ dirIn+"\n";
                    }
                    count++;
                    break;
                }
                case " –output ": {

                    dirOut = inputArray[++i];
                    File dirOut1 = new File(dirOut);
                    if (!dirOut1.isDirectory()){
                        checher = false;
                        errorMsg = errorMsg + "Illegal symbols in output path: "+ dirOut +"\n";
                    }
                    dirOut=dirOut+"\\";
                    count++;
                    break;
                }
                case " –mask ": {
                    for (String mask1 : inputArray[i+1].split("")){
                        if (mask1.equals("!") || mask1.equals("~") || mask1.equals("`") || mask1.equals("@") || mask1.equals("#") || mask1.equals("№") || mask1.equals("$") || mask1.equals("%") || mask1.equals("^") || mask1.equals("&") || mask1.equals("(") || mask1.equals(")") || mask1.equals("-") || mask1.equals("\\") || mask1.equals("/")){
                            checher = false;
                            errorMsg = errorMsg + "Illegal symbols in mask: "+ inputArray[i+1]+"\n";
                            break;
                        }
                    }
                    mask = maskTransform(inputArray[++i]);
                    count++;
                    break;
                }
                default:{
                    inputArray2 = inputArray[i].split(" ");
                    break;
                }

            }
        }
        for (int i = 0; i < inputArray2.length; i ++ ){
            switch (inputArray2[i]){
                case "–waitInterval":{
                    waitInterval = new Integer(inputArray2[++i]);
                    if (waitInterval < 0){
                        checher = false;
                        errorMsg = errorMsg + "Incorrect value for waitInterval: "+ waitInterval+" (must be positive int value)"+"\n";
                    }
                    count++;
                    break;
                }
                case "–includeSubfolders":{
                    if (inputArray2[++i].equals("true")){
                        includeSubfolders = true;
                    }
                    if (inputArray2[i].equals("false")) {
                        includeSubfolders = false;
                    }
                    if (!inputArray2[i].equals("true") && !inputArray2[i].equals("false")){
                        checher = false;
                        errorMsg = errorMsg + "Invalid value for includeSubfolders: "+ inputArray2[i] + " (must be true or false)"+"\n";
                    }
                    count++;
                    break;
                }
                case "–autoDelete":{
                    if (inputArray2[++i].equals("true")){
                        autoDelete = true;
                    }
                    if (inputArray2[i].equals("false")){
                        autoDelete = false;
                    }
                    if (!inputArray2[i].equals("true") && !inputArray2[i].equals("false")){
                        checher = false;
                        errorMsg = errorMsg + "Invalid value for autoDelete: "+ inputArray2[i] + " (must be true or false)"+"\n";
                    }
                    count++;
                    break;
                }
                default: break;
            }
        }
        if (count != 6){
            checher = false;
            errorMsg = errorMsg + " Not all values are entered.";
        }
    }


    // Информирует о начале сканирования и конце. SOUT -----> Scan -----> SOUT. Для удобства.
    synchronized public static void superScan() throws IOException {

        System.out.println("Сканирование началось.");
        scan(dirIn, dirOut, mask, waitInterval, includeSubfolders,autoDelete);
        System.out.println("Сканирование закончилось.");
    }


   // Обнуляет переменные. Чтобы сканер не начал работать при неверных входных данных. Например, если после остановки первого сканера ввести команду начала неверно.
    public static void resetValues(){
        dirIn = "";
        dirOut = "";
        mask = "";
        waitInterval = 0;
        includeSubfolders = Boolean.parseBoolean(null);
        autoDelete = Boolean.parseBoolean(null);
        checher = true;
        errorMsg= "";
    }

}
