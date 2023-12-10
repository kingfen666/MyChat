package org.kingfen;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.kingfen.ChatGpt.MakeChat;

public class Main {
    public static void main(String[] args) {
        System.setProperty("file.encoding","UTF-8");
        Scanner scanner=new Scanner(System.in,"GBK");
        System.out.print("我:");
        while (true){
            String next = scanner.next();
            if (next.isEmpty()){
                System.out.print("我:");
            }else {
                MakeChat.chat(next);
            }
        }
    }
}
