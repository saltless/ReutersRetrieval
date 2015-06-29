package com.IR.util;

import java.io.*;


public class TermStem 
{
	public String stemmed;
	
	public TermStem(String original)
   {
      char[] w = new char[501];
      StemByPorter s = new StemByPorter();
      ByteArrayInputStream in = new ByteArrayInputStream(original.getBytes());
      try{ 
    	  while(true)
    	  {  
    		  int ch = in.read();
              if (Character.isLetter((char) ch))
              {
                 int j = 0;
                 while(true)
                 {  ch = Character.toLowerCase((char) ch);
                    w[j] = (char) ch;
                    if (j < 500) j++;
                    ch = in.read();
                    if (!Character.isLetter((char) ch))
                    {
                       for (int c = 0; c < j; c++) s.add(w[c]);
                       s.stem();
                       {  
                    	   String u;
                    	   u = s.toString();
                    	   this.stemmed=u;
                    	   //System.out.print(u);
                       }
                       break;
                    }
                 }
              }
              if (ch < 0) break;
              //System.out.print((char)ch);
           }
         }
         catch (Exception e)
         {  
        	 System.out.println("error reading original word");
         }
      }
   }