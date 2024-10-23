package com.hxw.wxchat.controller;

public class Test {

    public static void main(String[] args) {
        Test test=new Test();
    System.out.println(test.numDistinct( "babgbag","bag"));
    }
    static int count=0;

    public static   int numDistinct(String s, String t) {

        char[] ch_s=s.toCharArray();
        char[] ch_t=t.toCharArray();
        int  len_s=ch_s.length;
        int  len_t=ch_t.length;
        int index=0;
        for (int i = 0; i <len_s ; i++) {

            if (ch_s[i]==ch_t[index]){
                if (ch_t.length==1){

                    count++;

                    continue;
                }

                numDistinct(s.substring(i+1,len_s),t.substring(index+1,len_t));
            }


        }
        return count;
    }
}
